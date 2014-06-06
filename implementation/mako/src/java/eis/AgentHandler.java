package eis;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import eis.exceptions.AgentException;
import eis.exceptions.RelationException;
import eis.iilang.Identifier;
import eis.iilang.Numeral;
import eis.iilang.Parameter;
import eis.iilang.Percept;
import eis.iilang.TruthValue;
import graph.Graph;

/**
 * The AgentHandler class deals with several things regarding the team's agents.
 * It is a vital process that runs in the background of the simulation. It
 * initializes the agents and registers them with the server. It watches for
 * percepts from the server and handles those that aren't agent-specific, such
 * as the team's score. It also provides utility functions and interfaces for
 * communicating with Jason and the graph generation.
 * 
 * 
 * @author Artur Daudrich
 * @author Michael Sewell
 */
public class AgentHandler implements AgentListener {

    // Set up the AgentHandler's logger for debugging purposes.
    public static String NAME = AgentHandler.class.getName();
    AgentLogger logger = new AgentLogger(AgentHandler.NAME);

    private EnvironmentInterfaceStandard environmentInterface;
    private String configPath = "agentsConfig.xml";

    // keep track of our agents
    private HashMap<String, Agent> agents = new HashMap<String, Agent>();

    public AgentHandler(EnvironmentInterfaceStandard ei) {
        // logger
        logger.setVisible(false);

        // create agents
        environmentInterface = ei;
        createAgents();
    }

