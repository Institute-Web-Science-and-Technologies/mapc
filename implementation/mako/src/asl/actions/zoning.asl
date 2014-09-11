{ include("../actions/zoning.replies.asl")}
{ include("../actions/zoning.minion.asl") }
{ include("../actions/zoning.coach.asl") }
{ include("../actions/zoning.periodicTriggers.asl") }

/* Initial beliefs and rules */
isCoach(false).
isMinion(false).
isLocked(false).
isAvailableForZoning :- isCoach(false) & isMinion(false) & isLocked(false) & zoneMode(true).
isInZoningRound :- isCoach(false) & isMinion(false) & isLocked(true) & zoneMode(true).
// This belief expresses the number of steps we plan to invest for getting and
// staying in a zone.
plannedZoneTimeInSteps(15).
defaultRange(1).

//TODO: maybe make a cut if Range gets too high. Higher than 5 sounds high.

/* Plans */

// Zoning mode has begun and it will trigger the achievement goal
// preparedNewZoningRound if an agent is interested in zoning. This belief is
// set by the corresponding agents themselves.
// Initialise the range we use to look for single zones.
+zoneMode(true):
    isAvailableForZoning
    & defaultRange(Range)
    <- -+currentRange(Range);
       !preparedNewZoningRound.

// Minions can enter this method because the round's winning coach will only
// tell all losing agents to build a new zone. If this agent thought he was
// destined to be a minion, he might wait forever for a coach to tell him what
// to do. So as he gets told by a coach, it must mean that he was not a minion
// of this round. Checking for zoneNodes or zoneGoalVertices ensures that he
// wasn't a minion of an earlier, successful zone formation either.
+!preparedNewZoningRound[source(Coach)]:
    isMinion(true)
    & not zoneNode(_)
    & not zoneGoalVertex(_)
    <- !resetZoningBeliefs;
       !preparedNewZoningRound.

// After some agents formed a zone a new round of zoning for all the other
// agents will start. We allow locked agents in here as long as they are not
// zoners. These locks are being reused to prevent a foreignBestzone conflicting
// with the bestZone we are going to determine in !builtZone.
//
// Before starting to build any zone, register to the JavaMap to be an available
// zoner.
+!preparedNewZoningRound:
    isAvailableForZoning
    & .my_name(MyName)
	<- -+isLocked(true);
	   ia.registerForZoning(MyName);
	// TODO unregister if you want to quit zoning but are not in a zone
	   !clearedZoningPercepts;
	   !builtZone.

// Agents that are still happily zoning or doing something else, don't have to
// do anything.
+!preparedNewZoningRound.

// Clear all percepts which are generated during zone building and formation.
+!clearedZoningPercepts
    <- .abolish(bestZone(_, _, _)[source(_)]);
       -broadcastAcknowledgement[source(_)];
       .abolish(zoneNode(_)[source(_)]);
       .abolish(foreignBestZone(_, _, _)[source(_)]);
       -bestZoneRequest[source(_)];
       .abolish(zoneGoalVertexProposal(_, _, _, _)[source(_)]).

// The agent is now looking for possible zones to build around him. It will
// retrieve the best in his 1HNH (short for: one-hop-neighbourhood) and start
// broadcasting it.
@determineMyBestZone[priority(3)]
+!builtZone:
    position(PositionVertex)
    & isInZoningRound
    // ask for best zone in his 1HNH (if any)
    & currentRange(Range)
    & ia.getBestZone(PositionVertex, Range, Value, CentreNode, ClosestAgents)
    <- // trigger broadcasting:
       +bestZone(Value, CentreNode, ClosestAgents)[source(self)];
       .broadcast(tell, foreignBestZone(Value, CentreNode, ClosestAgents)).

// No zone could be found in this agent's 1HNH that could have been built with
// the currently available amount of agents. We need to ask others for zones.
+!builtZone:
    isInZoningRound
    <- .broadcast(tell, bestZoneRequest).
    
// if we receive a builtZone achievement goal, but were are not available for
// zoning, do nothing
+!builtZone.

// This agent becomes a coach because the bestZone is his. He will inform his
// minions about it and move to the CentreNode.
// Removing zoneGoalVertex from self makes sure we stop going to the one-agent-
// zone.
@becomeACoach[priority(2), atomic]
+!choseZoningRole:
    bestZone(_, _, _)[source(self)]
    & isInZoningRound
    <- -+isCoach(true);
       -zoneGoalVertex(_)[source(self)];
       .print("[zoning][coach] I'm now a coach.");
       !assignededAgentsTheirPosition.

// We aren't this round's coach. But we are a minion.
// Removing zoneGoalVertex from self makes sure we stop going to the one-agent-
// zone.
@becomeAMinion[priority(2), atomic]
+!choseZoningRole:
    .my_name(MyName)
    & bestZone(_, _, ClosestAgents)
    & .member(MyName, ClosestAgents)
    & isInZoningRound
    <- -+isMinion(true);
       -zoneGoalVertex(_)[source(self)];
       .print("[zoning][minion] I'm now a minion.").

// If there actually was no best zone, then all agents couldn't find a zone in
// their 1HNH that could be built. We go to a well and wait either until s/o
// else triggers us for a new round or the next step begins.
@noBestZoneExisting[priority(2)]
+!choseZoningRole:
    not bestZone(_, _, _)
    & currentRange(Range)
    & position(Position)
    & isInZoningRound
    <- ia.getNextBestValueVertex(Position, Range, GoalVertex);
       -+zoneGoalVertex(GoalVertex)[source(_)];
       IncreasedRange = Range + 1;
       -+currentRange(IncreasedRange);
       
       -+isLocked(false);
       .print("[zoning] No zone was found this round. Going to ", GoalVertex, " in range of ", Range).

// A zone was properly built but this agent wasn't part of it. He'll try his
// luck with a new round of zoning when the coach allows him to. This is to
// ensure that the agents needed for zoning are successfully unregistered from
// the available zoners.
@waitingForNextZoneRound[priority(2)]
+!choseZoningRole:
    isInZoningRound
    <- -+isLocked(false);
       .print("[zoning] I'm waiting for any zone to be build and me being woken up.").

// This means nothing to a non zoner.
+!cancelledZoneBuilding:
    isAvailableForZoning.

// Reset the current range, remove role percepts, unlock and remove goal vertex
// if any. Called when zones break up.
+!resetZoningBeliefs:
    defaultRange(Range)
    <- -+isMinion(false);
       -+isCoach(false);
       -+isLocked(false);
       .abolish(zoneGoalVertex(_)[source(_)]); 
       -+currentRange(Range).