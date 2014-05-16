package mas.agents0815.rules;

import java.util.Collection;

import mas.agents0815.Const;
import mas.agents0815.SubsumptionAgent;
import massim.javaagents.agents.MarsUtil;
import apltk.interpreter.data.LogicBelief;
import apltk.interpreter.data.LogicGoal;
import eis.iilang.Percept;

public class RulePrimitiveAttack extends Rule {

	@Override
	public boolean fire(Collection<Percept> percepts,
			Collection<LogicBelief> beliefs, Collection<LogicGoal> goals,
			SubsumptionAgent agent) {
		
		//fires if there is an enabled enemy agent statnding on my position
		for (Percept p : percepts) {
			if (p.getName().equals(Const.VISIBLEENTITY)
					&& !p.getParameters().get(2).toString().equals(agent.getMyTeam())){
				 if (p.getParameters().get(1).toString().equals(agent.getMyPos()) && p.getParameters().get(3).toString().equals("normal")) {
					
					 //do I have enough energy for an attack?
					 if (agent.getMyEnergy()<2){
						 setAction(MarsUtil.rechargeAction());
							return true;						
					 }
					 else{
					 setAction(MarsUtil.attackAction(p.getParameters().get(0).toString().toLowerCase()));
						return true;
					}//else
				}
			}

		}//for
		return false;
	}
}
