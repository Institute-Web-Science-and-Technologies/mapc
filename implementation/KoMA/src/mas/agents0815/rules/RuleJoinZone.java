package mas.agents0815.rules;

import java.util.Collection;

import mas.agents0815.Const;
import mas.agents0815.InternalAction;
import mas.agents0815.SubsumptionAgent;
import massim.javaagents.agents.MarsUtil;

import apltk.interpreter.data.LogicBelief;
import apltk.interpreter.data.LogicGoal;
import eis.iilang.Percept;

public class RuleJoinZone extends Rule {

	@Override
	public boolean fire(Collection<Percept> percepts,
			Collection<LogicBelief> beliefs, Collection<LogicGoal> goals, SubsumptionAgent agent) {
		
		boolean inZone =false;
		boolean zoneExist=false;
		for(LogicBelief b : beliefs){
			
			if(b.getPredicate().equals(Const.INZONE))
				inZone=true;
			if(b.getPredicate().equals(Const.FREENODESOFZONE)){
				
				if(!b.getParameters().isEmpty()){
				    zoneExist =true;
				}//if
			}//if
		}
		
		if (!inZone && zoneExist){
			 //do I have enough energy ?
			 if (agent.getMyEnergy()<9){
				 setAction(MarsUtil.rechargeAction());
					return true;						
			 }else{
				 this.setAction(new InternalAction(Const.JOINZONE));
				 return true;
			 }
		}
		else 
			return false;
	}

}
