// Agent nodeAgent in project mako
{ include("nodeAgent.steps.asl") }
{ include("nodeAgent.costs.asl") }
{ include("nodeAgent.zoning.asl") }

/* Initial beliefs and rules */

/* Initial goals */
!start.

/* Plans */
// Set distances to self as 0:
+!start:
    .my_name(Name)
    <-
//	+minCostPath(Name, Name, 0, 0); // DestinationId, HopId, total costs, costs to hop
    +minStepsPath(Name, Name, 0, 0). // DestinationId, HopId, total steps, costs to hop

// This plan adds Vertex as a neighbour and may set this path as the new
// cheapest (cost) or shortest (steps) path.
+path(Vertex, Weight)[source(Sender)]:
    Sender == cartographer & not neighbour(Vertex, Weight)
    <-
	   -path(Vertex, Weight);
	   -neighbour(Vertex, _);
	   !pathStepsFewer(Vertex, 0, Weight)[source(cartographer)];
	   // add the neighbour afterwards to prevent telling someone who knows
	   // about exactly that edge:
	   +neighbour(Vertex, Weight)
	   .

//Received a new nodeValue belief, either from an explorer who has probed or
//from a nearby node agent.
+nodeValue(Vertex, NodeValue)[source(Sender)]:
	Sender \== self
	& .my_name(Vertex)
	& not nodeValue(Vertex, NodeValue)
	<-
	.abolish(nodeValue(Vertex, _));
	+nodeValue(Vertex, NodeValue)[source(self)];
	.findall(Vertex, neighbour(Vertex, _), OneHopNeighbourList);
	.findall(Vertex, minStepsPath(Vertex, _, 2, _), TwoHopNeighbourList);
	.concat(OneHopNeighbourList, TwoHopNeighbourList, OneAndTwoHopNeighbourList);
	.print("My one and two-hop neighbour list is ", OneAndTwoHopNeighbourList);
	.send(OneAndTwoHopNeighbourList, tell, nodeValue(Vertex, NodeValue));
	!calculateZone.
	
//Received a nodeValue belief that I already knew about.
+nodeValue(Vertex, NodeValue)[source(Sender)]:
	nodeValue(Vertex, NodeValue)[source(self)]
	<-
	-nodeValue(Vertex, NodeValue)[source(Sender)].
	
+nodeValue(Vertex, NodeValue):
	not nodeValue(Vertex, NodeValue)[source(self)]
	<-
	.abolish(nodeValue(Vertex, _));
	+nodeValue(Vertex, NodeValue)[source(self)];
	!calculateZone.

+minStepsPath(Vertex, _, Value, _):
	Value <= 2
	& .my_name(ThisVertex)
	& nodeValue(ThisVertex, NodeValue)
	<-
	.print("Learned about a new neighbour in my two-hop neighbourhood, telling him about my node value.");
	.send(Vertex, tell, nodeValue(ThisVertex, NodeValue))
	.
	
+neighbour(Vertex, Weight) <- .print("I learned about my neighbour ", Vertex, " with edge weight ", Weight, ".").
	   
//Default plan if we already know about this neighbour.
+path(Vertex, Weight) <- .print("I already know about this path.").

// From the input list of vertices (VertexList) calculates the closest vertex
+?getClosestVertexFromList(VertexList, NextVertex):
    not .length(VertexList, 0)
    & .my_name(ThisVertex)
    <-
    .findall([Steps, NextHopVertex], minStepsPath(Vertex, NextHopVertex, Steps, _) & .member(Vertex, VertexList), VertexDistanceList);
    if (VertexDistanceList == []) {
    	NextVertex = ThisVertex
    }
    else {
    	.min(VertexDistanceList, NextVertexWithDist);
    	.nth(1, NextVertexWithDist, NextVertex)
    }
    .print("The closest vertex from the list ", VertexList, " is ", NextVertex, ".").
    
+?getClosestVertexFromList(VertexList, NextVertex)
	<-
	.print("Tried to find the closest vertex from a list, but the list was empty!").