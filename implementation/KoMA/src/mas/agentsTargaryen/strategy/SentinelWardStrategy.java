package mas.agentsTargaryen.strategy;

import mas.agentsTargaryen.TargaryenAgent;
import massim.javaagents.agents.MarsUtil;
import apltk.interpreter.data.LogicBelief;
import apltk.interpreter.data.LogicGoal;
import eis.iilang.Action;

/**
 * Sentinel: Agent festlegen, der Ward spielt
 */
public class SentinelWardStrategy implements StrategyIf {

	@Override
	public Action execute(TargaryenAgent agent) {
		if (!agent.getRole().equals("Sentinel")) {
			return null;
		}
		if (agent.isDisabled()) {
			return null;
		}
		if (agent.getAllBeliefs("wardAgent").isEmpty()) {
			// Ward-Agent festlegen
			LogicBelief wardBelief = new LogicBelief("wardAgent", agent.getName());
			agent.addBelief(wardBelief);
			agent.broadcastBelief(wardBelief);
			agent.addGoal(new LogicGoal("wantVisRange", "15"));
		} else if (agent.getAllBeliefs("wardAgent").getFirst().getParameters().get(0).equals(agent.getName())) {
			if (!agent.getAllGoals("wantVisRange").isEmpty()) {
				int wantVisRange = Integer.parseInt(agent.getAllGoals("wantVisRange").getFirst().getParameters().get(0));
				// Wenn genug visRange: Ziel erreicht
				if (agent.getVisRange() >= wantVisRange) {
					agent.deleteGoals("wantVisRange");
					agent.broadcastBelief(new LogicBelief("wardDone"));
					return null;
				}
				// Wenn genug Geld: Sensor kaufen
				if (agent.getMoney() >= 2) {
					return MarsUtil.buyAction("sensor");
				}
			}
		}
		return null;
	}
}