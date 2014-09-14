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
       .print("[zoning][minion] Breaking up my zone because my coach ", Sender, " told me to.");
       !preparedNewZoningRound.

// If someone other than our coach tries to cancel the zone, we ignore it.
// TODO: at times, minions forgot about their bestZone. I have no idea, how this could happen.
+!cancelledZoneBuilding[source(Sender)]:
    isMinion(true)
    & bestZone(_, _, _)[source(Coach)]
    <- .print("[zoning][minion] I was told to break up my zone but ignoring that. Sender: ", Sender, " My coach: ", Coach).

// TODO: this should never be called but it is; see #38.
+!cancelledZoneBuilding[source(Sender)]:
    isMinion(true)
    <- .print("[zoning][minion][bug] I forgot about my bestZone belief. I have no idea how this can happen. Restarting zoning");
       !resetZoningBeliefs;
       !preparedNewZoningRound.

// Although this agent didn't know his role yet (or anymore?), his coach told
// him to break up his zone. He then continues to break up his zone like a
// minion would.
+!cancelledZoneBuilding[source(Sender)]:
    isLocked(true)
    & isMinion(false)
    & isCoach(false)
    & zoneMode(true)
    & bestZone(_, _, _)[source(Sender)]
    <- -+isMinion(true);
       !cancelledZoneBuilding;
       .print("[zoning][minion] I hadn't been a minion but was locked, when my coach ", Sender, " told me to break up my zone.").

// This probably gets called throughout the time when an agent has to decide
// his role and gets locked for it but hasn't decided yet. It must be ignored
// as long as the sender is not the actual (to-be-) coach.
+!cancelledZoneBuilding[source(Sender)]:
    isLocked(true)
    & isMinion(false)
    & isCoach(false)
    & zoneMode(true).

// This only happens when a coach sends this to himself. It is in this file
// nevertheless to ensure the correct execution order.
+zoneGoalVertexProposal(_, _, _, GoalVertex)[source(self)]:
    zoneMode(true)
    <- .abolish(zoneGoalVertex(_)[source(_)]);
       +zoneGoalVertex(GoalVertex)[source(Coach)].

// If our coach tells us to move somewhere, we do as we are told. In theory,
// this could happen before we found out that we are going to be a minion. So
// we set this belief manually.
+zoneGoalVertexProposal(_, _, _, GoalVertex)[source(Coach)]:
    zoneMode(true)
    & bestZone(_, _, _)[source(Coach)]
    <- -+isMinion(true);
       .abolish(zoneGoalVertex(_)[source(_)]);
       +zoneGoalVertex(GoalVertex)[source(Coach)].

// We were told to move to a node by someone who knows of a better zone. We have
// to inform our former zone; exchange our best zone belief as well as our
// zoneGoalVertex.
@switchToBetterZone[atomic]
+zoneGoalVertexProposal(Value, CentreNode, ClosestAgents, GoalVertex)[source(Coach)]:
    zoneMode(true)
    & bestZone(FormerValue, _, _)[source(FormerCoach)]
    // my zone is worse:
    & FormerValue < Value
    // or the zones are identical but my name is alphabetically bigger:
    | (FormerValue == Value
        & .my_name(MyName)
        & .sort([Coach, MyName], [Coach, MyName])
    )
    <- !acceptedZoneGoalVertexProposal;
       -+isMinion(true);
       -+isCoach(false);
       +bestZone(Value, CentreNode, ClosestAgents)[source(Coach)];
       .abolish(bestZone(_, _, _)[source(FormerCoach)]);
       
       .abolish(zoneGoalVertex(_)[source(_)]);
       +zoneGoalVertex(GoalVertex)[source(Coach)];
       .print("[zoning][minion] I'm giving up my zone for ", Coach, "'s zone.").

// Tell off coaches if we aren't zoning anymore.
+zoneGoalVertexProposal(_, _, _, _)[source(Coach)]:
    zoneMode(false)
    <- .send(Coach, achieve, cancelledZoneBuilding).

// A minion who accepts a zoneGoalVertex but was in a zone already has to
// inform his coach.
+!acceptedZoneGoalVertexProposal:
    isMinion(true)
    & bestZone(_, _, _)[source(Coach)]
    <- .send(Coach, achieve, cancelledZoneBuilding).

// A coach who accepts a zoneGoalVertex but was in a zone already has to inform
// all his minions. He also has to switch roles and cancel defence saboteurs.
+!acceptedZoneGoalVertexProposal:
    isCoach(true)
    & .my_name(Coach)
    & bestZone(_, _, ClosestAgents)
    <- !informedSaboteursAboutZoneBreakup;
       .difference(ClosestAgents, [Coach], Minions);
       .send(Minions, achieve, cancelledZoneBuilding).

// If an agent isAvailableForZoning or isInZoningMode, he may be recruited as
// well.
// If he was on a well, it must be reported that he now left it to JavaMap (done
// through removing any matching beliefs).
+!acceptedZoneGoalVertexProposal:
    zoneMode(true)
    <- -zoneGoalVertex(_).

// If a coach wanted to have this agent in his zone but his zone is worse, this
// agent has to tell him that he won't be available.
+zoneGoalVertexProposal(_, _, _, _)[source(WannabeCoach)]
    <- .print("[zoning] ", WannabeCoach, " wanted me in his zone but his zone is worse. Telling him to destroy his zone.");
       .send(WannabeCoach, achieve, cancelledZoneBuilding).