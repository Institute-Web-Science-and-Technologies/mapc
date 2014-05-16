package mas.agents0815.rules;

import java.util.Collection;
import java.util.LinkedList;

import mas.agents0815.Const;
import mas.agents0815.InternalAction;
import mas.agents0815.SubsumptionAgent;
import massim.javaagents.agents.MarsUtil;
import apltk.interpreter.data.LogicBelief;
import apltk.interpreter.data.LogicGoal;
import eis.iilang.Percept;

public class RulePlanSurvey extends Rule{
	
	public boolean fire(Collection<Percept> percepts, Collection<LogicBelief> beliefs,Collection<LogicGoal> goals, SubsumptionAgent agent){
		
		//fires if game lasts less than 20 steps and if there is no zone
		
		LinkedList<LogicBelief> inzone = agent.getAllBeliefs(Const.INZONE);
		if(agent.getRussianCounter()>=agent.getZoneStart() || inzone.size()>0){
			return false;
		}
		
		
		if(agent.getRussianCounter() <=1){
			setAction(MarsUtil.skipAction());
			return true;
		}
		//check whether there is any  uneploredEdge
		for (LogicBelief b: beliefs){
			if (b.getPredicate().equals(Const.UNEXPLOREDEDGE)){
				//check whether no other agents already go there to survey
				if (!agent.containsBelief(new LogicBelief(Const.BLOCK,Const.SURVEY,b.getParameters().get(0)))){
					//found an unexplored edge
				    setAction(new InternalAction (Const.PLANSURVEY));
					return true;
				}
			}//if	
		}//for
		 
		//no nearby unexploredEdge found
		return false;
			
	
		}

}
