// Calling for help, going to the repairer, get repaired.

// Initialize repair - send help request to repairers, wait for the answer and perform the corresponding action.
+!getRepaired:
    not closestRepairer(_)
    & position(Position)
    & repairerList(RepairerList)
    <-
    .abolish(closestRepairer(_));
    .print("Asking repairers about the closest less busy repairer.");
    .send(RepairerList, tell, requestRepair(Position));
    .wait(closestRepairer(X) & .ground(X), 1200, ElapsedTime);
    if(closestRepairer(Repairer)){
    	ia.getAgentPosition(Repairer, RepairerPosition);
    	!goto(RepairerPosition);
    } else{
    	.print("I did not receive any answer from repairers, will recharge.");
    	recharge;
    }.

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
    