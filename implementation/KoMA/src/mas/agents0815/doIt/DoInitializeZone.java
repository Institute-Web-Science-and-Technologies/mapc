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

public class DoInitializeZone {

	Collection<LogicBelief> beliefs;
	Collection<LogicGoal> goals;
	final static int MAXZONESIZE=20;
	final static int MINZONESIZE=6;
	final static int MAXENEMIES = 10;

	private final HelpFunctions help = new HelpFunctions();
	
	/*********************************************************
	 * @brief Find appropriate zone to be conquered
	 * join the zone
	 * @param current
	 *            beliefs and goals, copy of the SubsumptionAgent
	 * @return goto Action to a nearest border vertex of the new initialized
	 *         zone
	 * @see processAction 
	 ****************************************/
	public DoInitializeZone(Collection<LogicBelief> beliefs,
			Collection<LogicGoal> goals) {
		super();
		this.beliefs = beliefs;
		this.goals = goals;
	}

	public Action doIt(SubsumptionAgent agent) {
	
		LinkedList<LogicBelief> edges = new LinkedList<LogicBelief>();
		LinkedList<String> verticesByName = new LinkedList<String>();
		LinkedList<String> probedVerticesByName = new LinkedList<String>();
		LinkedList<LogicBelief> probedVertices = new LinkedList<LogicBelief>();
		LinkedList<LogicBelief> vertices = new LinkedList<LogicBelief>();
		LinkedList<String> enemyVertices = new LinkedList<String>();
		LinkedList<LinkedList<String>> possibleZones = new LinkedList<LinkedList<String>>();
		
		String position = "";
		
		//get all Beliefs necessary for further computation
		for (LogicBelief l : beliefs) {

			if (l.getPredicate().equals(Const.POSITION))
				position = l.getParameters().get(0).toString();

			// put all unknown edges in one LinkedList
			if (l.getPredicate().equals(Const.EXPLOREDEDGE))
				edges.add(l);
			if (l.getPredicate().equals(Const.UNEXPLOREDEDGE))
				edges.add(l);

			// put all Vertices into a list
			if (l.getPredicate().equals(Const.PROBEDVERTEX)){
				probedVerticesByName.add(l.getParameters().get(0));
				probedVertices.add(l);
				verticesByName.add(l.getParameters().get(0));
				vertices.add(l);
			}
			if (l.getPredicate().equals(Const.UNPROBEDVERTEX)){
				verticesByName.add(l.getParameters().get(0));
				vertices.add(l);
			}
			if (l.getPredicate().equals(Const.VISIBLEENEMY)){
				enemyVertices.add(l.getParameters().get(1));
			}
			
		}
		
		//if there are too few possible startVertices, we may not
		//find a zone, so do not search
		if (probedVerticesByName.size()<5){
			return MarsUtil.rechargeAction();
		}
		
		//calculate a zone with given parameters
		LinkedList<String> bestZone = new LinkedList<String>();
		LinkedList<String> newZone = new LinkedList<String>();
		for(String trythisVertex: probedVerticesByName){
			newZone =help.findZone(12, MAXZONESIZE, trythisVertex, vertices,  edges);
			if (!(newZone==null)){
				//is the zone big enough?
				if (getborderSize(newZone)>7){
					bestZone=newZone;
					break;
				}
			//possibleZones.add(newZone);
			}
		}//for
		
		if (bestZone==null){
			return MarsUtil.rechargeAction();
		}

			int bestValue =0;
			int bestIndex =-1;
			int bestBorderSize=getborderSize(bestZone);
			
	//In current version, optimization is not done, as it takes
			//too much time
	/*	for(int i=0; i<possibleZones.size();i++){
			if (possibleZones.get(i)!=null){
				LinkedList<String> myZone=possibleZones.get(i);
				if (possibleZones.size()>MINZONESIZE){
					if (countEnemiesInZone(myZone, enemyVertices)<MAXENEMIES){
						//get the value of the Zone
						int value = getZoneValue(myZone, probedVertices);
						int borderSize= getborderSize(myZone);
						value = value/borderSize;
						if (value>bestValue){
							bestValue=value;
							bestIndex=i;
							bestBorderSize=borderSize;
						}
					}
				}
			}
		}//for
		
		if(bestIndex==-1){
			System.out.println("Could not find a Zone!!");
			return null;
		}*/
		
		
		LinkedList<String> borderLineByName = new LinkedList<String>();
		LinkedList<String> innerZoneByName = new LinkedList<String>();
		
		for (int i=0; i<bestBorderSize; i++){
			System.out.print("; "+ bestZone.get(i));
			borderLineByName.add(bestZone.get(i));
		}
		
		
		//go to first vertex of zone
		LinkedList<String> destination = new LinkedList<String>();
		destination.add(borderLineByName.get(0));
		ArrayList<LogicGoal> routeToNearestVertex = help.findRoute(position, destination, verticesByName, edges, true,200);
		
		//Create the necessary Messages
		String[] nodes = new String[borderLineByName.size()];
		String[] innerNodes = new String[innerZoneByName.size()];
		String[] freeNodes = new String[borderLineByName.size()-1];
		int index = 0;
		
		
		//fill the lists
		for(int i = 0; i < borderLineByName.size(); i++){				
			nodes[i] = borderLineByName.get(i);		
		}
		
		for(int i = 1; i < borderLineByName.size(); i++){				
			freeNodes[i-1] = borderLineByName.get(i);		
		}
		
		for(int i = 0; i < innerZoneByName.size(); i++){				
			innerNodes[i] = innerZoneByName.get(i);		
		}
		
		//add and send all Beliefs
		agent.broadcastBelief(new LogicBelief(Const.ZONE, nodes));
		agent.addBelief(new LogicBelief(Const.ZONE, nodes));
		agent.broadcastBelief(new LogicBelief(Const.FREENODESOFZONE, freeNodes));
		agent.addBelief(new LogicBelief(Const.FREENODESOFZONE, freeNodes));
		agent.broadcastBelief(new LogicBelief(Const.INNERZONE, innerNodes));
		agent.addBelief(new LogicBelief(Const.INNERZONE, innerNodes));
		agent.addBelief(new LogicBelief(Const.INZONE, destination.get(0)));
		agent.broadcastBelief(new LogicBelief(Const.ALLIEDINZONE, agent.getMyName()));
		
		// fill goals
		int priority=1;
		String dest="";
		for (int i = 0; i < routeToNearestVertex.size() ; i++) {
			dest = routeToNearestVertex.get(i).getParameters().firstElement()
					.toString();
			agent.addGoal(new LogicGoal(Const.GOTO, String.valueOf(priority),dest));
			priority++;
		}

		// return first step of the route
		dest = destination.get(0).toString();
		return MarsUtil.gotoAction(dest);
	}//doIt
	
	
	/*********************************************************
	 * @brief returns the number of enemies located in the zone
	 * @param zone in compressed format,
	 * list of the positions of known enemies
	 * @return number of enemies in zone
	 * @see DoInitializeZone
	 ****************************************/
	public int countEnemiesInZone(LinkedList<String> myZone, LinkedList<String> enemyVertices){
		
	 int count=0;
	 for (String s:enemyVertices){
		 if (myZone.contains(s))
			 count++;
	 }//for
	 return count;
	}//countEnemiesInZone
	
	
	/*********************************************************
	 * @brief calculate the value of the zone
	 * @param zone in compressed format
	 * list of all probed vertices
	 * @return value of the zone
	 * @see DoInitializeZone
	 ****************************************/
	public int getZoneValue(LinkedList<String> myZone, LinkedList<LogicBelief> probedVertices){
		int value=0;
		boolean foundProbed;
		for(String s: myZone){
			foundProbed=false;
			for(LogicBelief b:probedVertices){
				if (b.getParameters().get(0).equals(s)){
					value+=Integer.parseInt(b.getParameters().get(1));
					foundProbed=true;
				}//if
			}//for
			if (!foundProbed){
				value+=1;
			}
		}//for
	    return value;	
	}//getZoneValue
	
	/*********************************************************
	 * @brief calculate the border size of the zone
	 * @param zone in compressed format
	 * @return size of the zones border
	 * @see DoInitializeZone
	 ****************************************/
	public int getborderSize(LinkedList<String>  myZone){
		for (int i=0; i<myZone.size();i++){
			if(myZone.get(i).equals("interiorNOW"))
				return i;
		}
	return 0;
	}//getborderSize
	
	
}//class
