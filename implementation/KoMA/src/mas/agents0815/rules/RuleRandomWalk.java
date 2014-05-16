package mas.agents0815.rules;

import java.util.Collection;
import java.util.LinkedList;


import mas.agents0815.Const;
import mas.agents0815.InternalAction;
import mas.agents0815.SubsumptionAgent;

import apltk.interpreter.data.LogicBelief;
import apltk.interpreter.data.LogicGoal;

import eis.iilang.Percept;

public class RuleRandomWalk extends Rule{

	
	@Override
	public boolean fire(Collection<Percept> percepts, Collection<LogicBelief> beliefs,Collection<LogicGoal> goals, SubsumptionAgent agent){
		
   
		//fires, if the agent is not standing in a zone
		LinkedList<LogicBelief> inZone = agent.getAllBeliefs(Const.INZONE);
		
		if (!(inZone.size()>0)){
			setAction(new InternalAction(Const.RANDOMWALK));
			return true;
		}
		return false;
	}


}
