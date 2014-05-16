package mas.agents0815.rules;

import java.util.Collection;
import java.util.LinkedList;

import mas.agents0815.Const;
import mas.agents0815.InternalAction;
import mas.agents0815.SubsumptionAgent;

import apltk.interpreter.data.LogicBelief;
import apltk.interpreter.data.LogicGoal;
import eis.iilang.Percept;

public class RuleGotoNearestUnprobedVertex extends Rule{

	@Override
	public boolean fire(Collection<Percept> percepts, Collection<LogicBelief> beliefs,Collection<LogicGoal> goals, SubsumptionAgent agent){
		
		//fires only, if the agent is not in a zone, and the game is no within the first
		//X rounds
		LinkedList<LogicBelief> inzone = agent.getAllBeliefs(Const.INZONE);
		if(inzone.size()>0|| agent.getRussianCounter()>=agent.getZoneStart())
			return false;
		
       for(LogicBelief b: beliefs){
    	  if ( b.getPredicate().equals(Const.UNPROBEDVERTEX)){
              if(!beliefs.contains(new LogicBelief(Const.BLOCK, Const.PROBE,b.getParameters().get(0)))){
            	  setAction(new InternalAction(Const.GOTONEARESTUNPROBEDVERTEX));
    			  return true;
              }
    	   }
       }
		
       //Agents do not know any unprobed vertices
		return false;
	}
}
