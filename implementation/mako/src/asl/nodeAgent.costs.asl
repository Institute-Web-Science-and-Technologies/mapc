// Agent nodeAgent_costs in project mako

/* Goals */

// Add path if known alternatives where more expensive.
@addCheaperPath[atomic]
+!pathCostsCheaper(DestinationId, Costs)[source(HopId)]:
    // How much does travelling to the hop and to the destination currently cost:
   minCostPath(DestinationId, _, KnownCosts, _) & neighbour(HopId, HopCost)
    // We know a route but with higher costs:
    & NewCosts = Costs + HopCost & KnownCosts > NewCosts
    <- -minCostPath(DestinationId, _, KnownCosts, _);
       +minCostPath(DestinationId, HopId, NewCosts, HopCost);
       !toldNeighboursAboutCheaperPath(DestinationId, NewCosts).

// Add path if there is no known alternative yet but there is a path to the hop:
@addNewCheapestPath[atomic]
+!pathCostsCheaper(DestinationId, Costs)[source(HopId)]:
    // The agent does not know an alternative path (the rest are just needed parameters):
    not minCostPath(DestinationId, _, _, _) & neighbour(HopId, HopCost)
    & NewCosts = Costs + HopCost
    <- +minCostPath(DestinationId, HopId, NewCosts, HopCost);
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
    <- -minCostPath(DestinationId, _, KnownCosts, _);
       +minCostPath(DestinationId, DestinationId, Costs, Costs);
       !toldNeighboursAboutCheaperPath(DestinationId, Costs).

// the suggested path does not improve our situation, hence ignore it:
+!pathCostsCheaper(DestinationId, Cost)[source(Sender)]
    <- true.
    
+!toldNeighboursAboutCheaperPath(DestinationId, Costs)
    <- .findall(NodeAgent, neighbour(NodeAgent, _), Neighbours);
       for (.member(Neighbour, Neighbours)) {
            .send(Neighbour, achieve, pathCostsCheaperPrioritised(DestinationId, Costs));
       }.

@preferCheaperPaths[priority(0)]
+!pathCostsCheaperPrioritised(DestinationId, Costs)[source(HopId)]:
    Costs < 20
    <- !pathCostsCheaper(DestinationId, Costs)[source(HopId)].

@delayExpensivePaths[priority(-10)]
+!pathCostsCheaperPrioritised(DestinationId, Costs)[source(HopId)]
    <- !pathCostsCheaper(DestinationId, Costs)[source(HopId)].