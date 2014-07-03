// Agent nodeAgent in project mako

/* Initial beliefs and rules */

/* Initial goals */

/* Plans */

// this method adds Vertex as a neighbour and removes all other paths to this
// Vertex, assuming the neighbour is the shortest path.
+path(Vertex, Weight)[source(Sender)]:
    Sender == cartographer
    <- -path(Vertex, Weight)[source(cartographer)];
       !addedPathIfShorter(Vertex, Weight);
       -+neighbour(Vertex).

+path(DestinationId, Costs)[source(HopId)]:
    // How much does travelling to the hop and to the destination currently cost:
    shortestPath(HopId, _, HopCost) & shortestPath(DestinationId, _, KnownCosts)
    // We know a route but with higher costs:
    & NewCosts = Costs + HopCost & KnownCosts > NewCosts
    <- -shortestPath(DestinationId, _, KnownCosts);
       // don't add this plan to the BB:
       -shortestPath(DestinationId, Cost)[source(HopId)];
       +shortestPath(DestinationId, HopId, NewCosts);
       if (neighbour(Neighbours)) {
           .send(Neighbours, tell, path(DestinationId, NewCosts));
       }.
       
// the suggested path does not improve our situation, hence ignore it:
+path(DestinationId, Cost)[source(Sender)]
    <- -path(DestinationId, Cost)[source(Sender)].

/* Additional goals */

// Replace old edges by the new one if it was the cheapest:
@letCartographerOnlyAddCheaperEdges[atomic]
+!addedPathIfShorter(Vertex, Cost):
    not shortestPath(Vertex, _, KnownCosts)
    // We know a route but with higher costs:
    | KnownCosts > Cost
    <- -shortestPath(Vertex, _, _);
       +shortestPath(Vertex, Vertex, Cost);
       if (neighbour(Neighbours)) {
           .send(Neighbours, tell, path(Vertex, Cost));
       }.
       
// We know a cheaper route – but it is more steps away. This scenario is quite
// unlikely in practice:
+!addedPathIfShorter(_, _)
    <- true.