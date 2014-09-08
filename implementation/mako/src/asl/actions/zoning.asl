{ include("../actions/zoning.replies.asl")}
{ include("../actions/zoning.minion.asl") }
{ include("../actions/zoning.coach.asl") }

/* Initial beliefs and rules */
isCoach(false).
isMinion(false).
isLocked(false).
isAvailableForZoning :- isCoach(false) & isMinion(false) & isLocked(false) & zoneMode(true).
// This belief expresses the number of steps we plan to invest for getting and
// staying in a zone.
plannedZoneTimeInSteps(15).
defaultRangeForSingleZones(1).

//TODO: maybe make a cut if Range gets too high. Higher than 5 sounds high.

/* Plans */

// Zoning mode has begun and it will trigger the achievement goal
// preparedNewZoningRound if an agent is interested in zoning. This belief is
// set by the corresponding agents themselves.
// Initialise the range we use to look for single zones.
+zoneMode(true):
    isAvailableForZoning
    & .my_name(MyName)
    & defaultRangeForSingleZones(Range)
    <- -+currentRange(Range);
       !preparedNewZoningRound.
    
// After some agents formed a zone a new round of zoning for all the other
// agents will start. We allow locked agents in here as long as they are not
// zoners. This allows us to remove the locks on agents that waited for the
// previous zoners to finish unregistering from available zoners.
//
// Before starting to build any zone, register to the JavaMap to be an available
// zoner.
+!preparedNewZoningRound:
    isCoach(false)
    & isMinion(false)
    & zoneMode(true)
    & .my_name(MyName)
	<- ia.registerForZoning(MyName);
	// TODO unregister if you want to quit zoning but are not in a zone
	   !clearedZoningPercepts;
	   !builtZone.

// Agents that are still happily zoning or doing something else, don't have to
// do anything.
+!preparedNewZoningRound.

// Clear all percepts which are generated during zone building and formation.
+!clearedZoningPercepts
    <- -zoneReply[source(_)];
       -bestZone(_, _, _)[source(_)];
       -negativeZoneReply[source(_)];
       -zoneNode(_)[source(_)];
       -asyncForeignBestZone(_, _, _)[source(_)];
       -foreignBestZone(_, _, _)[source(_)];
       -bestZoneRequest[source(_)];
       -+isLocked(false).

// The agent is now looking for possible zones to build around him. It will
// retrieve the best in his 1HNH (short for: one-hop-neighbourhood) and start
// broadcasting it. It will also register as an idleZoner.
//
// When zoning, do it asynchronously because we might be too late and have
// mistakenly deleted percepts from the new zoning round. Doing !builtZone
// asynchronously then ensures that we will get any left over zone information
// and in the end will know about the overall best zone.
+!builtZone:
    position(PositionVertex)
    & isAvailableForZoning
    & .my_name(MyName)
    // ask for best zone in his 1HNH (if any)
    & currentRange(Range)
    & ia.getBestZone(PositionVertex, Range, Value, CentreNode, ClosestAgents)
    & broadcastAgentList(BroadcastList)
    <- // trigger broadcasting:
       +bestZone(Value, CentreNode, ClosestAgents)[source(self)];
       .send(BroadcastList, tell, asyncForeignBestZone(Value, CentreNode, ClosestAgents)).

// No zone could be found in this agent's 1HNH that could have been built with
// the currently available amount of agents. We need to ask others for zones.
+!builtZone:
    broadcastAgentList(BroadcastList)
    & isAvailableForZoning
    <- .send(BroadcastList, tell, bestZoneRequest).
    
// if we receive a builtZone achievement goal, but were are not available for
// zoning, do nothing
+!builtZone.

// This agent becomes a coach because the bestZone is his. He will inform his
// minions about it and move to the CentreNode.
// Removing zoneGoalVertex from self makes sure we stop going to the one-agent-
// zone.
@becomeACoach[priority(2)]
+!choseZoningRole:
    bestZone(_, _, _)[source(self)]
    <- -+isCoach(true);
       -zoneGoalVertex(_)[source(self)];
       .print("[zoning] I'm now a coach.");
       !assignededAgentsTheirPosition.

// We aren't this round's coach. But we are a minion.
// Removing zoneGoalVertex from self makes sure we stop going to the one-agent-
// zone.
@becomeAMinion[priority(2)]
+!choseZoningRole:
    .my_name(MyName)
    & bestZone(_, _, ClosestAgents)
    & .member(MyName, ClosestAgents)
    <- -+isMinion(true);
       -zoneGoalVertex(_)[source(self)];
       .print("[zoning] I'm now a minion.").

// If there actually was no best zone, then all agents couldn't find a zone in
// their 1HNH that could be built. We try zoning again the hope it will get
// better but TODO: we should be smarter and start going to wells or so because trying zoning again will most likely not help!?
@noBestZoneExisting[priority(2)]
+!choseZoningRole:
    not bestZone(_, _, _)
    & currentRange(Range)
    & position(Position)
    <- ia.getNextBestValueVertex(Position, Range, GoalVertex);
       -+zoneGoalVertex(GoalVertex);
       IncreasedRange = Range + 1;
       -+currentRange(IncreasedRange);
       
      .print("[zoning] No zone was found this round. Going to ", GoalVertex, " in range of ", Range);
      // TODO: we should probably make sure that this happens only after a step?:
      !preparedNewZoningRound.

// A zone was properly built but this agent wasn't part of it. He'll try his
// luck with a new round of zoning when the coach allows him to. This is to
// ensure that the agents needed for zoning are successfully unregistered from
// the available zoners.
@waitingForNextZoneRound[priority(2)]
+!choseZoningRole.

// This means nothing to a non zoner.
+!cancelledZoneBuilding:
    isAvailableForZoning.

// Reset the current range, remove role percepts, unlock and remove goal vertex
// if any. Called when zones break up.
+!resetZoningBeliefs:
    defaultRangeForSingleZones(Range)
    <- -+isMinion(false);
       -+isCoach(false);
       -+isLocked(false);
       -zoneGoalVertex(_)[source(_)]; 
       -+currentRange(Range).