package net.sf.beenuts.apr;

/**
 * 
 * this class provides a foundation for locally
 * simulations running on one agentProcessCluster node. a Local
 * Simulation Relay (LSR) can be used to implement
 * a simulation or environment connectivity on
 * one agentProcessCluster node, and use the agentProcessCluster network to interact
 * with non-local agents.
 * 
 * this class goes hand in hand with the RemoteSimulationRelay
 * (RSR), and all perceptions and actions are distributed
 * over the apr  network.
 *  
 * @author Thomas
 *
 */
public class LocalSimulationRelay extends APR {

	@Override
	public void sendAction(String agentName, Object action) {
		// a lsr will handle actions locally.

	}

	public void receiveAction(String srcAgent, String action) {
		//TODO: handle action form remote agent here
	}	 
}
