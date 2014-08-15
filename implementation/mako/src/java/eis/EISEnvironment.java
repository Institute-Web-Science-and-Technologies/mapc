package eis;

import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import jason.environment.Environment;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import eis.exceptions.ActException;
import eis.exceptions.AgentException;
import eis.exceptions.ManagementException;
import eis.exceptions.RelationException;
import eis.iilang.Action;
import eis.iilang.Identifier;
import eis.iilang.Percept;

/**
 * @author Artur Daudrich
 * @author Michael Sewell
 */
public class EISEnvironment extends Environment implements AgentListener {

    public static String NAME = EISEnvironment.class.getName();
    private EnvironmentInterfaceStandard environmentInterface;
    private AgentLogger logger = new AgentLogger(EISEnvironment.NAME);
    private HashMap<String, Agent> serverAgentMap = new HashMap<String, Agent>();
    private HashMap<String, Agent> jasonAgentMap = new HashMap<String, Agent>();
    private HashMap<String, Collection<Percept>> delayedPerceptsMap = new HashMap<String, Collection<Percept>>();
    private HashSet<Percept> cartographerPerceptSet = new HashSet<Percept>();

    /*
     * jason lifecycle: init -> user-init -> compile -> run -> user-end
     */
    @Override
    public void init(String[] args) {
        // logger
        logger.setVisible(true);

        // init EISMASSIM environment
        try {
            environmentInterface = EILoader.fromClassName("massim.eismassim.EnvironmentInterface");
        } catch (IOException e) {
            e.printStackTrace();
        }
        serverAgentMap = AgentHandler.createAgents();
        jasonAgentMap = initJasonAgentMap();
        initAgents();
        try {
            environmentInterface.start();
        } catch (ManagementException e) {
            e.printStackTrace();
        }
    }

    private HashMap<String, Agent> initJasonAgentMap() {
        HashMap<String, Agent> map = new HashMap<String, Agent>();
        for (String serverAgentName : serverAgentMap.keySet()) {
            Agent agent = serverAgentMap.get(serverAgentName);
            map.put(agent.getJasonName(), agent);
        }
        return map;
    }

    /**
     * This function is called when the AgentHandler is initialized. It is a
     * helper function that registers the agents with the server. It also sets
     * up the agents so that they are registered as listeners with the server
     * and will receive percepts related to them.
     */
    private void initAgents() {
        for (Agent agent : serverAgentMap.values()) {
            try {
                // tell server which agents are there
                environmentInterface.registerAgent(agent.getServerName());

                // tell server which agent is connected to which entity
                environmentInterface.associateEntity(agent.getServerName(), agent.getEntity());
                addPercept(agent.getJasonName(), Literal.parseLiteral("myName(" + agent.getServerName() + ")"));
                addPercept(agent.getJasonName(), Literal.parseLiteral("myTeam(" + agent.getTeam() + ")"));
                addPercept(agent.getJasonName(), Literal.parseLiteral("enemyTeam(" + agent.getEnemyTeam() + ")"));
                // listener for global percepts from the server
                environmentInterface.attachAgentListener(agent.getServerName(), this);
            } catch (AgentException e) {
                e.printStackTrace();
            } catch (RelationException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void stop() {
        if (environmentInterface != null) {
            try {
                if (environmentInterface.isKillSupported())
                    environmentInterface.kill();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean executeAction(String agentJasonName, Structure command) {
        logger.info("Agent " + agentJasonName + " wants to execute action " + command + ".");
        String agentServerName = jasonAgentMap.get(agentJasonName).getServerName();
        Action action = new Action("skip");
        String functor = command.getFunctor();
        if (command.getArity() == 0) {
            action = new Action(functor);
        } else if (command.getArity() == 1) {
            String entityName = command.getTerm(0).toString();
            action = new Action(functor, new Identifier(entityName));
        }
        try {
            environmentInterface.performAction(agentServerName, action);
            return true;
        } catch (ActException e) {
            return false;
        }
    }

    @Override
    public void handlePercept(String agentName, Percept percept) {
        logger.info("Kommen wir hier jemals rein?");
        String jasonName = serverAgentMap.get(agentName).getJasonName();
        Literal literal = Literal.parseLiteral(percept.toProlog());
        // removePercept(jasonName, literal);
        addPercept(jasonName, literal);
    }

    /**
     * This method clears earlier percepts with {@code [source(percept)]} of the
     * respective agents and then adds the new ones from the current step.
     */
    @Override
    public void handlePercept(String agentName, Collection<Percept> percepts) {
        // agentName: agentA1, jasonName: explorer1
        String jasonName = serverAgentMap.get(agentName).getJasonName();
        // The following if-else block was added because agents were missing out
        // on the initial list of beliefs. This is of course a dirty workaround,
        // but I can't think of any other way to fix this issue. -sewell
        if (percepts.toString().contains("role")) {
            delayedPerceptsMap.put(jasonName, percepts);
            return;
        } else {
            if (delayedPerceptsMap.containsKey(jasonName)) {
                percepts.addAll(delayedPerceptsMap.get(jasonName));
                delayedPerceptsMap.remove(jasonName);
            }
        }
        Percept requestAction = null;
        clearPercepts(jasonName);
        // clearPercepts("cartographer");
        // logger.info("Received percepts for " + jasonName + ": " +
        // percepts.toString());
        for (Percept percept : percepts) {
            // Make sure that the requestAction percept is handled last by the
            // agents because when the agent receives the requestAction
            // percept, it determines
            // which action to perform in the current step. By this point, all
            // other percepts need to have been handled properly already.
            if (percept.getName().equalsIgnoreCase("requestAction")) {
                requestAction = percept;
                continue;
            }
            if (!percept.getName().equalsIgnoreCase("lastActionParam")) {
                addCartographerPercept(percept);
                addAgentPercept(jasonName, percept);
            }
        }
        if (requestAction != null) {
            addAgentPercept(jasonName, requestAction);
        }

    }

    private void addCartographerPercept(Percept percept) {
        Literal literal = perceptToLiteral(percept);
        if (!cartographerPerceptSet.contains(percept)) {
            switch (percept.getName()) {
            // case "visibleEntity":
            case "probedVertex":
            case "visibleVertex":
            case "visibleEdge":
            case "surveyedEdge":
            case "edges":
            case "vertices":
            case "step":
                cartographerPerceptSet.add(percept);
                // logger.info("Sending percept " + literal +
                // " to cartographer.");
                addPercept("cartographer", literal);
                break;
            }
        }
    }

    private void addAgentPercept(String jasonName, Percept percept) {
        Literal literal = perceptToLiteral(percept);
        // logger.info("Sending percept " + perceptToLiteral(percept) +
        // " to agent " + jasonName + ".");
        addPercept(jasonName, literal);
    }

    private Literal perceptToLiteral(Percept percept) {
        switch (percept.getName()) {
        case "role":
            return Literal.parseLiteral(percept.toProlog().toLowerCase());
        case "visibleVertex":
        case "visibleEntity":
            String escaped = percept.toProlog().replace("A", "teamA").replace("B", "teamB");
            return Literal.parseLiteral(escaped);
        case "inspectedEntity":
            String escaped2 = percept.toProlog().toLowerCase().replace("inspectedentity", "inspectedEntity");
            return Literal.parseLiteral(escaped2);
        default:
            return Literal.parseLiteral(percept.toProlog());
        }
    }
}
