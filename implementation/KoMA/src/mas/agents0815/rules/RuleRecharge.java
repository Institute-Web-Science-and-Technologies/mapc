package mas.agents0815.rules;

import java.util.Collection;

import mas.agents0815.Const;
import mas.agents0815.SubsumptionAgent;
import massim.javaagents.agents.MarsUtil;

import apltk.interpreter.data.LogicBelief;
import apltk.interpreter.data.LogicGoal;

import eis.iilang.Percept;

public class RuleRecharge extends Rule {

	@Override
	public boolean fire(Collection<Percept> percepts,
			Collection<LogicBelief> beliefs, Collection<LogicGoal> goals, SubsumptionAgent agent) {


		//TODO we still have to find a good recharge trigger
		if (agent.getMyEnergy() < (agent.getMyMaxEnergy() - agent.getMyMaxEnergy()/4)) {
			setAction(MarsUtil.rechargeAction());
			return true;
		}

		return false;

	}

}
