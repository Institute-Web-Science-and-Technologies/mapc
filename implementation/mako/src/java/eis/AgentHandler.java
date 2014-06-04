/*
 * @author Artur Daudrich
 * @author Michael Sewell
 */
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

public class AgentHandler implements AgentListener {

	public static String NAME = AgentHandler.class.getName();
	AgentLogger logger = new AgentLogger(AgentHandler.NAME);
	private EnvironmentInterfaceStandard environmentInterface;
	private String configPath = "agentsConfig.xml";
	private HashMap<String, Agent> agents = new HashMap<String, Agent>();

	public AgentHandler(EnvironmentInterfaceStandard ei) {
		// logger
		logger.setVisible(false);

		// create agents
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

	public void initAgents(String[] args) {
		for (Agent agent : agents.values()) {
			try {
				// tell server which agents are there
				environmentInterface.registerAgent(agent.getName());

				// tell server which agent is connected to which entity
				environmentInterface.associateEntity(agent.getName(),
						agent.getEntity());

				// listener for local percepts from the server
				environmentInterface
						.attachAgentListener(agent.getName(), agent);

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
		switch (percept.getName()) {
		case "step":
			SimulationState.getInstance().setStep((Numeral) parameter);
			break;
		case "steps":
			SimulationState.getInstance().setMaxSteps((Numeral) parameter);
			break;
		case "timestamp":
			SimulationState.getInstance().setLastTimeStamp((Numeral) parameter);
			break;
		case "deadline":
			SimulationState.getInstance().setDeadline((Numeral) parameter);
			break;
		case "bye":
			SimulationState.getInstance().setIsTournamentOver(
					new TruthValue(true));
			break;
		case "id":
			SimulationState.getInstance().setId((Identifier) parameter);
			break;
		case "lastStepScore":
			SimulationState.getInstance().setLastStepScore((Numeral) parameter);
			break;
		case "score":
			SimulationState.getInstance().setScore((Numeral) parameter);
			break;
		case "achievement":
			SimulationState.getInstance()
					.addAchievement((Identifier) parameter);
			break;
		case "money":
			SimulationState.getInstance().setMoney((Numeral) parameter);
			break;
		case "ranking":
			SimulationState.getInstance().setRanking((Numeral) parameter);
			break;
		case "visibleVertex":
			/*
			 * visibleVertex(<Identifier>,<Identifier>) denotes a visible
			 * vertex, represented by its name and the team that occupies it.
			 */
			// TODO send this information to our central map
			break;
		case "visibleEdge":
			/*
			 * visibleEdge(<Identifier>,<Identifier>) represents a visible edge,
			 * denoted by its two adjacent vertices.
			 */
			// TODO send this information to our central map
			break;
		case "visibleEntity":
			/*
			 * visibleEntity(<Identifier>,<Identifier>,<Identifier>,<Identifier>)
			 * denotes a visible vehicle. The first identifier represents the
			 * vehicle���s name, the second one the vertex it is standing on,
			 * the third its team and the fourth and final one indicates whether
			 * the entity is disabled or not.
			 */
			// TODO send this information to our central map
			break;
		case "position":
			/*
			 * position(<Identifier>) indicates the current position of the
			 * vehicle. The identifier is the vertex���s name.
			 */
			// TODO send this information to our central map
			break;
		case "edges":
			SimulationState.getInstance().setEdgeCount((Numeral) parameter);
			// TODO use this to construct our graph. This is the number of
			// edges in the currently running simulation.
			break;
		case "vertices":
			SimulationState.getInstance().setVerticesCount((Numeral) parameter);
			// TODO use this to construct our graph. This is the number of
			// vertices in the currently running simulation.
			break;
		}
	}

	public Agent getAgent(String agentName) {
		return agents.get(agentName);
	}
}
