// Agent baseAgent in project mako
{ include("actions.goto.asl") }
{ include("actions.repair.asl") }
{ include("actions.recharge.asl") }
{ include("storeBeliefs.asl") }


//Print all received beliefs. Used for debugging. (comment this out if your simulation crashes immediately)
//@debug[atomic] +Belief <-
//	.print("Received new belief from percept: ", Belief);
//	for (B) {
//		.print("	When ", Belief, " is added, this is another belief in the belief base: ", B);
//	}
//	-Belief;
//	.print("		Removed ", Belief, " from belief base.").
	

/* Initial beliefs and rules */
lowEnergy :- energy(Energy)[source(percept)] & Energy < 8.

/* Initial goals */

/* Events */
    
+position(Vertex)[source(percept)]
    <- .my_name(Name); 
    	internalActions.updateTeamAgentPosition(Name, Vertex);
       .send(cartographer, tell, position(Name, Vertex));
       .print("New position(", Vertex, ")");
	    -+position(Vertex)[source(self)].

+visibleEdge(VertexA, VertexB)[source(percept)]
    <- internalActions.addEdge(VertexA, VertexB);
       .send(cartographer, tell, edge(VertexA, VertexB, 1000)).

+surveyedEdge(VertexA, VertexB, Weight)[source(percept)]
    <- internalActions.addEdge(VertexA, VertexB, Weight);
       .print("Surveyed Edge: ", VertexA, " -> ", VertexB, " Value: ", Weight);
       .send(cartographer, tell, edge(VertexA, VertexB, Weight)).

+edges(AmountEdges)[source(percept)]
    <- internalActions.setGlobalEdgesAmount(AmountEdges). //TODO: send to cartographer

+vertices(AmountVertices)[source(percept)]
    <- internalActions.setGlobalVerticesAmount(AmountVertices).

+probedVertex(Vertex, Value)[source(percept)]
    <- internalActions.addVertex(Vertex, Value);
       .send(cartographer, tell, probed(Vertex, Value));
        .print("New probedVertex(", Vertex, " ", Value, ")");
	    -+probedVertex(Vertex, Value)[source(self)].
	    
+visibleVertex(Vertex, Team)[source(percept)] 
	<- internalActions.addVertex(Vertex, Team).

//TODO: visibleEntity, zoneScore
    
+simStart 
    <- .print("Simulation started."). 
   

+step(Step)[source(percept)] 
	<- .print("Current step is ", Step);
	.send(cartographer, askAll, surveyed(Vertex));
    !walkAround.
    

/* Plans */

+!getNextVertex: position(Vertex)[source(percept)] & surveyedEdge(X, Y, Z) & X==Vertex
	<- .findall(Cost,surveyedEdge(Vertex, Y, Cost),List);
	   //.findall(Cost2,surveyedEdge(Y2, Vertex, Cost2), List2);
	   //.concat(List,List2,ConcatedList);
		.min(List, MinValue);
		.print("Minimum Cost: ",MinValue);
		?surveyedEdge(Vertex, NextNode, MinValue);
		//?surveyedEdge(NextNode, Vertex, MinValue);
		.print("Going to ",NextNode," which has cost of ",MinValue);
		goto(NextNode);
		.print("went to node ", NextNode).
		
+!getNextVertex: position(Vertex)[source(percept)] & surveyedEdge(X, Y, Z) & Y==Vertex
	<- .findall(Cost2,surveyedEdge(Y2, Vertex, Cost2), List2);
    	.min(List2, MinValue2);
    	.print("Minimum Cost (First Plan fails): ",MinValue2);
    	?surveyedEdge(NextNode2, Vertex, MinValue2);
    	.print("Going to ",NextNode2," which has cost of ",MinValue2);
		goto(NextNode2);
		.print("went to node ", NextNode2).

+!walkAround: energy(E)[source(percept)] & E<10
    <- .print("My energy is low, going to recharge.");
        recharge;
        -energy(E)[source(percept)].

+!walkAround: position(Vertex)[source(percept)] & surveyed(Vertex)
    <- .print("Surveyed ", Vertex, ". Now getNextVertex ");
    	!getNextVertex.
    	//goto(NextVertex).
   
+!walkAround: position(Vertex)[source(percept)] & not surveyed(Vertex)
	<- .print("Not surveyed ", Vertex);
		survey;
		.send(cartographer, tell, surveyed(Vertex)).
	