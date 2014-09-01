// Calling for help, going to the repairer, get repaired.

+!getRepaired:
    not sentHelpRequest
    & position(Position)
    & repairerList(RepairerList)
    <-
    .print("Asking repairers about the closest less busy repairer and recharge while waiting for the answer.");
    +sentHelpRequest;
    .send(RepairerList, tell, requestRepair(Position));
    recharge.
    


