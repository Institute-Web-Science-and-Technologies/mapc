package net.sf.beenuts.apr;

import net.sf.beenuts.ap.*;
import eis.iilang.Action;

/**
 * 
 * this class implements a Remote Simulation APR (RS-APR).
 * a remote simulation apr is used for agents running on agentProcessCluster 
 * nodes where a direct connection to an environment is not 
 * possible, or not wanted.
 * 
 * this class goes hand in hand with the LocalSimulationRelay
 * (LSR), and all perceptions and actions on RS-APR are
 * are transfered over the agentProcessCluster network to a LSR.  
 * 
 * @author Thomas Vengels
 *
 */
public class RemoteSimulationRelay extends APR {

	@Override
	public void sendAction(String agentName, Object action) {
		this.getAPC().sendAgentAction(agentName, ((Action)action).toProlog());
	}

	public void receivePerception(String dstAgent, String perception) {
		// look up destination agent
		AgentProcess ag = this.agentMap.get(dstAgent);
		if (ag != null)
			ag.perceive(perception);
	}
}
