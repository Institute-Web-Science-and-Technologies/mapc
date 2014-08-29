/* Initial beliefs and rules */
isCoach(false).
isMinion(false).
isAvailableForZoning :- isCoach(false) & isMinion(false).
// This belief expresses the number of steps we plan to invest for getting and
// staying in a zone. It is used in !calculateLongTermZoneValue.
plannedZoneTimeInSteps(15).

// @all: If you are looking for something to do, search this file as well as
//       zoning.* for the keyword "TODO".
// TODO [prio:low]: test that the internal actions work properly when JavaMap is merged onto master.
// TODO [prio:medium, #28]: !foundNewZone achievement goal needs to be written. It expresses that an agent should either ask for expendable zones or start zoning anew. Its stub is located at the bottom of this file.
// TODO [prio:high, #26]: we must incorporate plannedZoneTimeInSteps to split up zones after some time.
// TODO [prio:medium, #27]: when we have reached the plannedZoneTimeInSteps, we should start zoning again (needs to increment a counter in each step) – also see the zone movement idea which is located lower in this TODO list.
// TODO [prio:none, #27]: @wontfix search our 1HNH only for zones which need at max .count(idleZoner(_),X)+1 many agents. This could be problematic as idleZoner is extremely dynamic.
// TODO [prio:none, #28]: @wontfix in zoning.coach.asl we could have free agents extend our zone (if possible). — instead, harass enemies and go to wells.
// TODO [prio:low, #27]: when a zone has been established, each zoner should look at zones with maximum .count(zoner,X) many nodes. If there is a better option, the whole zone or parts of it may be moved onto the better nearby zone.
/* Plans */

// Zoning mode has begun and it will trigger the achievement goal builtZone if
// an agent is interested in zoning. This belief is set by the corresponding
// agents themselves.
// Before starting to build any zone, register to the JavaMap to be an available
// zoner.
+zoneMode(true):
    isInterestedInZoning(true)
    & .my_name(MyName)
    <- ia.registerForZoning(MyName);
       !builtZone.
    
// After some agents formed a zone a new round of zoning for all the other
// agents will start. Before that, all previous beliefs regarding zoning will be
// deleted.
//
// Before starting to build any zone, register to the JavaMap to be an available
// zoner.
+!preparedNewZoningRound:
    isInterestedInZoning(true)
    & .my_name(MyName)
	<- ia.registerForZoning(MyName);
	   !clearedZoningPercepts;
	   !builtZone.

// If an agent is not interested in zoning, he will still clear his percepts as
// a precaution.
+!preparedNewZoningRound
    <- !clearedZoningPercepts.

// Clear all percepts which are generated during zone building and formation.
+!clearedZoningPercepts
    <- -bestZone(_, _, _)[source(_)];
       -negativeZoneReply[source(_)];
       -zoneGoalVertex(_)[source(_)];
       -zoneNode(_)[source(_)];
       -foreignBestZone(_, _, _)[source(_)];
       -closestAgents(_)[source(_)];
       -bestZoneRequest[source(_)].

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
    & ia.getBestZone(PositionVertex, 1, Value, CentreNode, ClosestAgents)
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
    
//if we receive a builtZone achievement goal, but were are not available for zoning, do nothing
+!builtZone
    <- true.

// Inform the sender about our zone – if we still know our zone.
+bestZoneRequest[source(Sender)]:
    isAvailableForZoning
    & bestZone(ZoneValue, CentreNode, ClosestAgents)[source(self)]
    <- .send(Sender, tell, foreignBestZone(ZoneValue, CentreNode, ClosestAgents)).

// Reply "no" in any other case – no matter if we are interested in zoning or
// not.
+bestZoneRequest[source(Sender)]
    <- .send(Sender, tell, negativeZoneReply).

// We received an asynchronous foreignBestZone percept. Chances are, the
// Broadcaster doesn't know about our zone so we tell him as we haven't started
// building one yet. We also deal with his zone information.
+asyncForeignBestZone(Value, CentreNode, ClosestAgents)[source(Broadcaster)]:
    isAvailableForZoning
    & bestZone(BestZoneValue, BestZoneCentreNode, BestZoneClosestAgents)[source(self)]
    <- .send(Broadcaster, tell, foreignBestZone(BestZoneValue, BestZoneCentreNode, BestZoneClosestAgents));
       +foreignBestZone(Value, CentreNode, ClosestAgents)[source(Broadcaster)].

