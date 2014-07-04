// Agent nodeAgent in project mako

/* Initial beliefs and rules */

/* Initial goals */

/* Plans */

// this method adds Vertex as a neighbour and removes all other paths to this
// Vertex, assuming the neighbour is the shortest path.
+path(Vertex, Weight)[source(Sender)]:
    Sender == cartographer
    <- -path(Vertex, Weight)[source(cartographer)];
       -neighbour(Vertex);
//       !addedPathIfShorter(Vertex, Weight);
       !pathCostsCheaper(DestinationId, Costs)[source(HopId)];
       !pathStepsFewer(DestinationId, 1)[source(HopId)];
       +neighbour(Vertex).

//+stepPath(DestinationId, Steps)[source(HopId)] <- true.
//+costPath(DestinationId, Costs)[source(HopId)] <- true.

//+path(DestinationId, Costs)[source(HopId)]
//    <- !pathCostsCheaper(DestinationId, Costs)[source(HopId)];
//       !pathStepsFewer(DestinationId, Costs)[source(HopId)];
//       -path(DestinationId, Cost)[source(HopId)].
       
+!pathCostsCheaper(DestinationID, Costs)[source(HopId)]:
	// How much does travelling to the hop and to the destination currently cost:
    minCostPath(HopId, _, HopCost, _) & minCostPath(DestinationId, _, KnownCosts, _)
    // We know a route but with higher costs:
    & NewCosts = Costs + HopCost & KnownCosts > NewCosts
	<- -minCostPath(DestinationId, _, KnownCosts, _);
       +minCostPath(DestinationId, HopId, NewCosts, HopCost);
       .findall(NodeAgent, neighbour(NodeAgent), Neighbours);
       for (.member(Neighbour, Neighbours)) {
       		.send(Neighbour, achieve, pathCostsCheaper(DestinationId, NewCosts));
       }.
       
+!pathStepsFewer(DestinationId, Steps)[source(HopId)]: 
	// How many steps does travelling to the hop and to the destination currently take:
    minStepsPath(HopId, _, _, HopCost) & minStepsPath(DestinationId, _, KnownSteps, _)
    // We know a route but with more steps:
    & NewSteps = Steps + 1 & KnownSteps > NewSteps
	<- -minStepsPath(DestinationId, _, KnownSteps, _);
       +minStepsPath(DestinationId, HopId, NewSteps, HopCost);
       .findall(NodeAgent, neighbour(NodeAgent), Neighbours);
       for (.member(Neighbour, Neighbours)) {
       		.send(Neighbour, achieve, pathStepsFewer(DestinationId, NewSteps));
       }.
	
// the suggested path does not improve our situation, hence ignore it:
+!pathCostsCheaper(DestinationId, Cost)[source(Sender)]
    <- true.

+!pathStepsCheaper(DestinationId, Steps)[source(Sender)]
    <- true.

/* Additional goals */

// Replace old edges by the new one if it was the cheapest:
//@letCartographerOnlyAddCheaperEdges[atomic]
//+!addedPathIfShorter(Vertex, Cost):
//    not minCostPath(Vertex, _, KnownCosts)
//    // We know a route but with higher costs:
//    | KnownCosts > Cost
//    <- -minCostPath(Vertex, _, _);
//       +minCostPath(Vertex, Vertex, Cost, Cost);
//       .findall(NodeAgent, neighbour(NodeAgent), Neighbours);
//       for (.member(Neighbour, Neighbours)) {
//       		.send(Neighbour, tell, path(Vertex, Cost));
//       }.
       
// We know a cheaper route -- but it is more steps away. This scenario is quite
// unlikely in practice:
//+!addedPathIfShorter(_, _)
//    <- true.