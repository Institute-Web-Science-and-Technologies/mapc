package eis;

import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import jason.environment.Environment;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;

import eis.exceptions.ActException;
import eis.exceptions.AgentException;
import eis.exceptions.ManagementException;
import eis.exceptions.RelationException;
import eis.iilang.Action;
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
        // logger.info("agName: " + agentJasonName);
        // logger.info("Functor: " + command.getFunctor());
        // logger.info("Terms: " + command.getTerms());
        String agentServerName = jasonAgentMap.get(agentJasonName).getServerName();
        Action action = ActionHandler.skip();
        String functor = command.getFunctor();
        switch (functor.toLowerCase(Locale.ENGLISH)) {
        case ("goto"): {
            String nodeName = command.getTerm(0).toString();
            action = ActionHandler.goTo(nodeName);
        }
        case ("survey"): {
            action = ActionHandler.survey();
        }
        case ("recharge"): {
            action = ActionHandler.recharge();
        }
        }
        try {
            environmentInterface.performAction(agentServerName, action);
            logger.info(agentServerName + ": " + action.getName());
            return true;
        } catch (ActException e) {
            return false;
        }
    }

    @Override
    public void handlePercept(String agentName, Percept percept) {
        String jasonName = serverAgentMap.get(agentName).getJasonName();
        Literal literal = JavaJasonTranslator.perceptToLiteral(percept);
        addPercept(jasonName, literal);
    }

    @Override
    public void handlePercept(String agentName, Collection<Percept> percepts) {
        for (Percept percept : percepts) {
            if (!percept.getName().equalsIgnoreCase("lastActionParam")) {
                String jasonName = serverAgentMap.get(agentName).getJasonName();
                Literal literal = JavaJasonTranslator.perceptToLiteral(percept);
                addPercept(jasonName, literal);
            }
        }
    }
}
