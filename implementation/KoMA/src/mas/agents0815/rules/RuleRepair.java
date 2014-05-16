package mas.agents0815.rules;

import java.util.Collection;

import mas.agents0815.Const;
import mas.agents0815.SubsumptionAgent;
import massim.javaagents.agents.MarsUtil;
import apltk.interpreter.data.LogicBelief;
import apltk.interpreter.data.LogicGoal;
import eis.iilang.Percept;

public class RuleRepair extends Rule {

	@Override
	public boolean fire(Collection<Percept> percepts,
			Collection<LogicBelief> beliefs, Collection<LogicGoal> goals,
			SubsumptionAgent agent) {
		
		//fires if there is a disabled, friendly unit on my vertex
		
		for (Percept p : percepts) {
			if (p.getName().equals(Const.VISIBLEENTITY)
					&& p.getParameters().get(2).toString().equals(agent.getMyTeam())){
				 if (p.getParameters().get(1).toString().equals(agent.getMyPos()) && !p.getParameters().get(3).toString().equals("normal")) {
						
					 //do I have enough energy for repairing?
					 if (agent.getMyEnergy()<3){
						 setAction(MarsUtil.rechargeAction());
							return true;
					 }else{
						 String name=agent.getName().substring(5).toLowerCase();
						 if(!name.equals(p.getParameters().get(0).toString())){
							 setAction(MarsUtil.repairAction(p.getParameters().get(0).toString().toLowerCase()));
						 	 return true;
						 }//if
					 }//else
				}//if
			}

		}//for
		return false;
	}
}
