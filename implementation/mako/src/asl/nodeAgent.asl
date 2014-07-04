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
       !addedPathIfShorter(Vertex, Weight);
       +neighbour(Vertex).

+path(DestinationId, Costs)[source(HopId)]:
    // How much does travelling to the hop and to the destination currently cost:
    minCostPath(HopId, _, HopCost) & minCostPath(DestinationId, _, KnownCosts)
    // We know a route but with higher costs:
    & NewCosts = Costs + HopCost & KnownCosts > NewCosts
    <- -minCostPath(DestinationId, _, KnownCosts);
       // don't add this plan to the BB:
       -path(DestinationId, Cost)[source(HopId)];
       +minCostPath(DestinationId, HopId, NewCosts);
       if (neighbour(Neighbours)) {
       		.print("neighbour(Neighbours): ",  Neighbours);
           .send(Neighbours, tell, path(DestinationId, NewCosts));
       }.
       
// the suggested path does not improve our situation, hence ignore it:
+path(DestinationId, Cost)[source(Sender)]
    <- -path(DestinationId, Cost)[source(Sender)].

/* Additional goals */

// Replace old edges by the new one if it was the cheapest:
@letCartographerOnlyAddCheaperEdges[atomic]
+!addedPathIfShorter(Vertex, Cost):
    not minCostPath(Vertex, _, KnownCosts)
    // We know a route but with higher costs:
    | KnownCosts > Cost
    <- -minCostPath(Vertex, _, _);
       +minCostPath(Vertex, Vertex, Cost);
       .findall(NodeAgent, neighbour(NodeAgent), Neighbours);
       for (.member(Neighbour, Neighbours)) {
       		.send(Neighbour, tell, path(Vertex, Cost));
       }.
       
// We know a cheaper route -- but it is more steps away. This scenario is quite
// unlikely in practice:
+!addedPathIfShorter(_, _)
    <- true.