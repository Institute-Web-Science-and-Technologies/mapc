// Agent baseAgent in project mako
{ include("actions.goto.asl") }
{ include("actions.repair.asl") }
{ include("actions.recharge.asl") }

/* Initial beliefs and rules */
lowEnergy :- energy(Energy)[source(percept)] & Energy < 8.

/* Initial goals */

/* Events */
+myName(Name)[source(percept)]
    <- .print("My Server Name is: ", Name);
    	.my_name(JName);
       .print("My Jason Name is: ", JName).
    
+health(Health)[source(percept)]:
    Health > 0
    <- .print("My Health is ", Health).

+energy(Energy)[source(percept)]
	<- .print("My Energy is ", Energy).

+position(Vertex)[source(percept)]
    <- .my_name(Name); 
    	internalActions.updateTeamAgentPosition(Name, Vertex);
       .send(cartographer, tell, position(Name, Vertex)).

+visibleEdge(VertexA, VertexB)[source(percept)]
    <- internalActions.addEdge(VertexA, VertexB);
       .send(cartographer, tell, edge(VertexA, VertexB, 1000)).

+surveyedEdge(VertexA, VertexB, Weight)[source(percept)]
    <- internalActions.addEdge(VertexA, VertexB, Weight);
       .print("Surveyed Edge: ", VertexA, " -> ", VertexB, " Value: ", Weight);
       .send(cartographer, tell, edge(VertexA, VertexB, Weight)).

+edges(AmountEdges)[source(percept)]
    <- internalActions.setGlobalEdgesAmount(AmountEdges).

+vertices(AmountVertices)[source(percept)]
    <- internalActions.setGlobalVerticesAmount(AmountVertices).

+probedVertex(Vertex, Value)[source(percept)]
    <- internalActions.addVertex(Vertex, Value);
       .send(cartographer, tell, probed(Vertex, Value)).
    
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
        recharge;
        -lowEnergy.

+!walkAround: position(Vertex)[source(percept)] & internalActions.isVertexSurveyed(Vertex) & internalActions.getBestUnexploredVertex(Vertex, NextVertex)
    <- .print("Surveyed ", Vertex, " going to ", NextVertex);
    	goto(NextVertex).
   
+!walkAround: position(Vertex)[source(percept)] & internalActions.isVertexUnsurveyed(Vertex)
	<- .print("Not surveyed ", Vertex);
		survey.
	