    /**
     * Create the agents according to the specification given in the agents' XML
     * configuration file. Created agents are added to the internal agents
     * HashMap.
     */
    private void createAgents() {
        File configFile = new File(configPath);

        // parse the XML document
        Document doc = null;
        try {
            DocumentBuilderFactory documentbuilderfactory = DocumentBuilderFactory.newInstance();
            doc = documentbuilderfactory.newDocumentBuilder().parse(configFile);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // get the root
        Element root = doc.getDocumentElement();

        // process root's children
        NodeList rootChildren = root.getChildNodes();
        for (int a = 0; a < rootChildren.getLength(); a++) {
            Node rootChild = rootChildren.item(a);

            // parse the agents list
            if (rootChild.getNodeName().equalsIgnoreCase("agents")) {

                NodeList rootChildChildren = rootChild.getChildNodes();
                for (int b = 0; b < rootChildChildren.getLength(); b++) {

                    // parse the entities list
                    Node rootChildChild = rootChildChildren.item(b);
                    if (rootChildChild.getNodeName().equalsIgnoreCase("agent")) {

                        Element e = (Element) rootChildChild;

                        String name = e.getAttribute("name");
                        String entity = e.getAttribute("entity");
                        String team = e.getAttribute("team");
                        String internalName = e.getAttribute("internalName");

                        // add to agents
                        Agent agent = new Agent();
                        agent.setName(name);
                        agent.setEntity(entity);
                        agent.setTeam(team);
                        agent.setInternalName(internalName);
                        agent.setEnvironmentInterface(environmentInterface);
                        agents.put(internalName, agent);
                        // agent.print();
                    }
                }
            }
        }
    }

    /**
     * This function is called when the AgentHandler is initialized. It is a
     * helper function that registers the agents with the server. It also sets
     * up the agents so that they are registered as listeners with the server
     * and will receive percepts related to them.
     * 
     * @param args
     */
    public void initAgents(String[] args) {
        for (Agent agent : agents.values()) {
            try {
                // tell server which agents are there
                environmentInterface.registerAgent(agent.getName());

                // tell server which agent is connected to which entity
                environmentInterface.associateEntity(agent.getName(), agent.getEntity());

                // listener for local percepts from the server
                environmentInterface.attachAgentListener(agent.getName(), agent);

                // listener for global percepts from the server
                environmentInterface.attachAgentListener(agent.getName(), this);
            } catch (AgentException e) {
                e.printStackTrace();
            } catch (RelationException e) {
                e.printStackTrace();
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see eis.AgentListener#handlePercept(java.lang.String,
     * eis.iilang.Percept)
     */
    public void handlePercept(String agentName, Percept percept) {
        updateSimulationState(percept);
    }

    /*
     * (non-Javadoc)
     * 
     * @see eis.AgentListener#handlePercept(java.lang.String,
     * java.util.Collection)
     */
    public void handlePercept(String agentName, Collection<Percept> percepts) {
        for (Percept percept : percepts) {
            updateSimulationState(percept);
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
        Parameter parameter = null;
        if (percept.getParameters().size() > 0) {
            parameter = percept.getParameters().getFirst();
        }
        switch (percept.getName()) {
        // step(<Numeral>) represents the current step of the current
        // simulation.
        case "step":
            SimulationState.getInstance().setStep((Numeral) parameter);
            break;
        // steps(<Numeral>) represents the overall number of steps of the
        // current simulation.
        case "steps":
            SimulationState.getInstance().setMaxSteps((Numeral) parameter);
            break;
        // timestamp(<Numeral>) represents the moment in time, when the last
        // message was sent by the server, again in Unix-time.
        case "timestamp":
            SimulationState.getInstance().setLastTimeStamp((Numeral) parameter);
            break;
        /*
         * deadline(<Numeral>) indicates the deadline for sending a valid
         * action-message to the server in Unix-time.
         */
        case "deadline":
            SimulationState.getInstance().setDeadline((Numeral) parameter);
            break;
        /* bye indicates that the tournament is over. */
        case "bye":
            SimulationState.getInstance().setIsTournamentOver(new TruthValue(true));
            break;
        // id(<Identifier>): indicates the identifier of the current simulation.
        case "id":
            SimulationState.getInstance().setId((Identifier) parameter);
            break;
        // lastStepScore(<Numeral>) indicates the score of the vehicle's team in
        // the last step of the current simulation.
        case "lastStepScore":
            SimulationState.getInstance().setLastStepScore((Numeral) parameter);
            break;
        // score(<Numeral>) represents is the overall score of the vehicle's
        // team.
        case "score":
            SimulationState.getInstance().setScore((Numeral) parameter);
            break;
        /* achievement(<Identifier>) denotes an achievement. */
        case "achievement":
            SimulationState.getInstance().addAchievement((Identifier) parameter);
            break;
        // money(<Numeral>) denotes the amount of money available to the
        // vehicle's team.
        case "money":
            SimulationState.getInstance().setMoney((Numeral) parameter);
            break;
        // ranking(<Numeral>) indicates the outcome of the simulation for the
        // vehicle's team, that is its ranking.
        case "ranking":
            SimulationState.getInstance().setRanking((Numeral) parameter);
            break;
        /*
         * edges(<Numeral>) represents the number of edges of the current
         * simulation.
         */
        case "edges":
            SimulationState.getInstance().setEdgeCount((Numeral) parameter);
            // TODO use this to construct our graph. This is the number of
            // edges in the currently running simulation.
            break;
        /*
         * vertices(<Numeral>) represents the number of vertices of the current
         * simulation.
         */
        case "vertices":
            SimulationState.getInstance().setVerticesCount((Numeral) parameter);
            // TODO use this to construct our graph. This is the number of
            // vertices in the currently running simulation.
            break;
        /*
         * probedVertex(<Identifier>,<Numeral>) denotes the value of a probed
         * vertex. The identifier is the vertex' name and the numeral is its
         * value.
         */
        case "probedVertex":
            // TODO implement this
            break;
        /*
         * visibleVertex(<Identifier>,<Identifier>) denotes a visible vertex,
         * represented by its name and the team that occupies it.
         */
        case "visibleVertex":
            Identifier vertexId = (Identifier) percept.getParameters().get(0);
            // Identifier teamId = (Identifier) percept.getParameters().get(1);
            Graph.getInstance().addVertex(vertexId);
            // Graph.getInstance().addVertex(vertexId, teamId);
            break;
        /*
         * surveyedEdge(<Identifier>,<Identifier>,<Numeral>) indicates the
         * weight of a surveyed edge. The identifiers represent the adjacent
         * vertices and the numeral denotes the weight of the edge.
         */
        case "surveyedEdge":
            break;
        /*
         * visibleEdge(<Identifier>,<Identifier>) represents a visible edge,
         * denoted by its two adjacent vertices.
         */
        case "visibleEdge":
            Identifier vertexA = (Identifier) percept.getParameters().get(0);
            Identifier vertexB = (Identifier) percept.getParameters().get(1);
            Graph.getInstance().addEdge(vertexA, vertexB);
            break;
        /*
         * visibleEntity(<Identifier>,<Identifier>,<Identifier>,<Identifier>)
         * denotes a visible vehicle. The first identifier represents the
         * vehicle's name, the second one the vertex it is standing on, the
         * third its team and the fourth and final one indicates whether the
         * entity is disabled or not.
         */
        case "visibleEntity":
            // TODO Where do we save this information?
            Identifier vehicleName = (Identifier) percept.getParameters().get(0);
            Identifier vertexName = (Identifier) percept.getParameters().get(1);
            Identifier teamName = (Identifier) percept.getParameters().get(2);
            Identifier isDisabled = (Identifier) percept.getParameters().get(3);
            break;
        }
    }

    /**
     * Helper function that retrieves an Agent object by its name.
     * 
     * @param agentName
     *            the name of the agent to be retrieved.
     * @return the Agent object with the given agentName.
     */
    public Agent getAgent(String agentName) {
        return agents.get(agentName);
    }
}
