package mas.agents0815.rules;

import java.util.Collection;

import mas.agents0815.Const;
import mas.agents0815.SubsumptionAgent;
import massim.javaagents.agents.MarsUtil;
import apltk.interpreter.data.LogicBelief;
import apltk.interpreter.data.LogicGoal;
import eis.iilang.Percept;

public class RuleBuySentinelUpgrades extends Rule {

	@Override
	public boolean fire(Collection<Percept> percepts,
			Collection<LogicBelief> beliefs, Collection<LogicGoal> goals,
			SubsumptionAgent agent) {

		//fires if there is enough money
		//(attacker has the right on first buy)
		if(agent.getMoney()>=20){
			if (agent.getMyMaxEnergy() < 16) {
				setAction(MarsUtil.buyAction("battery"));
				return true;
			} else if (agent.getMyMaxHealth() < 5) {
				setAction(MarsUtil.buyAction("shield"));
				return true;
			} 
		}
		return false;
	}
}
