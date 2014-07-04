// Agent nodeAgent in project mako
{ include("nodeAgent.steps.asl") }
{ include("nodeAgent.costs.asl") }

/* Initial beliefs and rules */

/* Initial goals */
!start.

/* Plans */
+!start:
    .my_name(Name)
    <- +minCostPath(Name, Name, 0, 0); // Destination, hop, total costs, costs to hop
       +minStepsPath(Name, Name, 0, 0). // Destination, hop, total steps, costs to hop

// this method adds Vertex as a neighbour and removes all other paths to this
// Vertex, assuming the neighbour is the shortest path.
+path(Vertex, Weight)[source(Sender)]:
    Sender == cartographer
    <- -path(Vertex, Weight)[source(cartographer)];
       -neighbour(Vertex);
       !pathCostsFromCartographer(Vertex, Weight);
       !pathStepsFromCartographer(Vertex, 0);
       +neighbour(Vertex).

/* Additional goals */


// If a cartographer wanted to add information from an edge but couldn't because
// no information existed before about minCostPaths, he may add it directly
// because there will be no intermediate nodes.
+!pathCostsFromCartographer(DestinationId, Steps):
    minCostPath(DestinationId, _, HopCost, _)
    <- -minStepsPath(DestinationId, _, KnownSteps, _);
       +minStepsPath(DestinationId, DestinationId, 1, HopCost);
       .findall(NodeAgent, neighbour(NodeAgent), Neighbours);
       for (.member(Neighbour, Neighbours)) {
            .send(Neighbour, achieve, pathStepsFewer(DestinationId, 1));
       }.

+!pathCostsFromCartographer(_, _).

// If a cartographer wanted to add information from an edge but couldn't because
// no information existed before about minCostPaths, he may add it directly
// because there will be no intermediate nodes.
+!pathStepsFromCartographer(DestinationId, Costs):
    // there was no alternative path:
    (not minCostPath(DestinationId, _, KnownCosts, _)
    // or it was more expensive:
    | KnownCosts > Costs)
    <- -minCostPath(DestinationId, _, KnownCosts, _);
       +minCostPath(DestinationId, DestinationId, Costs, Costs);
       .findall(NodeAgent, neighbour(NodeAgent), Neighbours);
       for (.member(Neighbour, Neighbours)) {
            .send(Neighbour, achieve, pathCostsCheaper(DestinationId, Costs));
       }.

+!pathStepsFromCartographer(_, _).