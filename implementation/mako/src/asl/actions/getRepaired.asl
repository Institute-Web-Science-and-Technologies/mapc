// Calling for help, going to the repairer, get repaired.

// Initialize repair - send help request to repairers, recharge to be ready for the journey.
+!getRepaired:
    not closestRepairer(_)
    & position(Position)
    & repairerList(RepairerList)
    <-
    .abolish(closestRepairer(_));
    .print("Asking repairers about the closest less busy repairer and recharge while waiting for the answer.");
    .send(RepairerList, tell, requestRepair(Position));
    recharge.

// If not reached the target - go to the repairer position.
+!getRepaired:
    closestRepairer(Repairer)
    & ia.getAgentPosition(Repairer, RepairerPosition)
    & position(Position)
    & Position \== RepairerPosition
    <-
    .print("Moving to the closest repairer ", Repairer, " at position ", RepairerPosition);
    !goto(RepairerPosition).

// When reached the repairer - just recharge and wait to get repaired.    
+!getRepaired:
    closestRepairer(Repairer)
    & ia.getAgentPosition(Repairer, RepairerPosition)
    & position(Position)
    & Position == RepairerPosition
    <-
    .print("Reached the repairer, waiting to get repaired, will recharge.");
    recharge.   
    
+closestRepairer(Repairer) <- .print(Repairer, " will repair me. Going towards his location.").
    