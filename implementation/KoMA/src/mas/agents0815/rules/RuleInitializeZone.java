package mas.agents0815.rules;

import java.util.Collection;

import mas.agents0815.Const;
import mas.agents0815.InternalAction;
import mas.agents0815.SubsumptionAgent;

import apltk.interpreter.data.LogicBelief;
import apltk.interpreter.data.LogicGoal;
import eis.iilang.Percept;

public class RuleInitializeZone extends Rule {

	@Override
	public boolean fire(Collection<Percept> percepts,
			Collection<LogicBelief> beliefs, Collection<LogicGoal> goals, SubsumptionAgent agent) {
		
		//fires if there is no zone yet and if the game lasts more
		//than 20 steps
		
		boolean zoneExist =false;
		
		for(LogicBelief b : beliefs){			
			if(b.getPredicate().equals(Const.ZONE)){
				zoneExist =true;
			}
		}
		if(!zoneExist && agent.getRussianCounter()>=agent.getZoneStart()){
			this.setAction(new InternalAction(Const.INTIALIZEZONE));
            return true;
		}
		else		
			return false;
	}

}
