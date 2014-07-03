// Agent nodeAgent in project mako

// TODO: create Neighbours belief
/* Initial beliefs and rules */

/* Initial goals */

!start.

/* Plans */

+!start :
    .my_name(Name)
    <- .print("A new node agent has been created: ", Name).

+path(DestinationId, Cost, Steps)[source(HopId)]:
    not path(DestinationId, _, _)
    | path(HopId, _, HopCost, HopSteps)
    // either we already know a route with higher costs:
    | path(DestinationId, _, KnownCost, KnownSteps) & KnownCost > Cost + HopCost
    // or it takes fewer steps to reach the destination:
    | KnownSteps > Steps + HopSteps
    <- -path(DestinationId, _, KnownCost, KnownSteps); // assuming removing fails silently when there is no such belief
       -path(DestinationId, Cost, Steps)[source(HopId)]; // don't add the trigger to the BB
       +path(DestinationId, HopId, Cost, Step);
       .send(Neighbours, tell, path(DestinationId, Cost, Steps)).
       
+path(DestinationId, Cost, Steps)[source(Sender)]
    <- -path(DestinationId, Cost, Steps)[source(Sender)].