// Agent baseAgent in project mako
{ include("actions.goto.asl") }
{ include("actions.repair.asl") }
{ include("actions.recharge.asl") }

/* Initial beliefs and rules */
lowEnergy :- energy(E)[source(percept)] & E<5.

/* Initial goals */

+myName(MyName)[source(percept)]: true <- .print("My Name is ", MyName).
+health(MyH)[source(percept)]: MyH > 0 <- .print("My Health is ", MyH).

+position(Vertex)[source(percept)]:
   .my_name(MyName)
    <- internalActions.updateTeamAgentPosition(MyName, Vertex).

+visibleEdge(VertexA, VertexB)[source(percept)]:
   true
   <-
   internalActions.addEdge(VertexA, VertexB).

+surveyedEdge(VertexA, VertexB, Weight)[source(percept)]
   <-
   internalActions.addEdge(VertexA, VertexB, Weight); !walkAround;
   .print("Kante von ", VertexA, " nach ", VertexB, " Gewicht: ", Weight).

+edges(AmountEdges)[source(percept)]:
   true
   <-
   internalActions.setGlobalEdgesAmount(AmountEdges).

+vertices(AmountVertices)[source(percept)]:
   true
   <-
   internalActions.setGlobalVerticesAmount(AmountVertices).

+visibleVertex(Vertex, Team)[source(percept)]:
    true
    <-
    internalActions.addVertex(Vertex, Team).

+probedVertex(Vertex, Value)[source(percept)]:
    true
    <-
    internalActions.addVertex(Vertex, Value).
    
+lowEnergy:
   .my_name(MyName)
   <- recharge(MyName).

+step(S):
    true
<- 
    .print("Current step is ", S);
    !walkAround.
    

/* Plans */
+!start <- survey.

+!walkAround: 
   position(Vertex) & internalActions.isVertexSurveyed(Vertex) & internalActions.getBestUnexploredVertex(Vertex, NextVertex)
   <- 
   .print("Surveyed ", Vertex, " going to ", NextVertex);
   goto(NextVertex).
   
+!walkAround:  position(Vertex)
	 <- 
	 .print("Not surveyed ", Vertex); survey.
