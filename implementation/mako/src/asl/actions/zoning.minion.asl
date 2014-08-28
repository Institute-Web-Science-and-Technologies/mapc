/* Initial beliefs and rules */

/* Plans */

// if a minion has to leave the zone, it will send its Coach !cancelledZoneBuilding

// Negative zone replies have no meaning for minions. Hence they are ignored.
+negativeZoneReply[source(_)]:
    isMinion(true)
    <- true.

// If we got a zoneGoalVertex, which is a node we should move to to build a
// zone, we will move there. If we'll reach it in the next step, we'll set a
// flag.
// This method is used by minions and coaches alike. Coaches will only have to
// to 0-1 steps to reach their goal though.
// TODO: this action will probably either be executed too early or too late. Place it at the correct place in agent.asl.
+!doAction:
    zoneGoalVertex(GoalVertex)
    & position(PositionVertex)
    & GoalVertex \== PositionVertex
    <- ia.getBestHopToVertex(PositionVertex, GoalVertex, NextHop);
       if (GoalVertex == NextHop) {
           -zoneGoalVertex(GoalVertex)[source(_)];
           -+zoneNode(GoalVertex);
       };
       // TODO: if Sergey merges his branch down, this will become -+intendedAction(goto(NextHop))
       goto(NextHop).

// If our coach cancelled the zone, we go back to start zoning from scratch.
// We also cancel directly leave zoning mode if we triggered this ourselves,
// because we cannot wait for a reply – it might be a matter of life and death.
+!cancelledZoneBuilding[source(Sender)]:
    isMinion(true)
    & (Sender == self
        | bestZone(_, _, _)[source(Sender)]
    )
    <- -+isMinion(false);
       !preparedNewZoningRound.