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

    /**
     * Because AgentSpeak treats any string that starts with an upper case
     * letter as a variable, we have to make sure to convert agent and team
     * names from mixed case to lower case before we send them to AgentSpeak,
     * and then convert them back to mixed case in the case where we have to
     * send an action to the server that contains an agent name.
     */
    public static HashMap<String, String> agentNameConversionMap = new HashMap<String, String>();

    // public static String enemyTeam = "teamB";

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

                        String serverNameMixedCase = e.getAttribute("serverName");
                        String serverNameLowerCase = e.getAttribute("serverName").toLowerCase();
                        String entity = e.getAttribute("entity");
                        String team = e.getAttribute("team");
                        String jasonName = e.getAttribute("jasonName");

                        // add to agents
                        Agent agent = new Agent();
                        agent.setServerName(serverNameLowerCase);
                        agentNameConversionMap.put(serverNameLowerCase, serverNameMixedCase);
                        agent.setServerNameMixedCase(serverNameMixedCase);
                        agent.setEntity(entity);
                        agent.setTeam(team);
                        agent.setJasonName(jasonName);
                        MapAgent.getInstance().addAgent(serverNameLowerCase, agent);
                        agents.put(serverNameLowerCase, agent);
                    }
                }
            }
        }
        return agents;
    }
}
