package mas.agents0815.doIt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import eis.iilang.Action;
import eis.iilang.Identifier;
import mas.agents0815.Const;
import mas.agents0815.HelpFunctions;
import mas.agents0815.InternalAction;
import mas.agents0815.SubsumptionAgent;
import massim.javaagents.agents.MarsUtil;
import apltk.interpreter.data.LogicBelief;
import apltk.interpreter.data.LogicGoal;


public class DoGetRepair {
	public Collection<LogicBelief> beliefs;
	public Collection<LogicGoal> goals;
	private final HelpFunctions help = new HelpFunctions();
	
	public DoGetRepair(Collection<LogicBelief> beliefs, Collection<LogicGoal> goals) {
		super();
		this.beliefs = beliefs;
		this.goals = goals;
	}

	/*********************************************************
	 * @brief identify the repairer nearest to me and 
	 * find a route to him
	 * @param copy of the SubsumptionAgent
	 * @return only the first step towards the repairer
	 * Goal List is not filled, as the repairer may change
	 * his position
	 * @see processAction 
	 ****************************************/
	
	public Action doIt(SubsumptionAgent agent) {

		LinkedList<String> repairerNames = new LinkedList<String>();
		LinkedList<LogicBelief> freeNodes = new LinkedList<LogicBelief>();
		LinkedList<String> vertices = new LinkedList<String>();
		
		//find the position of the repairer in my team
		for(LogicBelief l : agent.getAllBeliefs(Const.ALLIEDROLE)){
			if(l.getParameters().get(1).equals(Const.ROLEREPAIRER) && !(l.getParameters().get(0).equals(agent.getMyName()))){
				repairerNames.add(l.getParameters().get(0));
			}//if
		}//for
	
		//As I am broken, I will leave the zone
		LinkedList<LogicBelief> myZone = agent.getAllBeliefs(Const.INZONE);
		if (!myZone.isEmpty()){
			//my position is free now
			freeNodes =agent.getAllBeliefs(Const.FREENODESOFZONE);
			LogicBelief b=freeNodes.get(0);
			for (int i=0; i<b.getParameters().size(); i++){
				vertices.add(b.getParameters().get(i));
			}
			vertices.add(myZone.get(0).getParameters().get(0));
			agent.removeBeliefs(Const.FREENODESOFZONE);
			agent.addBelief(new LogicBelief(Const.FREENODESOFZONE,vertices));
			agent.broadcastBelief(new LogicBelief(Const.FREENODESOFZONE,vertices));
		}
		
		agent.removeBeliefs(Const.INZONE);

		//find the route the nearest repairer
		LinkedList<LogicBelief> AlliesPos = agent.getAllBeliefs(Const.ALLIEDPOSITION);
		LinkedList<String> destination = new LinkedList<String>();
		for (LogicBelief b:AlliesPos){
			if (repairerNames.contains(b.getParameters().get(0).toString())){
				destination.add(b.getParameters().get(1));
			}
		}
		
		if (destination.isEmpty()){
			System.out.println("could not find a repairer");
			return new DoRandomWalk(beliefs, goals).doIt(agent);
		}else{
			//Am I standing with a repairer?
			if(destination.contains(agent.getMyPos()))
				return MarsUtil.rechargeAction();
			else{
				
			LinkedList<LogicBelief> edges = new LinkedList<LogicBelief>();
			LinkedList<String> verticesByName = new LinkedList<String>();

			
			for (LogicBelief l : beliefs) {

				// put all unknown edges in one LinkedList
				if (l.getPredicate().equals(Const.EXPLOREDEDGE))
					edges.add(l);
				if (l.getPredicate().equals(Const.UNEXPLOREDEDGE))
					edges.add(l);

				// put all Vertices into a list
				if (l.getPredicate().equals(Const.PROBEDVERTEX)){
					verticesByName.add(l.getParameters().get(0));
				}
				if (l.getPredicate().equals(Const.UNPROBEDVERTEX)){
					verticesByName.add(l.getParameters().get(0));
				}	
			}//for
			
			
			ArrayList<LogicGoal> route = help.findRoute(agent.getMyPos(), destination, verticesByName, edges, true,200);
			// return first step of the route
			String dest = route.get(route.size() - 1).getParameters()
					.firstElement().toString();
			
			//do I have enough energy for the step?
			String from="";
			String to="";
			int cost=9;
			for (LogicBelief l : beliefs) {

				// put all unknown edges in one LinkedList
				if (l.getPredicate().equals(Const.EXPLOREDEDGE)){
					from=l.getParameters().get(0).toString();
					to=l.getParameters().get(1).toString();
					if(from.equals(agent.getMyPos()) && to.equals(dest)){
						cost = Integer.valueOf(l.getParameters().get(2));
						break;
					}
					if(to.equals(agent.getMyPos()) && from.equals(dest)){
						cost = Integer.valueOf(l.getParameters().get(2));
						break;
					}
				}//if

			}//for
			if(cost>7)
				return new DoRandomWalk(beliefs, goals).doIt(agent);
			if (agent.getMyEnergy()<cost)
				return MarsUtil.rechargeAction();
			else
				return MarsUtil.gotoAction(dest);
			}//else
		}
		

	}


}//class