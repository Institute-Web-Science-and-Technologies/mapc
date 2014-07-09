// Agent nodeAgent_costs in project mako

/* Goals */

// Add path if known alternatives where more expensive.
+!pathCostsCheaper(DestinationId, Costs)[source(HopId)]:
    // How much does travelling to the hop and to the destination currently cost:
    minCostPath(HopId, _, HopCost, _) & minCostPath(DestinationId, _, KnownCosts, _)
    // We know a route but with higher costs:
    & NewCosts = Costs + HopCost & KnownCosts > NewCosts
    <- .print("Entering the first !pathCostsCheaper(DestinationId, Costs)[source(HopId)] plan.");
    	-minCostPath(DestinationId, _, KnownCosts, _);
       +minCostPath(DestinationId, HopId, NewCosts, HopCost);
       !toldNeighboursAboutCheaperPath(DestinationId, NewCosts).

// Add path if there is no known alternative yet but there is a path to the hop:
+!pathCostsCheaper(DestinationId, Costs)[source(HopId)]:
    // The agent does not know an alternative path (the rest are just needed parameters):
    not minCostPath(DestinationId, _, _, _) & minCostPath(HopId, _, HopCost, _)
    & NewCosts = Costs + HopCost
    <- .print("Entering the second !pathCostsCheaper(DestinationId, Costs)[source(HopId)] plan.");
    +minCostPath(DestinationId, HopId, NewCosts, HopCost);
       !toldNeighboursAboutCheaperPath(DestinationId, NewCosts).

// If a cartographer wanted to add information from an edge but couldn't because
// no information existed before about paths to destination and/or hop, he may
// add it directly because there will be no intermediate nodes. This means, he
// knows the hop costs.
+!pathCostsCheaper(DestinationId, Costs)[source(Sender)]:
    Sender == cartographer
    // there was no alternative path:
    & (not minCostPath(DestinationId, _, KnownCosts, _)
    // or it was more expensive:
    | KnownCosts > Costs)
    <- .print("Entering the first +!pathCostsCheaper(DestinationId, Costs)[source(Sender)] plan.");
    -minCostPath(DestinationId, _, KnownCosts, _);
       +minCostPath(DestinationId, DestinationId, Costs, Costs);
       !toldNeighboursAboutCheaperPath(DestinationId, Costs).

// the suggested path does not improve our situation, hence ignore it:
+!pathCostsCheaper(DestinationId, Cost)[source(Sender)]
    <- .print("Entering the second +!pathCostsCheaper(DestinationId, Cost)[source(Sender)] plan.");
    true.
    
+!toldNeighboursAboutCheaperPath(DestinationId, Costs)
    <- .print("Entering the +!toldNeighboursAboutCheaperPath(DestinationId, Costs) plan.");
    .findall(NodeAgent, neighbour(NodeAgent, _), Neighbours);
       for (.member(Neighbour, Neighbours)) {
            .send(Neighbour, achieve, pathCostsCheaper(DestinationId, Costs));
       }.