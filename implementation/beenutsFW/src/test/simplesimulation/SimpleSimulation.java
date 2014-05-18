package test.simplesimulation;

import java.util.HashSet;
import java.util.Set;

import net.sf.beenuts.ap.*;
import net.sf.beenuts.apr.LocalSimulationRelay;

public class SimpleSimulation extends LocalSimulationRelay {

	protected int step = 0;
	
	Set<String> simulationAgents = new HashSet<String>();
		
	public void nextStep() {
		
		String per = "simulation step: " + step;
		++step;		
		System.out.println(per);
		
		// send per to all agents
		for (String ag : this.getAPC().getAgents()) {
			// local agents have an apcagent entry, remote not
			AgentProcess apcag = this.agentMap.get(ag);
			if (apcag != null) {
				apcag.perceive(per);
			} else {
				this.getAPC().sendAgentPerception(ag, per);
			}
		}
	}
}
