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
	   //!pathCostsCheaper(Vertex, Weight)[source(cartographer)];
	   -nodeValue(Vertex, _);
	   +nodeValue(Vertex, 1); //TODO: Actually implement this properly
	   ?nodeValue(Vertex, NodeValue);
	   .print("I added the node value for Vertex ", Vertex, ", and it is ", NodeValue, ".");
	   !calculateZone;
	   // add the neighbour afterwards to prevent telling someone who knows
	   // about exactly that edge:
	   +neighbour(Vertex, Weight).
	   
//Default plan if we already know about this neighbour.
+path(Vertex, Weight) <- .print("I already know about this path.").

// From the input list of vertices (VertexList) calculates the closest vertex
+?getClosestVertexFromList(VertexList, NextVertex):
    not .length(VertexList, 0)
    <-
    .findall([Steps, NextHopVertex], minStepsPath(Vertex, NextHopVertex, Steps, _) & .member(Vertex, VertexList), VertexDistanceList);
    .min(VertexDistanceList, NextVertexWithDist);
    .nth(1, NextVertexWithDist, NextVertex).