// We received an asynchronous foreignBestZone percept but don't know about our
// own zone anymore as it was worse. Hence, we cannot tell him our zone but we
// still take his zone into consideration when choosing the best one for us.
//
// It could even be that we aren't available for zoning. But we will then still
// have to reply (which is done on foreignBestZone percept addition).
+asyncForeignBestZone(Value, CentreNode, ClosestAgents)[source(Broadcaster)]
    <- +foreignBestZone(Value, CentreNode, ClosestAgents)[source(Broadcaster)].

// If we didn't find a bestZone in our 1HNH ourselves, we will thankfully take
// the first foreignBestZone that is offered to us.
@onlyAddOneFirstBestZone[atomic]
+foreignBestZone(Value, CentreNode, ClosestAgents)[source(Coach)]:
    isAvailableForZoning
    & not bestZone(_, _, _)
    <- +bestZone(Value, CentreNode, ClosestAgents)[source(Coach)];
       !receivedAllReplies.

// We have received a zone better than the one we know (or
// didn't know any), so we throw our zone away
// We also calculate the distance to it and cache that value
// as a belief. We discard better zones that are too far away.
//
// We also tell our former coach that we are no longer interested in his zone!
@onlyAddOneBetterZoneAtATime[atomic]
+foreignBestZone(Value, CentreNode, ClosestAgents)[source(Coach)]:
    isAvailableForZoning
    & position(PositionVertex)
    & bestZone(FormerZoneValue, FormerZoneCentreNode, BestZoneClosestAgents)[source(FormerCoach)]
    // my zone is worse:
    & FormerValue < Value
    // or the zones are identical but my name is alphabetically bigger:
    | (FormerValue == Value
        & .my_name(MyName)
        & .sort([Coach, MyName], [Coach, MyName])
    )
    <- .send(FormerCoach, tell, negativeZoneReply);
       -bestZone(FormerZoneValue, FormerZoneCentreNode, BestZoneClosestAgents)[source(FormerCoach)]; // or use .abolish(bestZone(_,_,_))
       +bestZone(Value, CentreNode, ClosestAgents)[source(Coach)];
       !receivedAllReplies.

// We were informed about a worse zone.
+foreignBestZone(_, CentreNode, _)[source(Sender)]:
    isAvailableForZoning
    <- .send(Sender, tell, negativeZoneReply);
       !receivedAllReplies.

// It doesn't matter if aren't interested in zoning. We have to reply no in any
// case.
+foreignBestZone(_, CentreNode, _)[source(Sender)]
    <- .send(Sender, tell, negativeZoneReply).

// Don't count the negative reply AND the broadcast of the same sender in the
// !receivedAllReplies[1] achievement goal.
+negativeZoneReply[source(Sender)]:
    isAvailableForZoning
    & foreignBestZone(_, _, _)[source(Sender)]
    <- -foreignBestZone(_, _, _)[source(Sender)];
       !receivedAllReplies.

// We only got a negative zone reply. We will test if we now got replies from
// every agent.
+negativeZoneReply[source(_)]:
    isAvailableForZoning
    <- !receivedAllReplies.

// If all agents have replied with either a broadcast or a refusal, we're done
// waiting.
+!receivedAllReplies:
    isAvailableForZoning
    & .count(foreignBestZone(_, _, _), BroadcastRepliesAmount)
    & .count(negativeZoneReply, RefusalAmount)
    & .count(bestZoneRequest, BestZoneRequest)
    & broadcastAgentList(BroadcastList)
    & .count(BroadcastList, AgentsAmount) // or use 28 as a static measure :Þ
    & AgentsAmount ==  BroadcastRepliesAmount + RefusalAmount + BestZoneRequest
    <- !choseZoningRole.

// We still have to wait and fail this achievement goal silently as true.
+!receivedAllReplies
    <- true.

// If we get told (or decide ourselves) to cancel our current process and start
// a different approach.
// The current behaviour is just a placeholder.
+!foundNewZone
    <- !cancelledZoneBuilding.

// This agent becomes a coach because the bestZone is his. He will inform his
// minions about it and move to the CentreNode.
@becomeACoach
+!choseZoningRole:
    bestZone(_, _, _)[source(self)]
    <- -+isCoach(true);
       !assignededAgentsTheirPosition.

// We aren't this round's coach. But we are a minion.
@becomeAMinion
+!choseZoningRole:
    .my_name(MyName)
    & bestZone(_, _, ClosestAgents)[source(self)]
    & .member(MyName, ClosestAgents)
    <- +isMinion(true).

// If there actually was no best zone, then all agents couldn't find a zone in
// their 1HNH that could be built. We try zoning again the hope it will get
// better but TODO: we should be smarter and start going to wells or so because trying zoning again will most likely not help!?
@noBestZoneExisting
+!choseZoningRole:
    not bestZone(_, _, _)
    <- !preparedNewZoningRound.

+!choseZoningRole
    <- true.