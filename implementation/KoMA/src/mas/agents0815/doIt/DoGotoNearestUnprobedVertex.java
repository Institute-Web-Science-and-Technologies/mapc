package mas.agents0815.doIt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import apltk.interpreter.data.LogicBelief;
import apltk.interpreter.data.LogicGoal;
import mas.agents0815.Const;
import mas.agents0815.HelpFunctions;

public class DoGotoNearestUnprobedVertex {
	private Collection<LogicBelief> beliefs;
	private Collection<LogicGoal> goals;

	public DoGotoNearestUnprobedVertex(Collection<LogicBelief> b,
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
	 * find all unprobed and known vertices, find a route to the nearest of
	 * those and returns the first step towards it
	 * 
	 * @param beliefs
	 *            and goals
	 * @return the first step towards the nearest unprobed vertex
	 * @see processAction
	 *********************************************************/
	public Collection<LogicGoal> doIt() {

		LinkedList<LogicBelief> edges = new LinkedList<LogicBelief>();
		LinkedList<String> unProbedVertices = new LinkedList<String>();
		LinkedList<String> vertices = new LinkedList<String>();
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
			if (l.getPredicate().equals(Const.UNPROBEDVERTEX)) {
				vertices.add(l.getParameters().get(0));
				if (!beliefs.contains(new LogicBelief(Const.BLOCK, Const.PROBE,l.getParameters().get(0))))
					unProbedVertices.add(l.getParameters().get(0));
			}
		}

		// can happen at beginning
		if (unProbedVertices.isEmpty()){
			// TODO add skip as goal
			System.out.println("i found no unprobed vertices");
			goals.add(new LogicGoal(Const.SKIPACTION));
		    return goals;
	    }

		ArrayList<LogicGoal> goals = new HelpFunctions().findRoute(position,
				unProbedVertices, vertices, edges, true,200);

		return goals;
	}// doGotoNearestUnprobedVertex
}
