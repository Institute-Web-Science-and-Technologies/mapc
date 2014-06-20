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
   

+step(Step)[source(self)] 
	<- !walkAround.
    

/* Plans */

+!walkAround: lowEnergy
    <- .print("My energy is low, going to recharge.");
        recharge;
        -lowEnergy.

+!walkAround: position(Vertex)[source(percept)] & internalActions.isVertexSurveyed(Vertex) & internalActions.getBestUnexploredVertex(Vertex, NextVertex)
    <- .print("Surveyed ", Vertex, " going to ", NextVertex);
    	goto(NextVertex).
   
+!walkAround: position(Vertex)[source(percept)] & internalActions.isVertexUnsurveyed(Vertex)
	<- .print("Not surveyed ", Vertex);
		survey.
	