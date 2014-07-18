// Agent nodeAgent in project mako
{ include("nodeAgent.steps.asl") }
{ include("nodeAgent.costs.asl") }

/* Initial beliefs and rules */

/* Initial goals */
!start.

/* Plans */
// Set distances to self as 0:
+!start:
    .my_name(Name)
    <- +minCostPath(Name, Name, 0, 0); // DestinationId, HopId, total costs, costs to hop
    +minStepsPath(Name, Name, 0, 0). // DestinationId, HopId, total steps, costs to hop

// This plan adds Vertex as a neighbour and may set this path as the new
// cheapest (cost) or shortest (steps) path.
+path(Vertex, Weight)[source(Sender)]:
    Sender == cartographer
    <- -path(Vertex, Weight)[source(cartographer)];
       -neighbour(Vertex, _);
       !pathStepsFewer(Vertex, 0, Weight)[source(cartographer)];
       //!pathCostsCheaper(Vertex, Weight)[source(cartographer)];
       // add the neighbour afterwards to prevent telling someone who knows
       // about exactly that edge:
       +neighbour(Vertex, Weight).

// From the input list of vertices (VertexList) calculates the closest vertex
+?getClosestVertexFromList(VertexList, NextVertex):
    not .length(VertexList, 0)
    <-
    .findall([Steps, NextHopVertex], minStepsPath(Vertex, NextHopVertex, Steps, _) & .member(Vertex, VertexList), VertexDistanceList);
    .min(VertexDistanceList, NextVertexWithDist);
    .nth(1, NextVertexWithDist, NextVertex).

