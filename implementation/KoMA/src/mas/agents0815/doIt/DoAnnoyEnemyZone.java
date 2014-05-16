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

public class DoAnnoyEnemyZone {
	
	Collection<LogicBelief> beliefs;
	Collection<LogicGoal> goals;

	private final HelpFunctions help = new HelpFunctions();
	
	public DoAnnoyEnemyZone(Collection<LogicBelief> beliefs,
			Collection<LogicGoal> goals) {
		super();
		this.beliefs = beliefs;
		this.goals = goals;
	}

	/*********************************************************
	 * @brief find enemy agents which is not an saboteur and
	 * is standing alone on a vertex
	 * @param current
	 *            beliefs and goals
	 * @return goto Action to the node where the zone is assumed to be
	 * @see processAction 
	 ****************************************/
	public Action doIt(SubsumptionAgent agent) {
		
		System.out.println("I go attacking!!");
		//if an enemy is on a neighbor node, go and beat him
		//precondition: don not run into the zone
		LinkedList<LogicBelief> visEnemy = agent.getAllBeliefs(Const.VISIBLEENEMY);
		LinkedList<LogicBelief> enemyInZone= agent.getAllBeliefs(Const.ENEMYINZONE);
		LinkedList<LogicBelief> neighbors = agent.getAllBeliefs(Const.NEIGHBOR);
		String enemyPosition ="";
		LinkedList<String> candidates = new LinkedList<String>();
		LinkedList<String> tabuList = new LinkedList<String>();
		
		for (LogicBelief b: visEnemy){
			enemyPosition = b.getParameters().get(1);
			//is he not in the zone?
			if (!enemyInZone.contains(new LogicBelief(Const.ENEMYINZONE,enemyPosition))){
				//is he enabled?
				if (b.getParameters().get(2).equals("normal")){
					//is he not an attacker?
					if(!(b.getParameters().get(0).contains(new Integer(5).toString()))||
							!(b.getParameters().get(0).contains(new Integer(6).toString()))){
							//only go to a vertex with one agent on it!
							if (candidates.contains(enemyPosition)){
								tabuList.add(enemyPosition);
							}else{
								candidates.add(enemyPosition);
							}
					}//if
				}//if			
			}//if
		}//for
		
		//delete candidates on tabuList
		for (String s:tabuList){
			candidates.remove(s);
		}
		
		//go to the nearest candidates
		
		//do I already stand on an enemy vertex?
		if (candidates.contains(agent.getMyPos())){
			return MarsUtil.rechargeAction();
		}
		
		
		//get all beliefs necessary for further computation
		LinkedList<LogicBelief> edges = new LinkedList<LogicBelief>();
		LinkedList<String> vertices = new LinkedList<String>();
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
			if (l.getPredicate().equals(Const.UNPROBEDVERTEX)) 
				vertices.add(l.getParameters().get(0));
		}//for
		
			
		//if there are candidates, go to the nearest
		if (candidates.size()>0){
	
				
			//find a route to the nearest candidate
			ArrayList<LogicGoal> routeToNearestVertex = new HelpFunctions().findRoute(agent.getMyPos(),
					candidates, vertices, edges, true,200);

			if (routeToNearestVertex==null || routeToNearestVertex.isEmpty()){
				System.out.println("annoyer: could not find route");
				return null;
			}
			String dest = routeToNearestVertex.get(routeToNearestVertex.size() - 1).getParameters()
			.firstElement().toString();
			return MarsUtil.gotoAction(dest);
		}//if
			
		//if there are no candidates, go roaming, to a neighbor out of the zone
		//catch the case, that he runs back and forth or in a circle
		ArrayList<LogicGoal> route = new ArrayList<LogicGoal>();
		
		LinkedList<LogicBelief> zone= agent.getAllBeliefs(Const.ZONE);
		LinkedList<String> zoneVertices= new LinkedList<String>();
		LinkedList<LogicBelief> innerZone= agent.getAllBeliefs(Const.INNERZONE);
		LinkedList<String> innerZoneVertices= new LinkedList<String>();
		
		if (!zone.isEmpty()){
			for (int i=0; i<zone.get(0).getParameters().size();i++)
				zoneVertices.add(zone.get(0).getParameters().get(i));
		
			for (int i=0; i<innerZone.get(0).getParameters().size();i++)
				zoneVertices.add(innerZone.get(0).getParameters().get(i));
		}
		
		//is there already a zone?
		if (zone.isEmpty()){
			System.out.println("there is no zone");
			return new DoRandomWalk(beliefs, goals).doIt(agent);
		}
		
		//Case 1: I am standing in the zone: get out of here!
		//got to the zone edge
		else if(innerZoneVertices.contains(agent.getMyPos())){
			System.out.println("Iam leaving the zone");
			route = (new HelpFunctions()).findRoute(agent.getMyPos(), zoneVertices, vertices, edges, true, 200);
			
			String dest= "";
			// fill goals
			int priority=1;
			for (int i = 0; i < route.size() ; i++) {
				dest = route.get(i).getParameters().firstElement()
						.toString();
				agent.addGoal(new LogicGoal(Const.GOTO, String.valueOf(priority),dest));
				priority++;
			}

			// return first step of the route
			dest = route.get(route.size() - 1).getParameters()
					.firstElement().toString();

			return MarsUtil.gotoAction(dest);	
		}//else
		
		//Case 2: I am standing on the border of the edge
		//leave the zone
		else if(zoneVertices.contains(agent.getMyPos())){
			System.out.println("Iam leaving the zone Edge");
			for (LogicBelief n: neighbors){
				if (!innerZoneVertices.contains(n.getParameters().get(0).toString()) &&
						!zoneVertices.contains(n.getParameters().get(0).toString())){
					//go there
					return MarsUtil.gotoAction(n.getParameters().get(0));
					
				}	
			}//for

			//no exit found, do random walk
			return new DoRandomWalk(beliefs, goals).doIt(agent);
		}//else
		
		//Case 3: I am out of zone
		else {

			return new DoRandomWalk(beliefs, goals).doIt(agent);
		}//else
		
		//System.out.println("nothing found");
		//return MarsUtil.skipAction();
		
	}//doIt

}//class
