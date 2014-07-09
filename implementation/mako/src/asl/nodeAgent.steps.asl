// Agent nodeAgent_steps in project mako

/* Goals */

// Add path if known alternatives took more steps.
+!pathStepsFewer(DestinationId, Steps)[source(HopId)]: 
    // How many steps does travelling to the hop and to the destination currently take:
    minStepsPath(HopId, _, _, HopCost) & minStepsPath(DestinationId, _, KnownSteps, _)
    // We know a route but with more steps:
    & NewSteps = Steps + 1 & KnownSteps > NewSteps
    <- .print("Entering the first !pathStepsFewer(DestinationId, Steps)[source(HopId)] plan.");
    -minStepsPath(DestinationId, _, KnownSteps, _);
       +minStepsPath(DestinationId, HopId, NewSteps, HopCost);
       !toldNeighboursAboutCloserPath(DestinationId, NewSteps).

// Add path if there is no known alternative yet but there is a path to the hop:
+!pathStepsFewer(DestinationId, Steps)[source(HopId)]:
    // The agent does not know an alternative path (the rest are just needed parameters):
    not minStepsPath(DestinationId, _, _, _)
    & NewSteps = Steps + 1 & neighbour(HopId, HopCost)
    <- .print("Entering the second !pathStepsFewer(DestinationId, Steps)[source(HopId)] plan.");
    +minStepsPath(DestinationId, HopId, NewSteps, HopCost);
       !toldNeighboursAboutCloserPath(DestinationId, NewSteps).

// If a cartographer wanted to add information from an edge but couldn't because
// no information existed before about paths to destination and/or hop, he may
// add it directly because there will be no intermediate nodes. This means, he
// knows the hop costs.
+!pathStepsFewer(DestinationId, Steps)[source(Sender)]:
    Sender == cartographer
    & minCostPath(DestinationId, _, HopCost, _)
    <- .print("Entering the first +!pathStepsFewer(DestinationId, Steps)[source(Sender)] plan.");
    -minStepsPath(DestinationId, _, KnownSteps, _);
       +minStepsPath(DestinationId, DestinationId, 1, HopCost);
       !toldNeighboursAboutCloserPath(DestinationId, 1).

// the suggested path does not improve our situation, hence ignore it:  
+!pathStepsFewer(DestinationId, Steps)[source(Sender)]
    <- .print("Entering the second +!pathStepsFewer(DestinationId, Steps)[source(Sender)] plan.");
    true.

+!toldNeighboursAboutCloserPath(DestinationId, Costs)
    <- .print("Entering the +!toldNeighboursAboutCloserPath(DestinationId, Costs) plan.");
    .findall(NodeAgent, neighbour(NodeAgent, _), Neighbours);
       for (.member(Neighbour, Neighbours)) {
            .send(Neighbour, achieve, pathStepsFewer(DestinationId, Costs));
       }.