//author: sewell
//author: daudrich
@surveyedEdgeFromSelf[atomic]	
+surveyedEdge(VertexA, VertexB, Weight)[source(percept)]:
	not surveyedEdge(VertexA, VertexB, Weight)[source(self)]
	& broadcastAgentList(AgentList)
	<-
	.print("Received percept surveyedEdge(", VertexA, ", ", VertexB, ", ", Weight, ").");
//	.print("I learned about a surveyed edge ", VertexA, " to ", VertexB, " with weight ", Weight, ".");
	.send(AgentList, tell, surveyedEdge(VertexA, VertexB, Weight));
	.abolish(surveyedEdge(VertexA, VertexB, Weight));
	.abolish(surveyedEdge(VertexB, VertexA, Weight));
	+surveyedEdge(VertexA, VertexB, Weight)[source(self)];
	+surveyedEdge(VertexB, VertexA, Weight)[source(self)].

@surveyedEdgeFromOthers[atomic]	
+surveyedEdge(VertexA, VertexB, Weight)[source(Agent)]:
	not surveyedEdge(VertexA, VertexB, Weight)[source(self)]
	<-
//	.print("I learned about a surveyed edge ", VertexA, " to ", VertexB, " with weight ", Weight, " from agent ", Agent, ".");
	.abolish(surveyedEdge(VertexA, VertexB, Weight));
	.abolish(surveyedEdge(VertexB, VertexA, Weight));
	+surveyedEdge(VertexA, VertexB, Weight)[source(self)];
	+surveyedEdge(VertexB, VertexA, Weight)[source(self)].
	
+surveyedEdge(VertexA, VertexB, Weight)[source(Agent)]:
	surveyedEdge(VertexA, VertexB, Weight)[source(self)]
	& Agent \== self
	<-
//	.print("I already knew about the surveyed edge ", VertexA, " to ", VertexB, " with weight ", Weight, ".");
	-surveyedEdge(VertexA, VertexB, Weight)[source(Agent)].

+!doExploring:
	position(Vertex)
	<-
	.send(cartographer, askOne, unsurveyedNeighbours(Vertex, _), unsurveyedNeighbours(_, UnsurveyedNeighbours));
	.print("I am currently on vertex ", Vertex, ". My unsurveyed neighbours are: ", UnsurveyedNeighbours);
	!chooseNextVertex(UnsurveyedNeighbours).
	
//If all of our neighbours have been surveyed, we need to look at nodes that are
//not our neighbours.
+!chooseNextVertex([]):
	position(CurrentVertex)
	<-
	.send(cartographer,askOne,unsurveyedVertices(_), unsurveyedVertices(UnsurveyedVertices));
	.send(CurrentVertex, askOne, getClosestVertexFromList(UnsurveyedVertices, _), getClosestVertexFromList(_, ClosestUnsurveyedVertex));
	.print("The closest node from the list of unsurveyed nodes ", UnsurveyedVertices, " is ", ClosestUnsurveyedVertex, ".");
	!goto(ClosestUnsurveyedVertex).
	
//In the case where we have unsurveyed neighbours, we go to the one that is the
//cheapest to go to.
+!chooseNextVertex(UnsurveyedNeighbours)
	<-
	.nth(0, UnsurveyedNeighbours, [Weight, Destination]);
	.print("I want move to vertex ", Destination, ". The edge weight is " , Weight, ".");
	!goto(Destination).