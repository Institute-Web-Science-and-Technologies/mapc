// Agent nodeAgent in project mako
{ include("nodeAgent.steps.asl") }
{ include("nodeAgent.costs.asl") }
{ include("nodeAgent.zoning.asl") }

/* Initial beliefs and rules */
twoHopNeighbours([]).
/* Initial goals */
!start.

/* Plans */
// Set distances to self as 0:
+!start:
    .my_name(Name)
    <-
//	+minCostPath(Name, Name, 0, 0); // DestinationId, HopId, total costs, costs to hop
	+nodeValue(Name, 1);
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
+nodeValue(Vertex, NodeValue)[source(Sender)]
	:
	Sender \== self
	& .my_name(Vertex)
	& not nodeValue(Vertex, NodeValue)[source(self)]
	& twoHopNeighbours(TwoHopNeighbourList)
	<-
	.print("Learned about my own nodeValue(", Vertex, ",", NodeValue, ") from ", Sender, ".");
	.abolish(nodeValue(Vertex, _));
	+nodeValue(Vertex, NodeValue)[source(self)];
	.findall(OneHopNeighbour, neighbour(OneHopNeighbour, _), OneHopNeighbourList);
	.print("My direct neighbours are ", OneHopNeighbourList, ".");
	.print("My two-hop neighbours are ", TwoHopNeighbourList, ".");
	.concat(OneHopNeighbourList, TwoHopNeighbourList, OneAndTwoHopNeighbourList);
	.print("Will inform ", OneAndTwoHopNeighbourList, " about my node value.");
	.send(OneAndTwoHopNeighbourList, tell, nodeValue(Vertex, NodeValue));
	!calculateZone.
	
//Received a nodeValue belief that I already knew about.
//+nodeValue(Vertex, NodeValue)[source(Sender)]:
//	nodeValue(Vertex, NodeValue)[source(self)]
//	<-
//	-nodeValue(Vertex, NodeValue)[source(Sender)].
	
+nodeValue(Vertex, NodeValue)[source(Sender)]:
	not nodeValue(Vertex, NodeValue)[source(self)]
	<-
	.print("Received nodeValue(", Vertex, ",", NodeValue, ") from ", Sender, ".");
	.abolish(nodeValue(Vertex, _));
	+nodeValue(Vertex, NodeValue)[source(self)];
	!calculateZone.

+minStepsPath(Vertex, _, Value, _):
	Value <= 2
	& .my_name(ThisVertex)
	& nodeValue(ThisVertex, NodeValue)
	& Vertex \== ThisVertex
	<-
	.print("Learned about ", Vertex, " in my two-hop neighbourhood. Will send him my node value.");
	.send(Vertex, tell, nodeValue(ThisVertex, NodeValue));
	.findall(TwoHopNeighbour, minStepsPath(TwoHopNeighbour, _, 2, _), TwoHopNeighbourList);
	.print("My two-hop neighbours are ", TwoHopNeighbourList, ".");
	-+twoHopNeighbours(TwoHopNeighbourList).
	
+neighbour(Vertex, Weight)[source(Sender)]
	<-
	.print("I learned about my neighbour ", Vertex, " with edge weight ", Weight, ".").
	   
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
	
+?getNextHopToVertex(Destination, NextHop):
	minStepsPath(Destination, NextHopVertex, _, _)
	<-
	.print("The next hop to ", Destination, " is ", NextHopVertex, ".");
	NextHop = NextHopVertex.
	
+?getNextHopToVertex(Destination, NextHop):
	.my_name(ThisVertex)
	<- .print("I do not know how to get to this node.");
	NextHop = ThisVertex.