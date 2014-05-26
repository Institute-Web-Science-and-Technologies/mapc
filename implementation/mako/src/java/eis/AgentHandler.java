package eis;

import java.io.File;
import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import eis.exceptions.AgentException;
import eis.exceptions.RelationException;

public class AgentHandler {
	private EnvironmentInterfaceStandard environmentInterface;
	private String configPath = "agentsConfig.xml";
	private LinkedList<Agent> agents = new LinkedList<Agent>();

	public AgentHandler(EnvironmentInterfaceStandard ei) {
		environmentInterface = ei;
		createAgents();
	}

	private void createAgents() {
		File configFile = new File(configPath);

		// parse the XML document
		Document doc = null;
		try {
			DocumentBuilderFactory documentbuilderfactory = DocumentBuilderFactory
					.newInstance();
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

					// parse the entites list
					Node rootChildChild = rootChildChildren.item(b);
					if (rootChildChild.getNodeName().equalsIgnoreCase("agent")) {

						Element e = (Element) rootChildChild;

						String name = e.getAttribute("name");
						String entity = e.getAttribute("entity");
						String team = e.getAttribute("team");
						String type = e.getAttribute("class");

						// add to agents
						Agent agent = new Agent();
						agent.setName(name);
						agent.setEntity(entity);
						agent.setTeam(team);
						agent.setType(type);
						agents.add(agent);
						agent.print();
					}
				}
			}
		}
	}

	public void initAgents(String[] args) {
		for (Agent agent: agents) {
			try {
				environmentInterface.registerAgent(agent.getName());
				environmentInterface.associateEntity(agent.getName(),agent.getEntity());
			} catch (AgentException e) {
				e.printStackTrace();
			} catch (RelationException e) {
				e.printStackTrace();
			}
		}
	}
}
