package mas.agents0815.doIt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import eis.iilang.Action;

import apltk.interpreter.data.LogicBelief;
import apltk.interpreter.data.LogicGoal;
import mas.agents0815.Const;
import mas.agents0815.HelpFunctions;
import mas.agents0815.SubsumptionAgent;
import massim.javaagents.agents.MarsUtil;

public class DoPlanDefendRoute {
	private Collection<LogicBelief> beliefs;
	private Collection<LogicGoal> goals;

	public DoPlanDefendRoute(Collection<LogicBelief> b,
			Collection<LogicGoal> g) {
		this.beliefs = b;
		this.goals = g;
	}

	public Collection<LogicBelief> getBeliefs() {
		return this.beliefs;
	}

	public Collection<LogicGoal> getGoals() {
		return this.goals;
	}

	/*********************************************************
	 * calculate a route to the enemies position
	 * within the zone and return the first step
	 * no goal list filled as enemy agent may move
	 * 
	 * @param beliefs
	 *            and goals
	 *            copy of SubsumptionAgent
	 *            position of enemy agent
	 * @return the first step towards the enemy
	 * @see todo*
	 *********************************************************/
	public Action doIt(String destination, SubsumptionAgent agent) {

		LinkedList<LogicBelief> edges = new LinkedList<LogicBelief>();
		LinkedList<String> vertices = new LinkedList<String>();
		LinkedList<String> destinations = new LinkedList<String>();
		destinations.add(destination);
		String position = "";
		
		//get all beliefs necessary for further computation
		for (LogicBelief l : beliefs) {

			// get current position
			if (l.getPredicate().equals(Const.POSITION))
				position = l.getParameters().get(0).toString();

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
			if (l.getPredicate().equals(Const.UNPROBEDVERTEX)) 
				vertices.add(l.getParameters().get(0));
		}//for
			


		ArrayList<LogicGoal> routeToNearestVertex = new HelpFunctions().findRoute(position,
				destinations, vertices, edges, true,200);

		if (routeToNearestVertex==null || routeToNearestVertex.isEmpty()){
			System.out.println("defender: could not go to "+ destination);
			return null;
		}
		String dest = routeToNearestVertex.get(routeToNearestVertex.size() - 1).getParameters()
		.firstElement().toString();
		return MarsUtil.gotoAction(dest);
	}// doDefendZone
}//class
