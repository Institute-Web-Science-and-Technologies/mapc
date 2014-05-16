package mas.agentsTargaryen.strategy;

import mas.agentsTargaryen.TargaryenAgent;
import massim.javaagents.agents.MarsUtil;
import eis.iilang.Action;

/**
 * Im Zweifelsfall: skip. Nein, warte... RECHARGE!
 */
public class SkipStrategy implements StrategyIf {

	@Override
	public Action execute(TargaryenAgent agent) {
		if (agent.getEnergy() < agent.getMaxEnergy()) {
			return MarsUtil.rechargeAction();
		}
		return MarsUtil.skipAction();
	}
}