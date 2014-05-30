package eis;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Logger;

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

public class AgentHandler implements AgentListener{
	private static final Logger logger = Logger.getLogger(AgentHandler.class.getCanonicalName());
	private EnvironmentInterfaceStandard environmentInterface;
	private String configPath = "agentsConfig.xml";
	private HashMap<String, Agent> agents = new HashMap<String, Agent>();

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
						agent.setEnvironmentInterface(environmentInterface);
						agents.put(name, agent);
						//agent.print();
					}
				}
			}
		}
	}

	public void initAgents(String[] args) {
		for (Agent agent: agents.values()) {
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

	public void handlePercept(String agentName, Percept percept) {
		updateSimulationState(percept);		
	}

	public void handlePercept(String agentName, Collection<Percept> percepts) {
		for (Percept percept : percepts) {
			updateSimulationState(percept);
		}
	}

	private void updateSimulationState(Percept percept) {
		Parameter parameter = null;
		if (percept.getParameters().size() > 0) { 
		  parameter = percept.getParameters().getFirst();
		}
		switch (percept.getName())
		{
			case "step": SimulationState.setStep((Numeral)parameter);
			break;
			case "steps": SimulationState.setMaxSteps((Numeral) parameter);
			break;
			case "timestamp": SimulationState.setLastTimeStamp((Numeral) parameter);
			break;
			case "deadline": SimulationState.setDeadline((Numeral) parameter);
			break;
			case "bye": SimulationState.setIsTournamentOver(new TruthValue(true));
			break;
			case "edges": SimulationState.setEdgeCount((Numeral) parameter);
			break;
			case "vertices": SimulationState.setVerticesCount((Numeral) parameter);
			break;
			case "id": SimulationState.setId((Identifier) parameter);
			break;
			case "lastStepScore": SimulationState.setLastStepScore((Numeral) parameter);
			break;
			case "score": SimulationState.setScore((Numeral) parameter);
			break;
			case "achievement": SimulationState.addAchievement((Identifier) parameter);
			break;
			case "money": SimulationState.setMoney((Numeral) parameter);
			break;
			case "ranking": SimulationState.setRanking((Numeral) parameter);
			break;
		}
		
	}

	public Agent getAgent(String agentName)	{
		return agents.get(agentName);
	}
}
