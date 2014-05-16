package mas.agents0815.doIt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import eis.iilang.Action;

import apltk.interpreter.data.LogicBelief;
import apltk.interpreter.data.LogicGoal;
import mas.agents0815.Const;
import mas.agents0815.HelpFunctions;
import mas.agents0815.InternalAction;
import mas.agents0815.SubsumptionAgent;
import massim.javaagents.agents.MarsUtil;

public class DoProbeInZone {
	private Collection<LogicBelief> beliefs;
	private Collection<LogicGoal> goals;

	public DoProbeInZone(Collection<LogicBelief> b,
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
	 * finds all unprobed vertices within the zones, find a route to the nearest of
	 * those and returns the first step towards it
	 * 
	 * @param beliefs
	 *            and goals
	 * @return the first step towards the nearest unprobed vertex
	 * in the zone
	 * @see processAction
	 *********************************************************/
	public Action doIt(SubsumptionAgent agent) {

		LinkedList<LogicBelief> edges = new LinkedList<LogicBelief>();
		LinkedList<String> unProbedVertices = new LinkedList<String>();
		LinkedList<String> vertices = new LinkedList<String>();
		String position = "";
		
		LinkedList<LogicBelief> zone = agent.getAllBeliefs(Const.ZONE);
		LinkedList<LogicBelief> innerZone = agent.getAllBeliefs(Const.INNERZONE);
		LinkedList<String> protectedVertices = new LinkedList<String>();

		//these vertices needs to be probed
		if(!zone.isEmpty() && !innerZone.isEmpty()){
			for (int i=0; i<zone.get(0).getParameters().size();i++)
				protectedVertices.add(zone.get(0).getParameters().get(i));
			for (int i=0; i<innerZone.get(0).getParameters().size();i++)
				protectedVertices.add(innerZone.get(0).getParameters().get(i));
		}//for

		//get all Beliefs necessary for further computation
		for (LogicBelief l : beliefs) {

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
			System.out.println("I found no unprobed vertices");
			return MarsUtil.rechargeAction();
	    }
		
		//get all unprobed vertices within the zone
		LinkedList<String> candidates = new LinkedList<String>();
		for(String s:unProbedVertices){
			if (protectedVertices.contains(s))
				candidates.add(s);
		}
		
		//do I have to move?
		if (candidates.contains(agent.getMyPos())){
			return MarsUtil.probeAction();
		}

		//else find the route to the nearest of those
		ArrayList<LogicGoal> route = new HelpFunctions().findRoute(agent.getMyPos(),
				candidates, vertices, edges, true,200);
		
		// fill goals
	if(!(route.size()==0)){
		agent.addGoal(new LogicGoal(Const.PROBE, "1"));
		String dest= "";
		// fill goals
		int priority=2;
		for (int i = 0; i < route.size() ; i++) {
			dest = route.get(i).getParameters().firstElement()
					.toString();
			agent.addGoal(new LogicGoal(Const.GOTO, String.valueOf(priority),dest));
			priority++;
		}//for

		// return first step of the route
		dest = route.get(route.size() - 1).getParameters()
				.firstElement().toString();
		
		//I go there, u do not follow me!
			agent.broadcastBelief(new LogicBelief(Const.BLOCK, Const.SURVEY, route.get(0).getParameters().firstElement()
				.toString()));
		
		return MarsUtil.gotoAction(dest);
	}//if
	//Iam standing on an unprobed vertex
	return MarsUtil.probeAction();

	}// doGotoNearestUnprobedVertex
}
