// Agent nodeAgent_costs in project mako

/* Initial beliefs and rules */

/* Initial goals */

/* Plans */

// Add new path if the others are worse:
+!pathCostsCheaper(DestinationId, Costs)[source(HopId)]:
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

// Add new path if there is no other:
+!pathCostsCheaper(DestinationId, Costs)[source(HopId)]:
    // The agent does not know an alternative path (the rest are just needed parameters):
    not minCostPath(DestinationId, _, _, _) & minCostPath(HopId, _, HopCost, _)
    & NewCosts = Costs + HopCost
    <- +minCostPath(DestinationId, HopId, NewCosts, HopCost);
       .findall(NodeAgent, neighbour(NodeAgent), Neighbours);
       for (.member(Neighbour, Neighbours)) {
            .send(Neighbour, achieve, pathCostsCheaper(DestinationId, NewCosts));
       }.

// The suggested path does not improve our situation, hence ignore it:
+!pathCostsCheaper(DestinationId, Cost)[source(Sender)]
    <- true.