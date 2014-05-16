package mas.agents0815;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.TreeMap;

import eis.iilang.Parameter;
import eis.iilang.Percept;

import apltk.interpreter.data.LogicBelief;
import apltk.interpreter.data.LogicGoal;

public class HelpFunctions {
	
	public static final int unexploredEdgeWeight = 10;
	public static final int MAXZONECONNECTION = 4;


	/*********************************************************
	 * @brief finds fastest route with dijkstra algorithms to nearest vertex
	 *        contained in destination
	 * @param current
	 *            position, all posible destinations, all edges known to the
	 *            agent, all vertices known to the agent
	 *            useEdgeWeight=1 means, that the algorihm take
	 *            account of the edge weight calculation shortes paths
	 *            useEdgeWeight=0 mean, that the length of a path
	 *            is determined by the number of edges used,
	 *            maximum number of serach steps
	 * @return ArrayList of Logic goal containing all steps to the nearest
	 *         unprobed vertex
	 * @see todo*
	 *********************************************************/
	public ArrayList<LogicGoal> findRoute(String position,
			LinkedList<String> destination,
			LinkedList<String> vertices, LinkedList<LogicBelief> edges, boolean useEdgeWeight, int MAXSEARCH) {
		// 1. find shortest ways in the whole network (way too much effort)
		// *todo*: Vertices in eine Array Liste schreiben<- Position in Array
		// Liste = index
		int dim = vertices.size();
		int[] distance = new int[dim];
		int[] parentIndex = new int[dim];
		boolean[] isReady = new boolean[dim];
		final int MAXVALUE = 99999;
		int startIndex = 0;
		String vertex = "";
		String curVertex = "";
		int edgeWeight = 0;
		int index = 0;
		boolean foundRoute=false;
		//int countAdditionalSteps =0;
		
		//form string lists for convenience
		LinkedList<String> destinationByName = new LinkedList<String>();
		for (int i=0; i<destination.size(); i++){
			destinationByName.add(destination.get(i));
		}

		// find index of current position within arrayList vertices
		for (String b : vertices) {
			if (b.equals(position))
				break;
			startIndex += 1;
		}
		
		//debugging
		if (startIndex==vertices.size()){
			System.out.println("--------DEBUGGING----------------");
			System.out.println("-----pos:"+position+"----------------");
			for (int y = 0; y < dim; y++) {
				System.out.println("--------"+vertices.get(y)+"----------------");
			}
		}

		// initialize array distance and array isReady
		for (int y = 0; y < dim; y++) {
			isReady[y] = false;
			vertex = vertices.get(y);
			edgeWeight = getEdgeWeight(edges, vertices, position, vertex, useEdgeWeight);
			if (edgeWeight > 0) {
				// there is an edge between position and vertex
				distance[y] = edgeWeight;
				parentIndex[y] = startIndex;
			} else {
				distance[y] = MAXVALUE;
				parentIndex[y] = -1;
			}
		}

		// mark starting position as ready
		isReady[startIndex] = true;
		distance[startIndex] = 0;

		// main loop of dijkstra algorithm
		for (int count = 1; count < dim; count++) {
			
			if (count>MAXSEARCH)
				break;
			// find vertex with shortest distance from position
			index = getMin(distance, isReady, dim);
			if (index>-1){
			//we have found a vertex
				
				curVertex = vertices.get(index);
				
				//are we finished?
				if (destinationByName.contains(curVertex)){
					foundRoute=true;
					break;
				}

				// mark the vertex with the upper index as ready
				isReady[index] = true;

				// update the array distance
				for (int y = 0; y < dim; y++) {
					if (!isReady[y]) {
						vertex = vertices.get(y);
						edgeWeight = getEdgeWeight(edges, vertices, curVertex, vertex, useEdgeWeight);
						// System.out.println(curVertex + " " + vertex + " " +
						// String.valueOf(edgeWeight));
						if (edgeWeight > 0
								&& (edgeWeight + distance[index] < distance[y])) {
							distance[y] = edgeWeight + distance[index];
							parentIndex[y] = index;
						}//if
					}// if
				}// if
			}// for
		}// for
		
		ArrayList<LogicGoal> nextroute = new ArrayList<LogicGoal>();
		// setGoals
		if (foundRoute==false){
			//no route could be found
			return nextroute;
		}//if

		else{
			while (index != startIndex) {
				vertex = vertices.get(index);
				nextroute.add(new LogicGoal(Const.GOTO, vertex));
				index = parentIndex[index];
			}//while
			return nextroute;
		}//else

	}// findNextRoute

