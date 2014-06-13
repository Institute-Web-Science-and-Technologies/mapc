// Agent baseAgent in project mako
{ include("actions.goto.asl") }
{ include("actions.repair.asl") }
{ include("actions.recharge.asl") }

/* Initial beliefs and rules */

/* Initial goals */
!start.

+myPosition(unknown)[source(self)].
+myName(MyName)[source(percept)]: true <- .print("My Name is ", MyName).
+health(MyH)[source(percept)]: MyH > 0 <- .print("My Health is ", MyH).

+position(Vertex)[source(percept)]:
   .my_name(MyName)
    <-
    -+myPosition(CurrentVertex);
    internalActions.updateTeamAgentPosition(MyName, Vertex).

+visibleEdge(VertexA, VertexB)[source(percept)]:
   true
   <-
   internalActions.addEdge(VertexA, VertexB).

+surveyedEdge(VertexA, VertexB, Weight)[source(percept)]:
   true
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

+visibleVertex(Vertex, Team)[source(percept)]:
    true
    <-
    internalActions.addVertex(Vertex, Team).

+probedVertex(Vertex, Value)[source(percept)]:
    true
    <-
    internalActions.addVertex(Vertex, Value).

/* Plans */
+!start <- .my_name(AgentName).
