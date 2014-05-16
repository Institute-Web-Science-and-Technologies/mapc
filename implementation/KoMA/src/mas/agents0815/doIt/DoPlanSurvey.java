package mas.agents0815.doIt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import eis.iilang.Action;

import mas.agents0815.Const;
import mas.agents0815.HelpFunctions;
import mas.agents0815.SubsumptionAgent;
import massim.javaagents.agents.MarsUtil;
import apltk.interpreter.data.LogicBelief;
import apltk.interpreter.data.LogicGoal;

public class DoPlanSurvey {
	
	Collection<LogicBelief> beliefs;
	Collection<LogicGoal> goals;

	private final HelpFunctions help = new HelpFunctions();
	
	public DoPlanSurvey(Collection<LogicBelief> beliefs,
			Collection<LogicGoal> goals) {
		super();
		this.beliefs = beliefs;
		this.goals = goals;
	}

	/*********************************************************
	 * @brief Find an unexplored edge, go there and survey
	 * @param current
	 *            beliefs and goals
	 * @return goto Action to the node from which best to perform survey
	 * @see processAction 
	 ****************************************/
	public Action doIt(SubsumptionAgent agent) {
		
		//1.) Idea: among all nodes linked to an unexplored edge find the one with the
		// smallest amount of connections to the graph
		//(in the hope that this is unknown territory)
		//2.) Idea: among all nodes linked to an unexplored edge find the one
		// with the largest amount of neighboring unexplored edges
		LinkedList<String> nodesUnexplored = new LinkedList<String>();
		LinkedList<String> nodesExplored = new LinkedList<String>();
		LinkedList<String> candidates = new LinkedList<String>();
		ArrayList<LogicGoal> newGoals = new ArrayList<LogicGoal>();
		
		//variables for topology
		LinkedList<String> vertices = new LinkedList<String>();
		LinkedList<LogicBelief> edges= new LinkedList<LogicBelief>();
		
		for (LogicBelief b: beliefs){
			if (b.getPredicate().equals(Const.UNEXPLOREDEDGE)){
				nodesUnexplored.add(b.getParameters().get(0));
				nodesUnexplored.add(b.getParameters().get(1));
				edges.add(b);
			}
			if (b.getPredicate().equals(Const.EXPLOREDEDGE)){
				nodesExplored.add(b.getParameters().get(0));
				nodesExplored.add(b.getParameters().get(1));
				edges.add(b);
			}
			if (b.getPredicate().equals(Const.PROBEDVERTEX)){
				vertices.add(b.getParameters().get(0));
			}
				
			if (b.getPredicate().equals(Const.UNPROBEDVERTEX)){
				vertices.add(b.getParameters().get(0));
			}
				
				
		}//for
		
		//use only those nodes not connected to the explored graph
		for (String nodeUn: nodesUnexplored){
			if (!nodesExplored.contains(nodeUn)){
				if (!agent.containsBelief(new LogicBelief(Const.BLOCK, Const.SURVEY, nodeUn)))
						candidates.add(nodeUn);
			}
				
		}//for
		
		//candidates may be empty for some reason
		//in this case, add any node, connected no an unexplored edge
		if (candidates.isEmpty()){
			for(int i = 0; i<nodesUnexplored.size(); i++){
				if (!agent.containsBelief(new LogicBelief(Const.BLOCK, Const.SURVEY, nodesUnexplored.get(i))))
					candidates.add(nodesUnexplored.get(i));
			}
		}
		
		//do I have to move to do my survey?
		if (candidates.contains(agent.getMyPos()))
			return MarsUtil.surveyAction();
		
		//if yes, find the route to the nearest vertex
		newGoals = help.findRoute(agent.getMyPos(), candidates, vertices, edges, true,200);
		
		// fill goals
		agent.addGoal(new LogicGoal(Const.SURVEY, "1"));
		String dest= "";
		// fill goals
		int priority=2;
		for (int i = 0; i < newGoals.size() ; i++) {
			dest = newGoals.get(i).getParameters().firstElement()
					.toString();
			agent.addGoal(new LogicGoal(Const.GOTO, String.valueOf(priority),dest));
			priority++;
		}

		// return first step of the route
		dest = newGoals.get(newGoals.size() - 1).getParameters()
				.firstElement().toString();
		
		//I go there, u do not follow me!
			agent.broadcastBelief(new LogicBelief(Const.BLOCK, Const.SURVEY, newGoals.get(0).getParameters().firstElement()
				.toString()));
		
		return MarsUtil.gotoAction(dest);
		
	}//doIt

}//class
