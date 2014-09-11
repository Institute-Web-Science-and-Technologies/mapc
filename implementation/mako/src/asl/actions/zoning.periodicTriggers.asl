/* Plans */
// debugZoning.

// Wake up all coaches and tell them to destroy their current zone properly by
// also informing JavaMap and their minions.
+step(Numeral)[source(self)]:
    isCoach(true)
    & isLocked(false)
    & plannedZoneTimeInSteps(Steps)
    & Numeral mod Steps == 0
    <- .print("[zoning] periodic trigger for coaches to destroy their zones.");
       !cancelledZoneBuilding.

// Make coaches check for enemies in range at every step to be able to call
// saboteurs for help.
+step(Numeral)[source(self)]:
    isCoach(true)
    <- !checkZoneUnderAttack.
       
// If there was no zone and an agent simply looked for a high valued zone around
// him, he should not directly do this again. Instead he either waits until
// others trigger him or a next step begins.
+step(Numeral)[source(self)]:
    isAvailableForZoning
    & zoneGoalVertex(_)
    <- !preparedNewZoningRound.

+step(Numeral)[source(self)]:
    debugZoning
    & bestZone(_, _, ClosestAgents)[source(self)]
    & isCoach(ShouldBeRole)
    & isMinion(X)
    & isLocked(Y)
    & .count(broadcastAcknowledgement[source(_)], RepliesAmount)
     <- .print("[zoning][coach=",ShouldBeRole,"][>>>>] Currently in my zone together with ", ClosestAgents, ". [locked=", Y,"][minion=",X,"][replies=",RepliesAmount,"]").
  
+step(Numeral)[source(self)]:
    debugZoning
    & bestZone(_, _, ClosestAgents)[source(Coach)]
    & isMinion(ShouldBeRole)
    & isCoach(X)
    & isLocked(Y)
    & .count(broadcastAcknowledgement[source(_)], RepliesAmount)
    <- .print("[zoning][minion=",ShouldBeRole,"][>>>>] Currently in ", Coach, "'s zone together with ", ClosestAgents, ". [locked=", Y,"][coach=",X,"][replies=",RepliesAmount,"]").
  
+step(Numeral)[source(self)]:
    debugZoning
    & zoneNode(Node)
    <- .print("[zoning][singleZoner][>>>>] Standing on ", Node).
