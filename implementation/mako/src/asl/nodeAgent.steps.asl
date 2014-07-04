// Agent nodeAgent_steps in project mako

/* Initial beliefs and rules */

/* Initial goals */

/* Plans */

// Add new path if the others are worse:
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

// Add new path if there is no other:
+!pathStepsFewer(DestinationId, Steps)[source(HopId)]:
    // The agent does not know an alternative path (the rest are just needed parameters):
    not minStepsPath(DestinationId, _, _, _)
    & NewSteps = Steps + 1
    <- +minStepsPath(DestinationId, HopId, NewSteps, HopCost);
       .findall(NodeAgent, neighbour(NodeAgent), Neighbours);
       for (.member(Neighbour, Neighbours)) {
            .send(Neighbour, achieve, pathStepsFewer(DestinationId, NewSteps));
       }.

// The suggested path does not improve our situation, hence ignore it:  
+!pathStepsFewer(DestinationId, Steps)[source(Sender)]
    <- true.