/* Plans */

// Wake up all coaches and tell them to destroy their current zone properly by
// also informing JavaMap and their minions.
+step(Numeral)[source(self)]:
    isCoach(true)
    & isLocked(false)
    & maxZoneTime(Steps)
    & Numeral mod Steps == 0
    <- .print("[zoning] periodic trigger for coaches to destroy their zones.");
       !cancelledZoneBuilding.

// Make coaches check for enemies in range at every step to be able to call
// saboteurs for help.
+step(Numeral)[source(self)]:
    isCoach(true)
    <- !checkZoneUnderAttack.

// If a minion does not get called by his coach, he starts anew.
+step(Numeral)[source(self)]:
    isMinion(true)
    & currentMinionWaitingTime(MaxSteps)
    & maxMinionWaitingTime(MaxSteps)
    <- .print("[zoning][minion] My coach did not call me in time.");
       !cancelledZoneBuilding.

// Make minions only wait a certain amount of time for their coaches to call
// them.
+step(Numeral)[source(self)]:
    isMinion(true)
    & currentMinionWaitingTime(Steps)
    <- NewSteps = Steps + 1;
       -+currentMinionWaitingTime(NewSteps).

// If there was no zone and an agent simply looked for a high valued zone around
// him, he should not directly do this again. Instead he either waits until
// others trigger him or a next step begins. The same goes for agents who did
// not find a zone but believed one would be built.
+step(Numeral)[source(self)]:
    isAvailableForZoning
    <- !preparedNewZoningRound.

// If an agent spent too many steps already waiting for beliefs, he tries anew.
+step(Numeral)[source(self)]:
    isInZoningRound
    & maxZoningRoundTime(MaxSteps)
    & currentTimeInZoningRound(MaxSteps)
    & .count(broadcastAcknowledgement[source(_)], RepliesAmount)
    <- -+isLocked(false);
       .print("[zoning] I have spent too many steps trying to build a zone. Restarting. Up to now, I got ", RepliesAmount, " replies.");
       !preparedNewZoningRound.

// Add a counter for time spent in zoning round to be able to restart it after
// a fixed period of time.
+step(Numeral)[source(self)]:
    isInZoningRound
    <- ?currentTimeInZoningRound(Steps);
       NewSteps = Steps + 1;
       -+currentTimeInZoningRound(NewSteps).