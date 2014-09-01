// Calling for help, going to the repairer, get repaired.

// Initialize repair - send help request to repairers, recharge to be ready for the journey.
+!getRepaired:
    not sentHelpRequest
    & position(Position)
    & repairerList(RepairerList)
    <-
    .abolish(closestRepairer(_));
    .print("Asking repairers about the closest less busy repairer and recharge while waiting for the answer.");
    +sentHelpRequest;
    .send(RepairerList, tell, requestRepair(Position));
    recharge.

// Still didn't received the answer from repairers. Normally shouldn't get in here.    
+!getRepaired:
   not closestRepairer(_)
   <-
   .print("Still waiting for repairer answer, will recharge.");
   recharge.

// Delete sentHelpRequest when we got an answer from repairer.
+!getRepaired:
    closestRepairer(Repairer)
    & sentHelpRequest
    <-
    .abolish(sentHelpRequest);
    !!getRepaired.

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
    
    