package eis;

import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import jason.environment.Environment;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;

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

    // private HashSet<Percept> cartographerPerceptSet = new HashSet<Percept>();

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
                addPercept(agent.getJasonName(), Literal.parseLiteral("myName(" + agent.getServerName().toLowerCase() + ")"));
                addPercept(agent.getJasonName(), Literal.parseLiteral("myTeam(" + agent.getTeam() + ")"));

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
        logger.info("DEBUG: executeAction(" + agentJasonName + ", " + command + ")");
        String agentServerName = jasonAgentMap.get(agentJasonName).getServerName();
        Action action = new Action("skip");
        String functor = command.getFunctor();
        if (command.getArity() == 0) {
            action = new Action(functor);
        } else if (command.getArity() == 1) {
            String entityName = command.getTerm(0).toString();
            // convert agent names from lower to mixed case (otherwise we get
            // false_wrong_param results from our actions)
            MapAgent.getInstance();
            if (MapAgent.agentNameConversionMap.containsKey(entityName)) {
                MapAgent.getInstance();
                entityName = MapAgent.agentNameConversionMap.get(entityName);
            }
            action = new Action(functor, new Identifier(entityName));
        }
        try {
            Agent agent = MapAgent.getInstance().getAgent(agentServerName);
            int step = MapAgent.getInstance().getStep();
            logger.info("Agent " + agent + " wants to " + action + " in step " + step);
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
        String jasonNameOfAgent = serverAgentMap.get(agentName).getJasonName();
        // The following if-else block was added because agents were missing out
        // on the initial list of beliefs. This is of course a dirty workaround,
        // but I can't think of any other way to fix this issue. -sewell
        if (percepts.toString().contains("role")) {
            delayedPerceptsMap.put(jasonNameOfAgent, percepts);
            return;
        } else {
            if (delayedPerceptsMap.containsKey(jasonNameOfAgent)) {
                percepts.addAll(delayedPerceptsMap.get(jasonNameOfAgent));
                delayedPerceptsMap.remove(jasonNameOfAgent);
            }
        }

        Percept requestAction = null;
        clearPercepts(jasonNameOfAgent);
        // clearPercepts("cartographer");
        // logger.info("Received percepts for " + jasonName + ": " +
        // percepts.toString());
        for (Percept percept : percepts) {
            String perceptName = percept.getName();
            Agent agent = MapAgent.getInstance().getAgent(agentName);
            // Make sure that the requestAction percept is handled last by the
            // agents because when the agent receives the requestAction
            // percept, it determines
            // which action to perform in the current step. By this point, all
            // other percepts need to have been handled properly already.
            if (perceptName.equalsIgnoreCase("requestAction")) {
                requestAction = percept;
                continue;
            }
            if (!perceptName.equalsIgnoreCase("lastActionParam")) {
                // logger.info("Sending percept " + perceptToLiteral(percept) +
                // " to agent MapAgent.");
                MapAgent.getInstance().addPercept(percept);
                addAgentPercept(jasonNameOfAgent, percept);
            }
            if (perceptName.equalsIgnoreCase("position")) {
                Vertex position = MapAgent.getInstance().getVertex(percept.getParameters().get(0).toString());
                agent.setPosition(position);
            }
            if (perceptName.equalsIgnoreCase("health")) {
                int health = Integer.parseInt(percept.getParameters().get(0).toString());
                agent.setHealth(health);
            }
            if (perceptName.equalsIgnoreCase("maxEnergy")) {
                int maxEnergy = Integer.parseInt(percept.getParameters().get(0).toString());
                agent.setMaxEnergy(maxEnergy);
            }
            if (perceptName.equalsIgnoreCase("maxHealth")) {
                int maxHealth = Integer.parseInt(percept.getParameters().get(0).toString());
                agent.setMaxHealth(maxHealth);
            }
            if (perceptName.equalsIgnoreCase("strength")) {
                int strength = Integer.parseInt(percept.getParameters().get(0).toString());
                agent.setStrength(strength);
            }
            if (perceptName.equalsIgnoreCase("visRange")) {
                int visRange = Integer.parseInt(percept.getParameters().get(0).toString());
                agent.setVisRange(visRange);
            }
            if (perceptName.equalsIgnoreCase("money")) {
                int money = Integer.parseInt(percept.getParameters().get(0).toString());
                MapAgent.getInstance().setMoney(money);
            }
            if (perceptName.equalsIgnoreCase("role")) {
                String role = percept.getParameters().get(0).toString();
                agent.setRole(role);
            }
        }
        if (requestAction != null) {
            addAgentPercept(jasonNameOfAgent, requestAction);
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
        case "visibleEntity": {
            // Add the name of the agent to the name conversion map.
            String entityName = percept.getParameters().getFirst().toString();
            MapAgent.getInstance();
            MapAgent.agentNameConversionMap.put(entityName.toLowerCase(), entityName);
            String escaped = percept.toProlog().toLowerCase().replace("visibleentity", "visibleEntity");
            return Literal.parseLiteral(escaped);
        }
        case "visibleVertex": {
            // Team names can start with a capital letter, which AgentSpeak
            // interprets as a variable name, so we have to convert them to
            // lowerCase.
            String lowerCasePercept = percept.toProlog().toLowerCase().replace("visiblevertex", "visibleVertex");
            return Literal.parseLiteral(lowerCasePercept);
        }
        case "inspectedEntity": {
            String escaped = percept.toProlog().toLowerCase().replace("inspectedentity", "inspectedEntity");
            return Literal.parseLiteral(escaped);
        }
        default:
            return Literal.parseLiteral(percept.toProlog());
        }
    }
}
