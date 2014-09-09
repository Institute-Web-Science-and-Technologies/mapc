/* Initial beliefs and rules */

/* Plans */

// If our coach cancelled the zone, we go back to start zoning from scratch.
// We also cancel directly leave zoning mode if we triggered this ourselves,
// because we cannot wait for a reply - it might be a matter of life and death.
// Also resets the currentRange.
+!cancelledZoneBuilding[source(Sender)]:
    isMinion(true)
    & (Sender == self
        | bestZone(_, _, _)[source(Sender)]
    )
    <- !resetZoningBeliefs;       
       .print("[zoning][minion] Breaking up my zone because ", Sender, " told me so.");
       !preparedNewZoningRound.

// TODO: minions seem to have forgotten about their bestZone when this get triggered:
+!cancelledZoneBuilding[source(Sender)]:
    isMinion(true)
    <- ?bestZone(_, _, _)[source(Coach)];
       .print("[zoning][minion] I was told to break up my zone but ignoring that. Sender: ", Sender, " My coach: ", Coach).

// If a Coach tells us to build a zone with us but we don't know him, we tell
// him to destroy his zone.
// TODO: it can happen that we deny s.o. to build a zone with us and he does the same to us. Naturally, this sounds quite idiotic but otherwise we wouldn't know, who's the coach. Fix this, when you have free time.
// TODO: be more open with this. Determine whether we aren't a coach and could drop our best zone.
// TODO: be more open with this. Accept better zones instead of telling them to break up.
+zoneGoalVertex(GoalVertex)[source(WannabeCoach)]:
    not bestZone(_, _, _)[source(WannabeCoach)]
    & bestZone(_,_,_)[source(Coach)]
    <- .print("[zoning] ", WannabeCoach, " wanted me in his zone but I'm sworn to ", Coach, ". Telling him to destroy his zone.");
       .send(WannabeCoach, achieve, cancelledZoneBuilding);
       -zoneGoalVertex(GoalVertex)[source(WannabeCoach)].

// Debug message to be sure that there never exists more than one zoneGoalVertex
// TODO: This gets triggered but I would think, it shouldn't.
+zoneGoalVertex(_)[source(_)]:
    .count(zoneGoalVertex(_)[source(_)], Amount)
    & Amount > 1
    <- .print("[zoning][bug] THIS SHOULD NEVER HAPPEN - but it does...").