	/*********************************************************
	 * @brief help function for dijkstra algorithm
	 * returns the index of the nearest neighbor of a ready node
	 * @param dimension
	 *            of vertices array
	 * @return vertex which is not yet processed nearest to the origin
	 * @see findnextRoute()
	 *********************************************************/
	public static int getMin(int[] distance, boolean[] isReady, int dim) {

		int nextVertexIndex = -1;
		int min = 99999;
		for (int y = 0; y < dim; y++) {
			if (isReady[y] == false) {
				if (distance[y] < min) {
					min = distance[y];
					nextVertexIndex = y;
				}
			}
		}// for
		return nextVertexIndex;
	}// getMin

	/*********************************************************
	 * @brief help function for dijkstra algorithm
	 * @param all
	 *            edges known to the agent, 2 vertices which supposedly form an
	 *            edge
	 * @return 0 if the edge does not exist/is unknown to agent, else weight of
	 *         the edge
	 * @see findnextRoute()
	 *********************************************************/
	public int getEdgeWeight(LinkedList<LogicBelief> edges,LinkedList<String> verticesByName, String origin,
			String destination, boolean useEdgeWeight) {
		// TODO Florian updates with Holger
		String param1 = "";
		String param2 = "";

		//check if we may use the vertex
		if(verticesByName.contains(destination)){
			//search for a direct edge between the two nodes
			for (LogicBelief b : edges) {
				param1 = b.getParameters().get(0).toString();
				param2 = b.getParameters().get(1).toString();
				if ((param1.equals(origin) && param2.equals(destination))
						|| (param2.equals(origin) && param1.equals(destination))) {
					//the two nodes are incident to each other
					if (!useEdgeWeight)
						return 1;
					else if (b.getPredicate().equals(Const.EXPLOREDEDGE)) {
					return Integer.parseInt(b.getParameters().get(2));
					} 
					else if (b.getPredicate().equals(Const.UNEXPLOREDEDGE)) {
						return unexploredEdgeWeight;
					}
				}// if
			}// for
		}//if
		//there exists no direct edge
		return 0;
	}// getEdgeWeight
	
	
	/*********************************************************
	 * extracts the agents ID out of his name
	 * precondition: agentName := 'name' + 'ID', |name|>0
	 * @param name of the agent as String
	 * @return ID of the agent
	 * @see sendMessagestoUnseenAgents
	 *********************************************************/
	public int getAgentID(String agentName){
		
		int agentID =-1;
		int length=agentName.length();
		String vorletztesElement = String.valueOf(agentName.charAt(length-2));
		if (isInt(vorletztesElement))
		    agentID = Integer.parseInt(agentName.substring(length-2));
		else
			agentID =Integer.parseInt(agentName.substring(length-1));
		return agentID;			
	}//getAgentID
	
	
	/*********************************************************
	 * @brief checks whether a String is an Integer
	 * @param String to test
	 * @return boolean
	 * @see getAgentID
	 *********************************************************/
	public boolean isInt(String i){
		try
		{
				Integer.parseInt(i);
				return true;
		}
		catch(NumberFormatException nfe)
		{
			return false;
		}
	}//isInt
	
	
	/*********************************************************
	 * @brief search for a zone which fulfills the conditions in the
	 * parameter list
	 * @param max size of the zones border
	 * max size of the zone itself (necessary to limit the search)
	 * name of the vertex around which the zone is established,
	 * edges and vertices of the map, topology information
	 * @return borderline of the zone, inner part of the zone
	 * @see initialozeZone
	 *********************************************************/
	//precondition: all edges and nodes are explored/ probed
	//idea: "lines" are no zones, the advantage of zones is its interior part
	public LinkedList<String> findZone(int borderSizeMAX, int MAXZONESIZE, String startVertexName, LinkedList<LogicBelief> vertices, LinkedList<LogicBelief> edges){
		
		ArrayList<String> innerZone = new ArrayList<String>();
		ArrayList<String> borderline = new ArrayList<String>();
		ArrayList<String> directNeighborsOfInnerZone = new ArrayList<String>();
		boolean shiftAllowed = true;
		
		innerZone.add(startVertexName);
		
		//all vertices are allowed in routes on borderline except interior
		LinkedList<String> allowedVertices = new LinkedList<String>();
		for (int i=0; i<vertices.size(); i++){
			if(!vertices.get(i).getParameters().get(0).equals(startVertexName))
				allowedVertices.add(vertices.get(i).getParameters().get(0));
		}
		
		//get all neighbors of startNode, these are the first components of the zone's borderline
		for (LogicBelief b : edges){
			if (b.getParameters().get(0).equals(startVertexName))
				borderline.add(b.getParameters().get(1).toString());
			    //directNeighborsOfInnerZone
			else if (b.getParameters().get(1).equals(startVertexName))
				borderline.add(b.getParameters().get(0).toString());
		}//for

	    //find the connections between the nodes of the borderline
	    //the borderline then completely encircles the startNode
		
		//calculate the "distance" in terms of nodes from each to each node of the borderline
		
		//Solve TSP with nodes:=vertices on borderline, edge weight=distance
		//make use of the topology of the map, i.e. there are no crossing edges
		//if their is a direct connection between two nodes on the borderline, use it
		//TODO else use minimum distance?!
		//start with first index in borderline
		int currentVertexIndex=0;
		ArrayList<String> completeBorderline = new ArrayList<String>();
		ArrayList<LogicGoal> route = new ArrayList<LogicGoal>();
        String currentNode="";
		
		//fill destinations, all but first element
		LinkedList<String> possibleDestinations = new LinkedList<String>();
		for (int i=1; i<borderline.size(); i++){
			possibleDestinations.add(borderline.get(i));
		}
		
		
		//main loop of TSP
		
		//start with first element
		currentNode=borderline.get(0).toString();
		completeBorderline.add(currentNode);
		allowedVertices.remove(currentNode);

		
		for (int count=0; count<borderline.size()-1; count++){

			//DEBUG
			//System.out.print("; " + currentNode);
			allowedVertices.add(currentNode);
			route= findRoute(currentNode,
			       possibleDestinations, allowedVertices, edges, false,20);
			allowedVertices.remove(currentNode);
			//dist[from][to]=route.size()-1;
			//update completeBorderline and Value of Zone
	        
	        //if no route could be found, close the circle on the other side;
			//UPDATE: if route too long, close circle on the other side;
			//CAREFUL: this can only be done once!!
	        if (route.isEmpty() || route.size()>MAXZONECONNECTION){
	        	if (shiftAllowed){
	        		System.out.println("DEBUG: need a first shift");
	        		shiftAllowed =false;
	        		currentNode = borderline.get(0).toString();
	        		allowedVertices.add(currentNode);
	        		route= findRoute(currentNode,
	        				possibleDestinations, allowedVertices, edges, false,20);
	        		allowedVertices.remove(currentNode);
	        		
	        		//DEBUG
	        		System.out.print("Route DEBUG:");
	        		for(int i=route.size()-1; i>-1;i--){
	        			System.out.print(", " + borderline.get(i));
	        		}
	        		System.out.println();
	        	}
	        	else{
	        		System.out.println("DEBUG: need a secondShift!");
	        		break;
	        	}//else
	        	
	        }//if
	        
	        for (int i=route.size()-1; i>-1;i--){
	        	
	        	currentNode=route.get(i).getParameters().get(0).toString();
	        	if (!shiftAllowed){
	        		//attach from  front
	        		completeBorderline.add(0,currentNode);
	        	}else
	        		completeBorderline.add(currentNode);

	        }//for
	        //delete destination node from possible destinations
	        possibleDestinations.remove(currentNode);
		}//for
		
		
		//get route from last to first element
		String lastDestination="";
		if (!shiftAllowed){
			lastDestination =completeBorderline.get(completeBorderline.size()-1);
		}
		else{
			lastDestination =completeBorderline.get(0);
			}
		allowedVertices.add(lastDestination);
		allowedVertices.add(currentNode);
		possibleDestinations.add(lastDestination);
		
    	route= findRoute(currentNode,
    				possibleDestinations, allowedVertices, edges, false,20);
    	allowedVertices.add(lastDestination);
    	allowedVertices.remove(lastDestination);
    	allowedVertices.remove(currentNode);
    	
    	   for (int i=route.size()-1; i>0;i--){
	        	
	        	currentNode=route.get(i).getParameters().get(0).toString();
	        	allowedVertices.remove(currentNode);
	        	if (!shiftAllowed){
	        		//attach from the front
	        		completeBorderline.add(0,currentNode);
	        	}else
	        		completeBorderline.add(currentNode);
    	   }
		
		
		//DEBUG
		System.out.print("DEBUG: start vertices :");
		for(int i=0; i<completeBorderline.size();i++){
			System.out.print(", " + completeBorderline.get(i));
		}
		System.out.println();
		
		//it may not be too small..
		if (completeBorderline.size()<4)
			return null;
		
		//.. or too big..
		if (borderSizeMAX<completeBorderline.size())
			//the zone cannot be build
			return null;
		
		//.. otherwise deformed zones are calculated
		
		//STEP 2
		//enlarge the zone?
		
		else {
		//try to enlarge the zone as long as possible
		boolean improved=true;
		while ((improved) && (innerZone.size()+completeBorderline.size())<MAXZONESIZE){
			
			possibleDestinations.clear();
			String destination="";
			String jumpOver="";
			improved=false;
		
			//try to put each neighbor of the inner zone in the innerZone
			for (int neighborIndex=0; neighborIndex<completeBorderline.size(); neighborIndex++){
				//find a way around this neighbor
				currentNode=completeBorderline.get(neighborIndex);
				//jump over the next neighbor
				if (neighborIndex==completeBorderline.size()-2){
					destination=completeBorderline.get(0);
					jumpOver = completeBorderline.get(completeBorderline.size()-1);
				}
				else if (neighborIndex==completeBorderline.size()-1){
					destination=completeBorderline.get(1);
					jumpOver = completeBorderline.get(0);
				}
				else{
					destination=completeBorderline.get(neighborIndex+2);
					jumpOver = completeBorderline.get(neighborIndex+1);
				}	
			     
				possibleDestinations.clear();
				possibleDestinations.add(destination);
				
				allowedVertices.add(currentNode);
				allowedVertices.add(destination);
				route= findRoute(currentNode,
						possibleDestinations, allowedVertices, edges, false,20);
				allowedVertices.remove(currentNode);
				allowedVertices.remove(destination);
			
				//evaluate the route; has another borderElement been used, delete it?
				//if no route can be found, just go to the next node
				//UPDATE: does that not mean, that origin/destination is on the fringe of the map?
				if (route.size()==0){
					LinkedList<String> outOfZone = ConnectionsOutOfZone(currentNode, allowedVertices, edges);
					if (outOfZone.isEmpty()){
						//add current Node to innerZone
						innerZone.add(currentNode);
						completeBorderline.remove(currentNode);
						//DEBUG
						System.out.print("removed cause outofZone" + currentNode + " :");
						improved=true;
						//remake order of borderline
						for (int i=completeBorderline.size()-1; i>=neighborIndex;i--){
							String element =completeBorderline.get(completeBorderline.size()-1);
							completeBorderline.remove(element);
							completeBorderline.add(0, element);
						}
						//DEBUG
						//System.out.println();
						//for(int i=0; i<completeBorderline.size();i++){
							//System.out.print(", " + completeBorderline.get(i));
						//}
						//System.out.println();
						//check if zone is now too small
						if(completeBorderline.size()<4)
							return null;
						break;
						
					}
					outOfZone = ConnectionsOutOfZone(destination, allowedVertices, edges);
					if (outOfZone.isEmpty()){
						//add destination to innerZone
						innerZone.add(destination);
						completeBorderline.remove(destination);
						improved=true;
						//DEBUG
						System.out.print("removed cause outofZone" + destination + " :");
						//remake order of borderline
						for (int i=completeBorderline.size()-1; i>neighborIndex+1;i--){
							String element =completeBorderline.get(completeBorderline.size()-1);
							completeBorderline.remove(element);
							completeBorderline.add(0, element);
						}
						//DEBUG
						//System.out.println();
						//for(int i=0; i<completeBorderline.size();i++){
							//System.out.print(", " + completeBorderline.get(i));
						//}
						//System.out.println();
						//check if zone is now too small
						if(completeBorderline.size()<4)
							return null;
						break;
					}
						
				}
				//if direct connection, use it whatsoever
				else if (route.size()==1){
					LinkedList<String> outOfZone=ConnectionsOutOfZone(jumpOver, allowedVertices, edges);
					if (outOfZone.isEmpty()){
						if (neighborIndex<completeBorderline.size()-2){
							//add to inner Zone
							innerZone.add(jumpOver);
							//DEBUG
							System.out.println("inserted directly:" + jumpOver);
							//change completeBorderline
							completeBorderline.remove(jumpOver);	
							improved=true;
							//check if zone is now too small
							if(completeBorderline.size()<4)
								return null;
							break;
						}
					}
				}
				else if (route.size()<MAXZONECONNECTION){
				   if (completeBorderline.size()+route.size()-1<=borderSizeMAX){
					  //add to inner Zone
					  innerZone.add(jumpOver);
					//DEBUG
						System.out.print("inserted with addition :" + jumpOver +  ": ");
					  int insertIndex=completeBorderline.indexOf(jumpOver);
					  completeBorderline.remove(jumpOver);
					  //rebuild zone
					  for (int i=route.size()-1; i>0;i--){
				        	//?
				        	currentNode=route.get(i).getParameters().get(0).toString();
				        	completeBorderline.add(insertIndex,currentNode);
				        	allowedVertices.remove(currentNode);
				        	insertIndex++;
						}//for
					  improved=true;
					 
						//DEBUG
						//for(int i=0; i<completeBorderline.size();i++){
							//System.out.print(", " + completeBorderline.get(i));
						//}
						System.out.println();
						break;
					  
				   }//if
				}//else
				else if (route.size()>2){
					//check whether we can enlarge along the fringe 
					//of the map
					//ConnectionsOutOfZone(String checkedVertex, LinkedList<String> innerZone, LinkedList<String> completeBorderline ,LinkedList<LogicBelief> vertices, LinkedList<LogicBelief> edges)
				}
			}//for
			
		}//while
	}//else
		
		
	//return the zone	
	LinkedList<String> compressedZone= new LinkedList<String>();
	int index=0;
	
	while (index< completeBorderline.size()){
		compressedZone.add(completeBorderline.get(index));
		index +=1;
	}
	compressedZone.add("interiorNOW");
	index=0;
	while (index< innerZone.size()){
		compressedZone.add(innerZone.get(index));
		index +=1;
	}
	System.out.println( compressedZone.size());
	return compressedZone;	
	
	}//findZone
	
	
	/*********************************************************
	 * @brief return the neighbors of the respective vertex
	 * which are not in the zone
	 * @param respective vertex, vertices not in the zone,
	 * all edges of the map
	 * @return all neighbors of the vertex not in a zone
	 * @see findZone
	 *********************************************************/
	public LinkedList<String> ConnectionsOutOfZone(String checkedVertex, LinkedList<String>allowedVertices , LinkedList<LogicBelief> edges){
		
		//get the direct neighbors of the checkedVertex, return those who are not involved with the zone
		LinkedList<String> directNeighbors = new LinkedList<String>();
		LinkedList<String> outOfZone = new LinkedList<String>();
		
		for (LogicBelief b: edges){
			if (b.getParameters().get(0).equals(checkedVertex)){
				directNeighbors.add(b.getParameters().get(1));
			}
			else if (b.getParameters().get(1).equals(checkedVertex)){
				directNeighbors.add(b.getParameters().get(0));
			}
		}
		for (String b: directNeighbors){
			if (allowedVertices.contains(b))
				outOfZone.add(b);
		}
		return outOfZone;
		
	}//ConnectionsOutOfZone

	/**
	 * Maps a goal to a belief.
	 * @param goal
	 * @return belief
	 */
	public LogicBelief goalToBelief(LogicGoal goal) {
						
		return new LogicBelief(goal.getPredicate()
				,goal.getParameters());	
	}

}
