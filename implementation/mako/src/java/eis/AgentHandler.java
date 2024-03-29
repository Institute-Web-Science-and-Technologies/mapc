package eis;

import java.io.File;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
public class AgentHandler {

    private static String configPath = "agentsConfig.xml";

    public static String selectedTeam = "MAKo";

    // public static String selectedTeam = "MAKo_b";

    /**
     * Create the agents according to the specification given in the agents' XML
     * configuration file. Created agents are added to the internal agents
     * HashMap.
     */
    public static HashMap<String, Agent> createAgents() {
        HashMap<String, Agent> agents = new HashMap<String, Agent>();
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

                        String serverNameNonEscaped = e.getAttribute("serverName");
                        String serverNameEscaped = e.getAttribute("serverName").toLowerCase().replace("-", "_");
                        String entity = e.getAttribute("entity");
                        String team = e.getAttribute("team");
                        String jasonName = e.getAttribute("jasonName");
                        String role = jasonName.split("\\d")[0];

                        // add to agents
                        Agent agent = new Agent();
                        agent.setServerName(serverNameEscaped);
                        MapAgent.getInstance();
                        MapAgent.agentNameConversionMap.put(serverNameEscaped, serverNameNonEscaped);
                        agent.setServerNameNonEscaped(serverNameNonEscaped);
                        agent.setEntity(entity);
                        agent.setTeam(team);
                        agent.setJasonName(jasonName);
                        agent.setRole(role);
                        MapAgent.getInstance().addAgent(serverNameEscaped, agent);
                        agents.put(serverNameEscaped, agent);
                    }
                }
            }
        }
        return agents;
    }
}
