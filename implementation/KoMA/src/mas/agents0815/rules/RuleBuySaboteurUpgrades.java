package mas.agents0815.rules;

import java.util.Collection;

import mas.agents0815.Const;
import mas.agents0815.SubsumptionAgent;
import massim.javaagents.agents.MarsUtil;
import apltk.interpreter.data.LogicBelief;
import apltk.interpreter.data.LogicGoal;
import eis.iilang.Percept;

public class RuleBuySaboteurUpgrades extends Rule {

	@Override
	public boolean fire(Collection<Percept> percepts,
			Collection<LogicBelief> beliefs, Collection<LogicGoal> goals,
			SubsumptionAgent agent) {

		if(agent.getMoney()>=10){
			if (agent.getMyMaxEnergy() < 18) {
				setAction(MarsUtil.buyAction("battery"));
				return true;
			} else if (agent.getMyMaxHealth() < 4) {
				setAction(MarsUtil.buyAction("shield"));
				return true;
			} else if (agent.getMyStrength() < 5) {
				setAction(MarsUtil.buyAction("sabotageDevice"));
				return true;
			 }
		}
		return false;
	}
}
