// Agent baseAgent in project mako
{ include("actions.goto.asl") }
{ include("actions.repair.asl") }
{ include("actions.recharge.asl") }

/* Initial beliefs and rules */
lowEnergy :- energy(E)[source(percept)] & E < 8.

/* Initial goals */

/* Events */

//Print all received beliefs. Used for debugging. (comment this out if your simulation crashes immediately)
@debug[atomic] +Belief <-
	.print("Received new belief from percept: ", Belief);
	for (B) {
		.print("	When ", Belief, " is added, this is another belief in the belief base: ", B);
	}
	-Belief;
	.print("		Removed ", Belief, " from belief base.").

+myName(MyName)[source(percept)]
    <- .print("My Name is ", MyName).
    
+health(MyH)[source(percept)]:
    MyH > 0
    <- .print("My Health is ", MyH).

+position(Vertex)[source(percept)]
    <-
    .my_name(MyName);
    internalActions.updateTeamAgentPosition(MyName, Vertex).

+visibleEdge(VertexA, VertexB)[source(percept)]
   <-
   internalActions.addEdge(VertexA, VertexB).

+surveyedEdge(VertexA, VertexB, Weight)[source(percept)]
    <-
    internalActions.addEdge(VertexA, VertexB, Weight).

+edges(AmountEdges)[source(percept)]
    <-
    internalActions.setGlobalEdgesAmount(AmountEdges).

+vertices(AmountVertices)[source(percept)]:
    internalActions.setGlobalVerticesAmount(AmountVertices).

+probedVertex(Vertex, Value)[source(percept)]
    <-
    internalActions.addVertex(Vertex, Value).
    
+simStart 
    <- .print("Simulation started."). 
//   !start.
   
+visibleVertex(Vertex, Team)[source(percept)] <-
    internalActions.addVertex(Vertex, Team).

+step(S)[source(percept)] <-
    //.print("Current step is ", S);
    !walkAround.
    

/* Plans */
//+!start <- survey.

+!walkAround: 
    has_low_energy
    <-
    .my_name(MyName);
    .print("My energy is low, going to recharge.");
    recharge(MyName).

+!walkAround: 
    position(Vertex)[source(percept)] & internalActions.isVertexSurveyed(Vertex) & internalActions.getBestUnexploredVertex(Vertex, NextVertex)
    <- 
    .print("Surveyed ", Vertex, " going to ", NextVertex);
    goto(NextVertex).
   
+!walkAround:
    position(Vertex)[source(percept)]
	<- 
	.print("Not surveyed ", Vertex); survey.
	
	
/* Rules */
has_low_energy
    :-
    energy(EnergyValue) & EnergyValue < 5.