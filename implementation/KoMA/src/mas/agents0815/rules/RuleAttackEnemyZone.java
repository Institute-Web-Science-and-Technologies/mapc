package mas.agents0815.rules;

import java.util.Collection;
import java.util.LinkedList;

import mas.agents0815.Const;
import mas.agents0815.InternalAction;
import mas.agents0815.SubsumptionAgent;
import massim.javaagents.agents.MarsUtil;
import apltk.interpreter.data.LogicBelief;
import apltk.interpreter.data.LogicGoal;
import eis.iilang.Action;
import eis.iilang.Percept;

public class RuleAttackEnemyZone extends Rule{

	public boolean fire(Collection<Percept> percepts, Collection<LogicBelief> beliefs,Collection<LogicGoal> goals, SubsumptionAgent agent){
		
		//fires always
		
		setAction(new InternalAction(Const.ATTACKENEMYZONE));
		return true;
			
	
		}
}
