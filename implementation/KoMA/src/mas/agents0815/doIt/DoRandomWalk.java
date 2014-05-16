package mas.agents0815.doIt;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import mas.agents0815.Const;
import mas.agents0815.SubsumptionAgent;
import massim.javaagents.agents.MarsUtil;
import apltk.interpreter.data.LogicBelief;
import apltk.interpreter.data.LogicGoal;
import eis.iilang.Action;


public class DoRandomWalk {
	
	Collection<LogicBelief> beliefs;
	Collection<LogicGoal> goals;
	
	/*********************************************************
	@brief finds all current neighbors and randomly chooses one to go to
	@param current beliefs and goals
	@return goto Action to a randomly chosen neighbor
	@see processAction
	*****************z****************************************/
	public DoRandomWalk(Collection<LogicBelief> beliefs, Collection<LogicGoal> goals){
		this.beliefs= beliefs;
		this.goals = goals;
	}
	
	public Action doIt(SubsumptionAgent agent){
		String position = agent.getMyPos();
		
		//TODO can this happen?
		if(position == null){ MarsUtil.skipAction(); }
				
		//get all neighbors ..
		LinkedList<LogicBelief> neighbors = agent.getAllBeliefs(Const.NEIGHBOR);
		
		
		//... and choose one randomly
		if (neighbors.isEmpty()){
			System.out.println("I have no neighbors");
			return MarsUtil.skipAction();
		}
		Collections.shuffle(neighbors);
		return(MarsUtil.gotoAction(neighbors.getFirst().getParameters().get(0)));
	}//doRandomWalk

}//class
