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
       !pathCostsCheaper(Vertex, Weight)[source(cartographer)];
       !pathStepsFewer(Vertex, 0)[source(cartographer)];
       +neighbour(Vertex).

/* Additional goals */

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

// If a cartographer wanted to add information from an edge but couldn't because
// no information existed before about minCostPaths, he may add it directly
// because there will be no intermediate nodes.
+!pathCostsCheaper(DestinationId, Costs)[source(Sender)]:
    Sender == cartographer
    // there was no alternative path:
    & not minCostPath(DestinationId, _, KnownCosts, _)
    // or it was more expensive:
    | KnownCosts > Costs
    <- -minCostPath(DestinationId, _, KnownCosts, _);
       +minCostPath(DestinationId, DestinationId, Costs, Costs);
       .findall(NodeAgent, neighbour(NodeAgent), Neighbours);
       for (.member(Neighbour, Neighbours)) {
            .send(Neighbour, achieve, pathCostsCheaper(DestinationId, Costs));
       }.
    
// the suggested path does not improve our situation, hence ignore it:
+!pathCostsCheaper(DestinationId, Cost)[source(Sender)]
    <- .print("@@@@@@@@@@@@@@@@@@@@@@@@@@", DestinationId, " <dest", Cost, " <cost", Sender, " <Sender");
    true.
       
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

// If a cartographer wanted to add information from an edge but couldn't because
// no information existed before about minCostPaths, he may add it directly
// because there will be no intermediate nodes.
+!pathStepsFewer(DestinationId, Steps)[source(Sender)]:
    Sender == cartographer
    & minCostPath(DestinationId, _, HopCost, _)
    <- -minStepsPath(DestinationId, _, KnownSteps, _);
       +minStepsPath(DestinationId, DestinationId, 1, HopCost);
       .findall(NodeAgent, neighbour(NodeAgent), Neighbours);
       for (.member(Neighbour, Neighbours)) {
            .send(Neighbour, achieve, pathStepsFewer(DestinationId, 1));
       }.

// the suggested path does not improve our situation, hence ignore it:	
+!pathStepsFewer(DestinationId, Steps)[source(Sender)]
    <- true.