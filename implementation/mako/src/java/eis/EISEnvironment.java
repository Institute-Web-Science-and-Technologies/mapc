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
import eis.iilang.Identifier;
import eis.iilang.Numeral;
import eis.iilang.Percept;
import eis.iilang.TruthValue;
import graph.Graph;

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
    public boolean executeAction(String agentJasonName, Structure action) {
        logger.info("agName: " + agentJasonName);
        logger.info("Functor: " + action.getFunctor());
        logger.info("Terms: " + action.getTerms());
        if (action.getFunctor().equals("recharge")) {
            try {
                String agentServerName = jasonAgentMap.get(agentJasonName).getServerName();
                environmentInterface.performAction(agentServerName, ActionHandler.recharge());
                return true;
            } catch (ActException e) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void handlePercept(String agentName, Percept percept) {
        // TODO Auto-generated method stub

    }

    @Override
    public void handlePercept(String agentName, Collection<Percept> percepts) {
        // TODO Auto-generated method stub
        for (Percept percept : percepts) {
            updateSimulationState(percept);
            if (!percept.getName().equalsIgnoreCase("lastActionParam")) {
                String jasonName = serverAgentMap.get(agentName).getJasonName();
                Literal literal = JavaJasonTranslator.perceptToLiteral(percept);
                addPercept(jasonName, literal);
            }
        }
    }

    /**
     * When the AgentHandler receives a new percept from the server, this method
     * is called. It extracts the information from the percepts and saves them
     * in the relevant Java classes' attributes.
     * 
     * @param percept
     *            the percept that information will be extracted from.
     */
    private void updateSimulationState(Percept percept) {
        switch (percept.getName()) {
        /**
         * step(<Numeral>) represents the current step of the current
         * simulation.
         */
        case "step": {
            SimulationState.getInstance().setStep(percept.getParameters());
            break;
        }
        /**
         * steps(<Numeral>) represents the overall number of steps of the
         * current simulation.
         */
        case "steps": {
            SimulationState.getInstance().setMaxSteps(percept.getParameters());
            break;
        }
        /**
         * timestamp(<Numeral>) represents the moment in time, when the last
         * message was sent by the server, again in Unix-time.
         */
        case "timestamp": {
            SimulationState.getInstance().setLastTimeStamp(percept.getParameters());
            break;
        }
        /**
         * deadline(<Numeral>) indicates the deadline for sending a valid
         * action-message to the server in Unix-time.
         */
        case "deadline": {
            SimulationState.getInstance().setDeadline(percept.getParameters());
            break;
        }
        /** bye indicates that the tournament is over. */
        case "bye": {
            SimulationState.getInstance().setIsTournamentOver(new TruthValue(true));
            break;
        }
        /**
         * id(<Identifier>): indicates the identifier of the current simulation.
         */
        case "id": {
            SimulationState.getInstance().setId(percept.getParameters());
            break;
        }
        /**
         * lastStepScore(<Numeral>) indicates the score of the vehicle's team in
         * the last step of the current simulation.
         */
        case "lastStepScore": {
            SimulationState.getInstance().setLastStepScore(percept.getParameters());
            break;
        }
        /**
         * score(<Numeral>) represents is the overall score of the vehicle's
         * team.
         */
        case "score": {
            SimulationState.getInstance().setScore(percept.getParameters());
            break;
        }
        /** achievement(<Identifier>) denotes an achievement. */
        case "achievement": {
            SimulationState.getInstance().addAchievement(percept.getParameters());
            break;
        }
        /**
         * money(<Numeral>) denotes the amount of money available to the
         * vehicle's team.
         */
        case "money": {
            SimulationState.getInstance().setMoney(percept.getParameters());
            break;
        }
        /**
         * ranking(<Numeral>) indicates the outcome of the simulation for the
         * vehicle's team, that is its ranking.
         */
        case "ranking": {
            SimulationState.getInstance().setRanking(percept.getParameters());
            break;
        }
        /**
         * edges(<Numeral>) represents the number of edges of the current
         * simulation.
         */
        case "edges": {
            SimulationState.getInstance().setEdgeCount(percept.getParameters());
            Numeral edgesAmount = (Numeral) percept.getParameters().get(0);

            Graph.getInstance().setGlobalEdgesAmount(edgesAmount);
            break;
        }
        /**
         * vertices(<Numeral>) represents the number of vertices of the current
         * simulation.
         */
        case "vertices": {
            SimulationState.getInstance().setVerticesCount(percept.getParameters());
            Numeral verticesAmount = (Numeral) percept.getParameters().get(0);

            Graph.getInstance().setGlobalVerticesAmount(verticesAmount);
            break;
        }
        /**
         * probedVertex(<Identifier>,<Numeral>) denotes the value of a probed
         * vertex. The identifier is the vertex' name and the numeral is its
         * value.
         */
        case "probedVertex": {
            Identifier vertexID = (Identifier) percept.getParameters().get(0);
            Numeral value = (Numeral) percept.getParameters().get(1);

            Graph.getInstance().updateVertexValue(vertexID, value);
            break;
        }
        /**
         * visibleVertex(<Identifier>,<Identifier>) denotes a visible vertex,
         * represented by its name and the team that occupies it.
         */
        case "visibleVertex": {
            Identifier vertexID = (Identifier) percept.getParameters().get(0);
            Identifier teamID = (Identifier) percept.getParameters().get(1);

            Graph.getInstance().addVertex(vertexID, teamID);
            break;
        }
        /**
         * surveyedEdge(<Identifier>,<Identifier>,<Numeral>) indicates the
         * weight of a surveyed edge. The identifiers represent the adjacent
         * vertices and the numeral denotes the weight of the edge.
         */
        case "surveyedEdge": {
            Identifier vertexAID = (Identifier) percept.getParameters().get(0);
            Identifier vertexBID = (Identifier) percept.getParameters().get(1);
            Numeral weight = (Numeral) percept.getParameters().get(2);

            Graph.getInstance().addEdge(vertexAID, vertexBID, weight);
            break;
        }
        /**
         * visibleEdge(<Identifier>,<Identifier>) represents a visible edge,
         * denoted by its two adjacent vertices.
         */
        case "visibleEdge": {
            Identifier vertexA = (Identifier) percept.getParameters().get(0);
            Identifier vertexB = (Identifier) percept.getParameters().get(1);

            Graph.getInstance().addEdge(vertexA, vertexB);
            break;
        }
        /**
         * visibleEntity(<Identifier>,<Identifier>,<Identifier>,<Identifier>)
         * denotes a visible vehicle. The first identifier represents the
         * vehicle's name, the second one the vertex it is standing on, the
         * third its team and the fourth and final one indicates whether the
         * entity is disabled or not.
         */
        case "visibleEntity": {
            // TODO Where do we save this information?
            Identifier vehicleName = (Identifier) percept.getParameters().get(0);
            Identifier vertexName = (Identifier) percept.getParameters().get(1);
            Identifier teamName = (Identifier) percept.getParameters().get(2);
            Identifier isDisabled = (Identifier) percept.getParameters().get(3);

            // TODO find out whether teamName belongs to our or the other team
            Graph.getInstance().updateEnemyPosition(vehicleName, vertexName);
            break;
        }
        }
    }
}
