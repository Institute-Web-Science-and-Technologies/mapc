// Calling for help, going to the repairer, get repaired.

// Go to the closest repairer if there is a repairer in reachability range and
// we did not reach its position.
+!getRepaired:
    .my_name(Name)
    & ia.getClosestRepairer(Name, RepairerName, RepairerPosition)
    & position(Position)
    & Position \== RepairerPosition
    <-
    .print("Going to the closest repairer ", RepairerName, " at position ", RepairerPosition);
    !goto(RepairerPosition).

// When the repairer is reached - recharge and wait to get repaired.
+!getRepaired:
    .my_name(Name)
    & ia.getClosestRepairer(Name, RepairerName, RepairerPosition)
    & position(RepairerPosition)
    <-
    .print("I have reached ", RepairerName," position, will recharge.");
    recharge.

// If there is no known path to a repairer, expand the subgraph that the agent
// is in.
+!getRepaired:
	position(MyPosition)
	& ia.getClosestSubgraphEdge(MyPosition, EdgeNode)
	<-
	.print("Couldn't find a path to a repairer - will expand my subgraph by moving towards ", EdgeNode);
	!goto(EdgeNode).
	
// If there is no known path to a repairer, and the agent can't expand the
// subgraph, move randomly.
+!getRepaired:
    position(Position)
    & (visibleEdge(Position, NextVertex) | visibleEdge(NextVertex, Position))
    <-
    .print("I did not find any repairer in the reachability range - will move to the random node.");
    !goto(NextVertex).  
