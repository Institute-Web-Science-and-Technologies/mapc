// Agent nodeAgent_steps in project mako

/* Goals */

// Add path if known alternatives took more steps.
@addShorterPath[atomic]
+!pathStepsFewer(DestinationId, Steps)[source(HopId)]: 
    // How many steps does travelling to the hop and to the destination currently take:
    neighbour(HopId, HopCost) & minStepsPath(DestinationId, _, KnownSteps, _)
    // We know a route but with more steps:
    & NewSteps = Steps + 1 & KnownSteps > NewSteps
    <- -minStepsPath(DestinationId, _, KnownSteps, _);
       +minStepsPath(DestinationId, HopId, NewSteps, HopCost);
       !toldNeighboursAboutCloserPath(DestinationId, NewSteps).

// Add path if there is no known alternative yet but there is a path to the hop:
@addNewShortestPath[atomic]
+!pathStepsFewer(DestinationId, Steps)[source(HopId)]:
    // The agent does not know an alternative path (the rest are just needed parameters):
    not minStepsPath(DestinationId, _, _, _)
    & NewSteps = Steps + 1 & neighbour(HopId, HopCost)
    <- +minStepsPath(DestinationId, HopId, NewSteps, HopCost);
       !toldNeighboursAboutCloserPath(DestinationId, NewSteps).

// If a cartographer wants to add information from an edge he may add it
// directly because there will be no shorter step path than this one:
+!pathStepsFewer(DestinationId, Steps, HopCost)[source(Sender)]
    <- -minStepsPath(DestinationId, _, KnownSteps, _);
       +minStepsPath(DestinationId, DestinationId, 1, HopCost);
       !toldNeighboursAboutCloserPath(DestinationId, 1).

// the suggested path does not improve our situation, hence ignore it:  
+!pathStepsFewer(DestinationId, Steps)[source(Sender)]
    <- true.

+!toldNeighboursAboutCloserPath(DestinationId, Steps)
    <- .findall(NodeAgent, neighbour(NodeAgent, _), Neighbours);
       for (.member(Neighbour, Neighbours)) {
            .send(Neighbour, achieve, pathStepsFewer(DestinationId, Steps));
       }.