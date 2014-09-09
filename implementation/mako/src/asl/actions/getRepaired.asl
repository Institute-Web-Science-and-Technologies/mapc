// Calling for help, going to the repairer, get repaired.

// Got to the closest repairer if there is a repairer in reachability range and we did not reach its position.
+!getRepaired:
    .my_name(Name)
    & ia.getClosestRepairer(Name, RepairerName, RepairerPosition)
    & position(Position)
    & Position \== RepairerPosition
    <-
    .print("Going to the closest repairer ", RepairerName, " at position ", RepairerPosition);
    !goto(RepairerPosition).

// When reached the repairer - recahrge and wait to get repaired.
+!getRepaired:
    .my_name(Name)
    & ia.getClosestRepairer(Name, RepairerName, RepairerPosition)
    & position(RepairerPosition)
    <-
    .print("I have reached ", RepairerName," position, will recharge.");
    recharge.

// It there is no path known to any repairer - move to random node trying to reach explored zones.
+!getRepaired:
    position(Position)
    & (visibleEdge(Position, NextVertex) | visibleEdge(NextVertex, Position))
    <-
    .print("I did not find any repairer in the reachability range - will move to the random node.");
    !goto(NextVertex).  
