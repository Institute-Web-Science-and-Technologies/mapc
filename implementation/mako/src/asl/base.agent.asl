// Agent baseAgent in project mako
{ include("actions.goto.asl") }
{ include("actions.repair.asl") }
{ include("actions.recharge.asl") }

/* Initial beliefs and rules */
lowEnergy :- energy(Energy)[source(percept)] & Energy < 8.

/* Initial goals */

/* Events */
+myName(Name)[source(percept)]
    <- .print("My Name is ", Name).
    
+health(Health)[source(percept)]:
    Health > 0
    <- .print("My Health is ", Health).

+position(Vertex)[source(percept)]
    <- internalActions.updateTeamAgentPosition(.my_name(Name), Vertex).

+visibleEdge(VertexA, VertexB)[source(percept)]
   <- internalActions.addEdge(VertexA, VertexB).

+surveyedEdge(VertexA, VertexB, Weight)[source(percept)]
    <- internalActions.addEdge(VertexA, VertexB, Weight).

+edges(AmountEdges)[source(percept)]
    <- internalActions.setGlobalEdgesAmount(AmountEdges).

+vertices(AmountVertices)[source(percept)]
    <- internalActions.setGlobalVerticesAmount(AmountVertices).

+probedVertex(Vertex, Value)[source(percept)]
    <- internalActions.addVertex(Vertex, Value).
    
+simStart 
    <- .print("Simulation started."). 
//   !start.
   
+visibleVertex(Vertex, Team)[source(percept)] 
	<- internalActions.addVertex(Vertex, Team).

+step(Step)[source(percept)] 
	<- .print("Current step is ", Step);
    !walkAround.
    

/* Plans */
//+!start <- survey.

+!walkAround: lowEnergy
    <- .print("My energy is low, going to recharge.");
        recharge.

+!walkAround: position(Vertex)[source(percept)] & internalActions.isVertexSurveyed(Vertex) & internalActions.getBestUnexploredVertex(Vertex, NextVertex)
    <- .print("Surveyed ", Vertex, " going to ", NextVertex);
    	goto(NextVertex).
   
+!walkAround: position(Vertex)[source(percept)] & ~internalActions.isVertexSurveyed(Vertex)
	<- .print("Not surveyed ", Vertex);
		survey.
	