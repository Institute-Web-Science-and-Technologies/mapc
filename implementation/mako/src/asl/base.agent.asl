// Agent baseAgent in project mako
{ include("actions.goto.asl") }
{ include("actions.repair.asl") }
{ include("actions.recharge.asl") }

/* Initial beliefs and rules */
lowEnergy :- energy(E)[source(percept)] & E < 8.

/* Initial goals */

/* Plans */

//Print all received beliefs. Used for debugging.
@debug[atomic] +Belief <-
	.print("Received new belief from percept: ", Belief);
	for (B) {
		.print("	When ", Belief, " is added, this is another belief in the belief base: ", B);
	}
	-Belief;
	.print("		Removed ", Belief, " from belief base.").

+myName(MyName)[source(percept)]: true <- .print("My Name is ", MyName).
+health(MyH)[source(percept)]: MyH > 0 <- .print("My Health is ", MyH).

+position(Vertex)[source(percept)]:
   .my_name(MyName)
    <- internalActions.updateTeamAgentPosition(MyName, Vertex);
    -+position(Vertex)[source(self)].

+visibleEdge(VertexA, VertexB)[source(percept)]:
   true
   <-
   internalActions.addEdge(VertexA, VertexB).

+surveyedEdge(VertexA, VertexB, Weight)[source(percept)]
   <-
   internalActions.addEdge(VertexA, VertexB, Weight).

+edges(AmountEdges)[source(percept)]:
   true
   <-
   internalActions.setGlobalEdgesAmount(AmountEdges).

+vertices(AmountVertices)[source(percept)]:
   true
   <-
   internalActions.setGlobalVerticesAmount(AmountVertices).

+probedVertex(Vertex, Value)[source(percept)]:
    true
    <-
    internalActions.addVertex(Vertex, Value).
    
+lowEnergy:
   .my_name(MyName)
   <- recharge(MyName).
   
+simStart 
   <- .print("Simulation started."). 
//   !start.
   
+visibleVertex(Vertex, Team)[source(percept)] <-
    internalActions.addVertex(Vertex, Team).

+step(S) <-
    //.print("Current step is ", S);
    !walkAround.
    

/* Plans */
//+!start <- survey.

+!walkAround: 
   lowEnergy
   <-
   .print("My energy is low, going to recharge.");
   recharge.

+!walkAround: 
   position(Vertex)[source(self)] & internalActions.isVertexSurveyed(Vertex) & internalActions.getBestUnexploredVertex(Vertex, NextVertex)
   <- 
   .print("Surveyed ", Vertex, " going to ", NextVertex);
   goto(NextVertex).
   
+!walkAround: position(Vertex)[source(self)]
	 <- 
	 .print("Not surveyed ", Vertex); survey.
