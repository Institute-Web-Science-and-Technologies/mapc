package mas.agentsTargaryen.strategy;

import mas.agentsTargaryen.TargaryenAgent;
import massim.javaagents.agents.MarsUtil;
import eis.iilang.Action;

/**
 * Energie nachladen bei Energiemangel
 */
public class RechargeStrategy implements StrategyIf {

	@Override
	public Action execute(TargaryenAgent agent) {
		if (agent.getEnergy() < 10) {
			return MarsUtil.rechargeAction();
		}
		return null;
	}
}