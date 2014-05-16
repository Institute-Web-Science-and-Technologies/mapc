package mas.agents0815.doIt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Vector;

import eis.iilang.Action;

import mas.agents0815.Const;
import mas.agents0815.HelpFunctions;
import mas.agents0815.SubsumptionAgent;
import massim.javaagents.agents.MarsUtil;
import apltk.interpreter.data.LogicBelief;
import apltk.interpreter.data.LogicGoal;

public class DoJoinZone {

	Collection<LogicBelief> beliefs;
	Collection<LogicGoal> goals;

	private final HelpFunctions help = new HelpFunctions();
	
	/*********************************************************
	 * @brief find an existing zone and join it
	 * @param current
	 *            beliefs and goals
	 *            copy of SubsumptionAgent
	 * @return goto Action to a vertex of the zone
	 * @see processAction 
	 ****************************************/
	public DoJoinZone(Collection<LogicBelief> beliefs,
			Collection<LogicGoal> goals) {
		super();
		this.beliefs = beliefs;
		this.goals = goals;
	}
	
	public Action doIt(SubsumptionAgent agent) {
		
		LinkedList<LogicBelief> edges = new LinkedList<LogicBelief>();
		LinkedList<String> vertices = new LinkedList<String>();
		Vector<String> zone;
		LinkedList<String> verticesToConquer = new LinkedList<String>();
		
		String position = "";
		String zoneIndex = null;
		
		//get all parameters necessary for further computation
		for (LogicBelief l : beliefs) {
			// get current position
			if (l.getPredicate().equals(Const.POSITION))
				position = l.getParameters().get(0).toString();

			// put all unknown edges in one LinkedList
			if (l.getPredicate().equals(Const.EXPLOREDEDGE))
				edges.add(l);
			
			if (l.getPredicate().equals(Const.UNEXPLOREDEDGE))
				edges.add(l);

			// put all Vertices into a list
			if (l.getPredicate().equals(Const.PROBEDVERTEX)){
				vertices.add(l.getParameters().get(0));
			}
			if (l.getPredicate().equals(Const.UNPROBEDVERTEX)){
				vertices.add(l.getParameters().get(0));
			}
			
			//get the free vertices of the zone
			if(l.getPredicate().equals(Const.FREENODESOFZONE)){
				for(int i = 0; i <l.getParameters().size(); i++){
					if (!(l.getParameters().get(i)==null))
						verticesToConquer.add(l.getParameters().get(i));
				}
			}//if
		}//for
		
		//go to the next vertex in line
		LinkedList<String> destination = new LinkedList<String>();
		if (verticesToConquer.size()==0){
			//there are no free Nodes
			 //do I have enough energy for an attack?
			 if (agent.getMyEnergy()<9){
				  return MarsUtil.rechargeAction();
			 }else
				return new DoRandomWalk(beliefs, goals).doIt(agent);
		}//if
		
		//leave one vertex between the agents if possible
		if (verticesToConquer.size()>1)
			destination.add(verticesToConquer.get(1));
		else if (verticesToConquer.size()==1)
			destination.add(verticesToConquer.get(0));
		else {System.out.println("zone is already conquered");
			if (agent.getMyEnergy()<9){
				return MarsUtil.rechargeAction();
			}else
				return new DoRandomWalk(beliefs, goals).doIt(agent);
		}//else
		
		if((destination.isEmpty()))
			return MarsUtil.rechargeAction();
		
		//find the route to vertex of the zone
		ArrayList<LogicGoal> routeToNearestVertex = new ArrayList<LogicGoal>();
		if(!destination.get(0).equals(agent.getMyPos())){
			routeToNearestVertex = help.findRoute(position, destination, vertices, edges, true,200);		
			if (routeToNearestVertex.size()==0){
				System.out.println("could not join zone");
				 if (agent.getMyEnergy()<9){
					  return MarsUtil.rechargeAction();
				 }else
					return new DoRandomWalk(beliefs, goals).doIt(agent);
			}
		}//if

			//update the free Nodes
			String[] freeNodes = new String[verticesToConquer.size()-1];
			
			System.out.println(destination.size());
			String vertex="";
			boolean onceDeleted =false;
	
			for(int i = 0, y = 0; i < verticesToConquer.size(); i++){		
					
				vertex=verticesToConquer.get(i);

				if (!(vertex==null)){
					//may not delete the same vertex twice, as 
					//array length is predetermined
					if (!(vertex.equals(destination.get(0))) || onceDeleted){
						freeNodes[y] = verticesToConquer.get(i);
						y++;
					}//if
					if(vertex.equals(destination.get(0)))
						onceDeleted=true;
				}
			}//for
		
		//add and send all relevant beliefs
		agent.broadcastBelief(new LogicBelief(Const.FREENODESOFZONE, freeNodes));
		agent.addBelief(new LogicBelief(Const.FREENODESOFZONE, freeNodes));
		agent.addBelief(new LogicBelief(Const.INZONE, destination.get(0)));
		agent.broadcastBelief(new LogicBelief(Const.ALLIEDINZONE, agent.getMyName()));
		
		// fill goals
		if(!destination.get(0).equals(agent.getMyPos())){
			int priority=1;
			String dest="";
			for (int i = 0; i < routeToNearestVertex.size() ; i++) {
				dest = routeToNearestVertex.get(i).getParameters().firstElement()
					.toString();
				agent.addGoal(new LogicGoal(Const.GOTO, String.valueOf(priority),dest));
				priority++;
			}

			// return first step of the route
			dest = routeToNearestVertex.get(routeToNearestVertex.size() - 1).getParameters()
					.firstElement().toString();
			return MarsUtil.gotoAction(dest);
		}
		else{
			 if (agent.getMyEnergy()<9){
				  return MarsUtil.rechargeAction();
			 }else
				return new DoRandomWalk(beliefs, goals).doIt(agent);
		}
		
	}//doIt
}//class
