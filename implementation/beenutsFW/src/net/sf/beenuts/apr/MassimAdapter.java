package net.sf.beenuts.apr;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

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
	private MassimRelay massimRelay;
	
	public MassimAdapter(MassimRelay massimRelay) {
		this.massimRelay = massimRelay;
		
		try {
			String className = "massim.eismassim.EnvironmentInterface";
			environmentInterface = EILoader.fromClassName(className);
		} catch (IOException ex) {
			// TODO Add logging
			System.out.println("Connection to MASSIM could not be established: " + ex.getMessage());
		}
	}
	
	public boolean open(String agentName, String connectionName) {		
		try {
			environmentInterface.registerAgent(agentName);
			environmentInterface.associateEntity(agentName, connectionName);
			environmentInterface.attachAgentListener(agentName, this);
			environmentInterface.start();
		} catch (AgentException ex) {
			// TODO Add logging
			System.out.println("Agent could not be registered: " + ex.getMessage());
			return false;
		} catch (RelationException ex) {
			// TODO Add logging
			System.out.println("Agent could not be connected: " + ex.getMessage());
		} catch (ManagementException ex) {
			System.out.println("Environment could not be started: " + ex.getMessage());
		}
		
		return true;
	}
	
	public boolean close(String agentName) {
		try {
			environmentInterface.unregisterAgent(agentName);
		} catch (AgentException ex) {
			// TODO Add logging
			System.out.println("Agent could not be disconnected: " + ex.getMessage());
			return false;
		}
		
		environmentInterface = null;
		return true;
	}
	
	@Override
	public void handlePercept(String agentName, Percept percept) {
		this.massimRelay.agentMap.get(agentName).perceive(percept);
	}
	
	@Override
	public void handlePercepts(String agentName, Collection<Percept> percepts) {
		
		/*
		// TODO: Forward all percepts at once
		for (Percept percept : percepts) {
			this.handlePercept(agentName, percept);
		}
		*/
		this.massimRelay.agentMap.get(agentName).perceive(
				new MassimPerceptionChunk(percepts));
		
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
			System.out.println("Could not get perceptions: " + ex.getMessage());
		} catch (NoEnvironmentException ex) {
			// TODO Add logging
			System.out.println("Could not find environment: " + ex.getMessage());
		}
		
		return null;
	}
	
	public void act(String agentName, Object action) {
		try {
			environmentInterface.performAction(agentName, (Action)action);
		} catch (ActException ex) {
			// TODO Add logging
			System.out.println("Could not perform action: " + ex.getMessage());
		}
	}

}
