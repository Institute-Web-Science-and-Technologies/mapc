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

public class DoAttackEnemyZone {
	
	Collection<LogicBelief> beliefs;
	Collection<LogicGoal> goals;

	private final HelpFunctions help = new HelpFunctions();
	
	public DoAttackEnemyZone(Collection<LogicBelief> beliefs,
			Collection<LogicGoal> goals) {
		super();
		this.beliefs = beliefs;
		this.goals = goals;
	}

	/*********************************************************
	 * @brief find enemy agents and plan a attack,
	 * but stay away from our Zone
	 * @param current
	 *            beliefs and goals
	 * @return goto Action to the node where the zone is assumed to be
	 * @see processAction 
	 ****************************************/
	public Action doIt(SubsumptionAgent agent) {
		
		System.out.println("I go attacking!!");
		//if an enemy is on a neighbor node, go and beat him
		//precondition: don not run into the zone
		LinkedList<LogicBelief> neighbors = agent.getAllBeliefs(Const.NEIGHBOR);
		LinkedList<LogicBelief> visEnemy = agent.getAllBeliefs(Const.VISIBLEENEMY);
		LinkedList<LogicBelief> enemyInZone= agent.getAllBeliefs(Const.ENEMYINZONE);
		LinkedList<String> possibleAim= new LinkedList<String>();
		String enemyPosition ="";
		
		for (LogicBelief b: visEnemy){
			enemyPosition = b.getParameters().get(1);
			//is he not in the zone?
			if (!enemyInZone.contains(new LogicBelief(Const.ENEMYINZONE,enemyPosition))){
				//is he enabled?
				if (b.getParameters().get(2).equals("normal")){
					possibleAim.add(enemyPosition);
					//is he on a neighbor vertex
					for (LogicBelief n: neighbors){
						if (n.getParameters().get(0).equals(enemyPosition)){
							//beat the shit out of him
							return MarsUtil.gotoAction(enemyPosition);
							
						}	
					}//for
				}			
			}
		}//for
		
		
		//else go roaming, to a neighbor with distance 3 or more
		//from the zone
		//catch the case, that he runs back and forth or in a circle
		ArrayList<LogicGoal> route = new ArrayList<LogicGoal>();
		LinkedList<LogicBelief> edges = new LinkedList<LogicBelief>();
		LinkedList<String> vertices = new LinkedList<String>();
		
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
			}
		}
		
		//go to the nearest healthy enemy out of the zone
		if(possibleAim.size()>0){
			ArrayList<LogicGoal> routeToNearestVertex = new HelpFunctions().findRoute(agent.getMyPos(),
					possibleAim, vertices, edges, true,200);

			if (routeToNearestVertex==null || routeToNearestVertex.isEmpty()){
				System.out.println("defender: could not go to ");
				return null;
			}
			String dest = routeToNearestVertex.get(routeToNearestVertex.size() - 1).getParameters()
			.firstElement().toString();
			return MarsUtil.gotoAction(dest);
		}//if
		
		//else roam
		
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
		
		//Case 2: I am standing in the zone: get out of here!
		//go to the zone edge
		else if(zoneVertices.contains(agent.getMyPos())){
			System.out.println("Iam leaving the zone Edge");
			for (LogicBelief n: neighbors){
				if (!innerZoneVertices.contains(n.getParameters().get(0)) &&
						!zoneVertices.contains(n.getParameters().get(0))){
					//go there
					return MarsUtil.gotoAction(n.getParameters().get(0));
					
				}	
			}//for
			return new DoRandomWalk(beliefs, goals).doIt(agent);
		}//else
		
		//Case 3: Iam out of zone
		//go to the vertex furthest away from the zone
		else {
			System.out.println("I go anywhere");
			String furthestVertex="";
			int  maxDistance =-1;
			for (LogicBelief n: neighbors){
				if(!zoneVertices.contains(n.getParameters().get(0))){
					if(!innerZoneVertices.contains(n.getParameters().get(0))){
						route = (new HelpFunctions()).findRoute(agent.getMyPos(), zoneVertices, vertices, edges, false,200);
						if(route.size()>maxDistance){
							furthestVertex=n.getParameters().get(0);
							maxDistance=route.size();
						}
			
					}
				}
			}//for
			return new DoRandomWalk(beliefs, goals).doIt(agent);
		}//else
		
		//System.out.println("nothing found");
		//return MarsUtil.skipAction();
		
	}//doIt

}//class
