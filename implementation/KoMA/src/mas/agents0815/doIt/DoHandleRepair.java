package mas.agents0815.doIt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import eis.iilang.Action;
import eis.iilang.Identifier;

import mas.agents0815.Const;
import mas.agents0815.HelpFunctions;
import mas.agents0815.SubsumptionAgent;
import massim.javaagents.agents.MarsUtil;
import apltk.interpreter.data.LogicBelief;
import apltk.interpreter.data.LogicGoal;

public class DoHandleRepair {

	Collection<LogicBelief> beliefs;
	Collection<LogicGoal> goals;
	private SubsumptionAgent agent;
	String target=null;
	
	public DoHandleRepair(Collection<LogicBelief> b, Collection<LogicGoal> g, SubsumptionAgent agent, String nameTarget) {
		super();
		this.beliefs = b;
		this.goals = g;
		this.agent = agent;
		this.target = nameTarget;
	}
	
	public Action doIt() {
		
		LinkedList<LogicBelief> edges = new LinkedList<LogicBelief>();
		LinkedList<String> destination = new LinkedList<String>();
		LinkedList<String> vertices = new LinkedList<String>();
		String position = "";
		for (LogicBelief l : beliefs) {
			// get current position
			if (l.getPredicate().equals(Const.POSITION))
				position = l.getParameters().get(0).toString();
			
			if (l.getPredicate().equals(Const.ALLIEDPOSITION))
				if (l.getParameters().get(0).equals(target))
					destination.add(l.getParameters().get(1).toString());
			
			// put all known edges in one LinkedList
			if (l.getPredicate().equals(Const.UNEXPLOREDEDGE))
				edges.add(l);

			// put all unknown edges in one LinkedList
			if (l.getPredicate().equals(Const.EXPLOREDEDGE))
				edges.add(l);

			// put all Vertices into a list
			if (l.getPredicate().equals(Const.PROBEDVERTEX)){
				vertices.add(l.getParameters().get(0));
			}
			// put all Vertices into a list
			if (l.getPredicate().equals(Const.UNPROBEDVERTEX)) {
				vertices.add(l.getParameters().get(0));
			}
		}
		
		//DEBUG_DENNIS: If the position of the target is unknown
		// This shouldn't be possible. If so, correct in DoInitRepair
		if (destination.isEmpty()){
			System.out.println("###DEBUG_DENNIS: DoHandleRepair - I don't know the targets position###");
			return null;
			
		}

		ArrayList<LogicGoal> routeThis = new HelpFunctions().findRoute(position, destination, vertices, edges, false,200);

		// ####################################################################################################
		// PROCESSING RESULTS
				
		// This is the outsourced code for processAction (copied from DoInitRepair)
		
		// Filling the goalBase
		LogicGoal singleGoal=null;
		for (int i=0; i<routeThis.size(); i++){
			singleGoal = routeThis.get(i);
			agent.addGoal(singleGoal);
		}
		
		// Processing the first action (last entry in the list)
		// catch case if no route is needed (already on right position)
		if (routeThis.size() == 0){
			agent.addBelief(new LogicBelief(Const.IWANTTOREPAIR, target));
			return MarsUtil.repairAction(target.substring(5).toLowerCase());
		}
		singleGoal = routeThis.get(routeThis.size()-1);
		
		Action returnAction = null;
		returnAction = new Action(singleGoal.getPredicate(), new Identifier(singleGoal.getParameters().get(0)));
		return returnAction;
		
		
		// PROCESSING RESULTS
		// ####################################################################################################
		
		
		
	}// DoHandleRepair
}
