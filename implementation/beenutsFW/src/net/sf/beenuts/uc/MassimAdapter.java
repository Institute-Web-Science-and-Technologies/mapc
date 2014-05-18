package net.sf.beenuts.uc;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Vector;

import eis.EILoader;
import eis.EnvironmentInterfaceStandard;
import eis.exceptions.*;
import eis.iilang.Action;
import eis.iilang.Percept;

/**
 * Adapter to send actions to the MASSIM server and receive
 * perceptions for each agent.
 * 
 * @author dhoelzgen
 *
 */
public class MassimAdapter implements eis.AgentListener {

	private static EnvironmentInterfaceStandard environmentInterface;

	public HashMap<String, String> inspectedAgents = new HashMap<String, String>();
	public Vector<String> surveyedEdges = new Vector<String>();

	public HashMap<String, Boolean> seenEdges = new HashMap<String, Boolean>();
	public HashMap<String, Boolean> seenVertexes = new HashMap<String, Boolean>();

	public MassimAdapter() {
		try {
			String className = "massim.eismassim.EnvironmentInterface";
			environmentInterface = EILoader.fromClassName(className);
		} catch (IOException ex) {
			// TODO Add logging
			System.err.println("Error: Connection to MASSIM could not be established: " + ex.getMessage());
		}
	}

	public boolean open(String agentName, String connectionName) {
		try {
			environmentInterface.registerAgent(agentName);
			environmentInterface.associateEntity(agentName, connectionName);
			environmentInterface.attachAgentListener(agentName, this);
		} catch (AgentException ex) {
			// TODO Add logging
			System.err.println("Error: Agent could not be registered: " + ex.getMessage());
			return false;
		} catch (RelationException ex) {
			// TODO Add logging
			System.err.println("Error: Agent could not be connected: " + ex.getMessage());
		}

		return true;
	}

	public boolean start() {
		try {
			environmentInterface.start();
		} catch (ManagementException ex) {
			System.err.println("Error: Could not start environment interface: " + ex.getMessage());
			return false;
		}
		return true;
	}

	public boolean close(String agentName) {
		try {
			environmentInterface.unregisterAgent(agentName);
		} catch (AgentException ex) {
			// TODO Add logging
			System.err.println("Error: Agent could not be disconnected: " + ex.getMessage());
			return false;
		}

		environmentInterface = null;
		return true;
	}

	@Override
	public void handlePercept(String agentName, Percept percept) {
		// System.out.println("Percept for " + agentName + ": " + percept.toString());

		// Remember inspected agents and surveyed edges
		if (percept.getName().equals("inspectedEntity")) {
			this.inspectedAgents.put(percept.getParameters().get(0).toString(), percept.getParameters().toString());
		} else if (percept.getName().equals("surveyedEdge")) {
			this.surveyedEdges.add(percept.getParameters().get(0).toString() + percept.getParameters().get(1).toString());
			this.surveyedEdges.add(percept.getParameters().get(1).toString() + percept.getParameters().get(0).toString());
		} else if (percept.getName().equals("visibleEdge")) {
			this.seenEdges.put(percept.getParameters().get(0).toString() + percept.getParameters().get(1).toString(), true);
			this.seenEdges.put(percept.getParameters().get(1).toString() + percept.getParameters().get(0).toString(), true);
		} else if (percept.getName().equals("visibleVertex")) {
			this.seenVertexes.put(percept.getParameters().get(0).toString(), true);
		}
	}

	@Override
	public void handlePercepts(String agentName, Collection<Percept> percepts) {
		for (Percept percept : percepts) {
			this.handlePercept(agentName, percept);
		}
	}

	@Deprecated
	public Collection<Percept> perceive(String agentName) {
		try {
			Map<String, Collection<Percept>> percepts = environmentInterface.getAllPercepts(agentName);

			Collection<Percept> ret = new LinkedList<Percept>();
			for ( Collection<Percept> ps : percepts.values() ) {
				ret.addAll(ps);
			}

			// TODO Add special handling for restarted server (See example)

			return ret;

		} catch (PerceiveException ex) {
			// TODO Add logging
			System.err.println("Could not get perceptions: " + ex.getMessage());
		} catch (NoEnvironmentException ex) {
			// TODO Add logging
			System.err.println("Could not find environment: " + ex.getMessage());
		}

		return null;
	}

	public void act(String agentName, Action action) {
		try {
			environmentInterface.performAction(agentName, action);
		} catch (ActException ex) {
			// TODO Add logging
			System.err.println("Error: Could not perform action: " + action.toString());
		}
	}

}
