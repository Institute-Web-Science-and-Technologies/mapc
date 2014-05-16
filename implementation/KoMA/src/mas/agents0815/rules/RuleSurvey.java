package mas.agents0815.rules;

import java.util.Collection;
import java.util.LinkedList;

import mas.agents0815.Const;
import mas.agents0815.SubsumptionAgent;
import massim.javaagents.agents.MarsUtil;
import apltk.interpreter.data.LogicBelief;
import apltk.interpreter.data.LogicGoal;
import eis.iilang.Percept;

public class RuleSurvey extends Rule{

	public boolean fire(Collection<Percept> percepts, Collection<LogicBelief> beliefs,Collection<LogicGoal> goals, SubsumptionAgent agent){
		
		String vertex1="";
		String vertex2 ="";
		
		LinkedList<LogicBelief> inzone = agent.getAllBeliefs(Const.INZONE);
		if(inzone.size()>0|| agent.getRussianCounter()>16)
			return false;
		
		
			//check whether there is any nearby unexploredEdge
			for (LogicBelief b: beliefs){
				if (b.getPredicate().equals(Const.UNEXPLOREDEDGE)){
					vertex1= b.getParameters().get(0).toString();
					vertex2= b.getParameters().get(1).toString();
					if (vertex1.equals(agent.getMyPos())|| vertex2.equals(agent.getMyPos())){
						//found an unexplored edge
						setAction(MarsUtil.surveyAction());
						return true;
					}
				}	
			}
		
		 
		//no nearby unexploredEdge found
		return false;
			
	
		}
}
