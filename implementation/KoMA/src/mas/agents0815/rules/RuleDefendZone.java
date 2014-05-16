package mas.agents0815.rules;

import java.util.Collection;
import java.util.LinkedList;

import mas.agents0815.Const;
import mas.agents0815.InternalAction;
import mas.agents0815.SubsumptionAgent;
import massim.javaagents.agents.MarsUtil;
import apltk.interpreter.data.LogicBelief;
import apltk.interpreter.data.LogicGoal;
import eis.iilang.Identifier;
import eis.iilang.Percept;

public class RuleDefendZone extends Rule {

	@Override
	public boolean fire(Collection<Percept> percepts,
			Collection<LogicBelief> beliefs, Collection<LogicGoal> goals,
			SubsumptionAgent agent) {
		
		//fires if there are enemy agent in our zone
		LinkedList<LogicBelief> en = agent.getAllBeliefs(Const.ENEMYINZONE);
		if  (!en.isEmpty()){

			 //do I have enough energy for an attack?
			 if (agent.getMyEnergy()<9){
				 setAction(MarsUtil.rechargeAction());
					return true;						
			 }else{
				setAction(new InternalAction(Const.PLANDEFENDROUTE, new Identifier(en.get(0).getParameters().get(0))));
				return true;
			 }
		}
		return false;
	}
}
