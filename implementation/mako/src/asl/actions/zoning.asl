{ include("../actions/zoning.replies.asl")}
{ include("../actions/zoning.minion.asl") }
{ include("../actions/zoning.coach.asl") }
{ include("../actions/zoning.periodicTriggers.asl") }
{ include("../actions/zoning.quitting.asl") }

/* Initial beliefs and rules */
isCoach(false).
isMinion(false).
isLocked(false).
isAvailableForZoning :- isCoach(false) & isMinion(false) & isLocked(false) & zoneMode(true).
isInZoningRound :- isCoach(false) & isMinion(false) & isLocked(true) & zoneMode(true).
// This belief expresses the number of steps we plan to invest for getting and
// staying in a zone.
maxZoneTimeInSteps(15).
defaultRange(1).
maxRange(15).
maxZoningRoundTimeInSteps(5).

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
    <- .print("[zoning][minion] Disoriented minion was told to start zoning again.");
       !resetZoningBeliefs;
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
	<- -+isLocked(true);
       -+currentTimeInZoningRound(1);
	   ia.registerForZoning;
	   !clearedZoningPercepts;
	   !builtZone.

// Agents that are still happily zoning or doing something else, don't have to
// do anything.
+!preparedNewZoningRound.

// Clear all percepts which are generated during zone building and formation.
+!clearedZoningPercepts
    <- .abolish(bestZone(_, _, _)[source(_)]);
       .abolish(broadcastAcknowledgement[source(_)]);
       .abolish(zoneNode(_)[source(_)]);
       .abolish(foreignBestZone(_, _, _, _)[source(_)]);
       .abolish(bestZoneRequest(_)[source(_)]);
       .abolish(zoneGoalVertexProposal(_, _, _, _)[source(_)]).

// The agent is now looking for possible zones to build around him. It will
// retrieve the best in his 1HNH (short for: one-hop-neighbourhood) and start
// broadcasting it.
@determineMyBestZone
+!builtZone:
    position(PositionVertex)
    & isInZoningRound
    // ask for best zone in his 1HNH (if any)
    & currentRange(Range)
    & ia.getBestZone(PositionVertex, Range, Value, CentreNode, ClosestAgents)
    <- // trigger broadcasting:
       +bestZone(Value, CentreNode, ClosestAgents)[source(self)];
       .print("DEBUG: bestZone(Value=", Value, ",CentreNode=", CentreNode, ",ClosestAgents=", ClosestAgents, ")");
       ia.generateId(Id);
       .broadcast(tell, foreignBestZone(Id, Value, CentreNode, ClosestAgents)).

// No zone could be found in this agent's 1HNH that could have been built with
// the currently available amount of agents. We need to ask others for zones.
+!builtZone:
    isInZoningRound
    <- ia.generateId(Id);
       .broadcast(tell, bestZoneRequest(Id)).
    
// if we receive a builtZone achievement goal, but were are not available for
// zoning, do nothing
+!builtZone.

// This agent becomes a coach because the bestZone is his. He will inform his
// minions about it and move to the CentreNode.
// Removing zoneGoalVertex from self makes sure we stop going to the one-agent-
// zone.
@becomeACoach[atomic]
+!choseZoningRole:
    bestZone(_, _, _)[source(self)]
    & isInZoningRound
    <- -zoneGoalVertex(_)[source(self)];
       -+isCoach(true);
       .print("[zoning][coach] I'm now a coach.");
       !assignededAgentsTheirPosition.

// We aren't this round's coach. But we are a minion.
// Removing zoneGoalVertex from self makes sure we stop going to the one-agent-
// zone.
@becomeAMinion[atomic]
+!choseZoningRole:
    .my_name(MyName)
    & bestZone(_, _, ClosestAgents)
    & .member(MyName, ClosestAgents)
    & isInZoningRound
    <- -zoneGoalVertex(_)[source(self)];
       -+isMinion(true);
       .print("[zoning][minion] I'm now a minion.").

// If there actually was no best zone, then all agents couldn't find a zone in
// their 1HNH that could be built. We go to a well and wait either until s/o
// else triggers us for a new round or the next step begins.
@noBestZoneExisting
+!choseZoningRole:
    not bestZone(_, _, _)
    & currentRange(Range)
    & position(Position)
    & isInZoningRound
    <- ia.getNextBestValueVertex(Position, Range, GoalVertex);
       -+zoneGoalVertex(GoalVertex)[source(self)];
       
       ?maxRange(MaxRange);
       if (Range < MaxRange) {
         IncreasedRange = Range + 1;
         -+currentRange(IncreasedRange);
       };
       
       -+isLocked(false);
       .print("[zoning] No zone was found this round. Going to ", GoalVertex, " in range of ", Range).

// A zone was properly built but this agent wasn't part of it. He'll try his
// luck with a new round of zoning when the coach allows him to. This is to
// ensure that the agents needed for zoning are successfully unregistered from
// the available zoners.
@waitingForNextZoneRound
+!choseZoningRole:
    isInZoningRound
    <- -+isLocked(false);
       .print("[zoning] I'm waiting for any zone to be build and me being woken up.").

// Break up single zoner zones when removing this goal vertex while not being
// a minion or coach.
-zoneGoalVertex(Vertex):
    isMinion(false)
    & isCoach(false)
    <- .print("[zoning] Destroyed 1-agent-zone at ", Vertex);
       ia.destroyZone(Vertex, 1).

// This means nothing to a non zoner.
+!cancelledZoneBuilding:
    isCoach(false)
    & isMinion(false).

// Reset the current range, remove role percepts, unlock and remove goal vertex
// if any. Called when zones break up.
+!resetZoningBeliefs:
    defaultRange(Range)
    <- -+isLocked(false);
       .abolish(zoneGoalVertex(_)[source(_)]); 
       -+currentRange(Range);
       -+isMinion(false);
       -+isCoach(false);.