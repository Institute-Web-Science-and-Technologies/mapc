/* Initial beliefs and rules */

/* Plans */

// If our coach cancelled the zone, we go back to start zoning from scratch.
// We also cancel directly leave zoning mode if we triggered this ourselves,
// because we cannot wait for a reply – it might be a matter of life and death.
// Also resets the currentRange.
+!cancelledZoneBuilding[source(Sender)]:
    isMinion(true)
    & (Sender == self
        | bestZone(_, _, _)[source(Sender)]
    )
    <- -+isMinion(false);
    
       ?defaultRangeForSingleZones(Range);
       -+currentRange(Range);
       -zoneGoalVertex(_)[source(_)]; // Sender should be the only source
       
       !preparedNewZoningRound.

// If a Coach tells us to build a zone with us but we are already a coach
// ourselves or are already going to a zone node as a minion, tell the wannabe
// coach to stop building his zone.
// TODO: it can happen that we deny s.o. to build a zone with us and he does the same to us. Naturally, this sounds quite idiotic but otherwise we wouldn't know, who's the coach. Fix this, when you have free time.
+zoneGoalVertex(GoalVertex)[source(Coach)]:
    isCoach(true)
        | (zoneGoalVertex(FormerGoalVertex)
           & FormerGoalVertex \== GoalVertex
        )
    <- .print("[zoning] ", Coach, " wanted me in his zone but I'm already busy. Telling him to destroy his zone.");
       .send(Coach, achieve, cancelledZoneBuilding);
       -zoneGoalVertex(GoalVertex).