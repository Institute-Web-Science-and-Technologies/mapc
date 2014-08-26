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
// TODO [prio:high, #28]: !foundNewZone achievement goal needs to be written. It expresses that an agent should either ask for expendable zones or start zoning anew. Its stub is located at the bottom of this file.
// TODO [prio:medium, #26]: we must modify plannedZoneTimeInSteps over time. Else the agent might never agree on zones farer away.
// TODO [prio:medium, #27]: when we have reached the plannedZoneTimeInSteps, we should start zoning again (needs to increment a counter in each step) – also see the zone movement idea which is located lower in this TODO list.
// TODO [prio:low]: is zoneMode really only set once? Else, we will have to lock the zoneMode(true) belief trigger with a mustCommunicateZones(true) belief.
// TODO [prio:low, #27]: search our 1HNH only for zones which need at max .count(idleZoner(_),X)+1 many agents. This could be problematic as idleZoner is extremely dynamic.
// TODO [prio:medium, #28]: in zoning.coach.asl we tell Minions we don't need to search a different zone. Instead, we could have them extend our zone (if possible). The corresponding belief triggered event is marked with a TODO.
// TODO [prio:low, #27]: when a zone has been established, each zoner should look at zones with maximum .count(zoner,X) many nodes. If there is a better option, the whole zone or parts of it may be moved onto the better nearby zone.
/* Plans */

// Zoning mode has begun and it will trigger the achievement goal builtZone.
+zoneMode(true)
    <- !builtZone(false).

// The agent is now looking for possible zones to build around him. It will
// retrieve the best in his 1HNH (short for: one-hop-neighbourhood) and start
// broadcasting it. It will also register as an idleZoner.
//
// The Asynchronous parameter is a switch to distinguish between the initial
// broadcast storm that sets in when zoneMode is reached and all other calls
// later on. Asynchronous may only be set to false once when zoneMode begins.
+!builtZone(Asynchronous):
    position(PositionVertex)
    & broadcastAgentList(BroadcastList) //TODO: replace with internal action for getting idleZoner
    & .my_name(MyName)
    <- // tell all agents that I am ready to build zones:
      .send(BroadcastList, tell, idleZoner(MyName));
       
      // start over new: clear all previous beliefs:
      -bestZone(_, _, _, _)[source(_)];
      -positiveZoneReply(_)[source(_)];
      -negativeZoneReply(_)[source(_)];
      -zoneGoalVertex(_)[source(_)];
      -zoneNode(_)[source(_)];
      -foreignBestZone(_, _, _, _)[source(_)];
      -minions(_)[source(_)];
      -closestAgents(_)[source(_)];
       
      // ask Vertex for its zone as well as its neighbours:
      ia.getBestZone(PositionVertex, 1, Value, CentreNode, UsedNodes, ClosestAgents);
      // And calculate it in regard to the time we plan to spend for zoning. We
      // assume 1 as a Distance to discount home zones (in 0HNH) at least a bit.
      // Also it spares us from calculating the distance:
      //?plannedZoneTimeInSteps(Steps);
      //ia.calculateLongTermZoneValue(Value, 1, Steps, PrognosedValue);
      // trigger broadcasting:
      +bestZone(Value, CentreNode)[source(self)];
      +closestAgents(ClosestAgents)[source(Self)];
      +minions(ClosestAgents);      
      if (Asynchronous) {
        .send(BroadcastList, tell, asyncForeignBestZone(Value, CentreNode));
      } else { // Initial broadcast storm when +zoneMode(true):
        .send(BroadcastList, tell, foreignBestZone(Value, CentreNode));
      }.

// We received an asynchronous foreignBestZone percept. Chances are, the
// Broadcaster doesn't know about our zone so we tell him as we haven't started
// building one yet. We also deal with his zone information.
+asyncForeignBestZone(Value, CentreNode, UsedNodes, ClosestAgents)[source(Broadcaster)]:
    isAvailableForZoning
    & bestZone(BestZoneValue, BestZoneCentreNode)[source(self)]
    <- .send(BroadcastList, tell, foreignBestZone(BestZoneValue, BestZoneCentreNode));
       +foreignBestZone(Value, CentreNode)[source(Coach)];
       ia.getBestZone(PositionVertex, 1, Value, CentreNode, UsedNodes, ClosestAgents);
       -+closestAgents(ClosestAgents).

// We received an asynchronous foreignBestZone percept but don't know about our
// own zone anymore as it was worse. Hence, we cannot tell him our zone but we
// still take his zone into consideration when choosing the best one for us.
+asyncForeignBestZone(Value, CentreNode)[source(Broadcaster)]:
    isAvailableForZoning
    <- +foreignBestZone(Value, CentreNode)[source(Coach)]
    ia.getBestZone(PositionVertex, 1, Value, CentreNode, UsedNodes, ClosestAgents);
    -+closestAgents(ClosestAgents).

// We have received a zone better than the one we know (or
// didn't know any), so we throw our zone away
// We also calculate the distance to it and cache that value
// as a belief. We discard better zones that are too far away.
//
// We also tell our former coach that we are no longer interested in his zone!
@onlyAddOneBetterZoneAtATime[atomic]
+foreignBestZone(Value, CentreNode)[source(Coach)]:
    isAvailableForZoning
    & position(PositionVertex)
    //& ia.getDistance(PositionVertex, CentreNode, Distance)
    //& plannedZoneTimeInSteps(Steps)
    //& ia.calculateLongTermZoneValue(Value, Distance, Steps, PrognosedValue)
    & bestZone(FormerZoneValue, FormerZoneCentreNode)[source(FormerCoach)]
    // my zone is worse:
    & FormerValue < Value
    // or the zones are identical but my name is alphabetically bigger:
    | (FormerValue == Value
        & .my_name(MyName)
        & .sort([Coach, MyName], [Coach, MyName])
    )
    <- //.send(FormerCoach, tell, negativeZoneReply(FormerZoneCentreNode));
       -bestZone(FormerZoneValue, FormerZoneCentreNode)[source(FormerCoach)]; // or use .abolish(bestZone(_,_,_))
       +bestZone(Value, CentreNode)[source(Coach)];
       ia.getBestZone(PositionVertex, 1, Value, CentreNode, UsedNodes, ClosestAgents);
       -+closestAgents(ClosestAgents);
       !receivedAllReplies.

// We were informed about a worse zone.
+foreignBestZone(_, CentreNode)[source(Sender)]:
    isAvailableForZoning
    <- //.send(Sender, tell, negativeZoneReply(CentreNode));
       !receivedAllReplies.

// Don't count the negative reply AND the broadcast of the same sender in the
// !receivedAllReplies[2] achievement goal.
//+negativeZoneReply(_)[source(Sender)]:
//    isAvailableForZoning
//    & foreignBestZone(_, _, _, _)[source(Sender)]
//    <- -foreignBestZone(_, _, _, _)[source(Sender)];
//       !receivedAllReplies.

// We only got a negative zone reply. We will test if we now got replies from
// every agent.
//+negativeZoneReply(_)[source(_)]:
//    isAvailableForZoning
//    <- !receivedAllReplies.

// If all idleZoners have replied, we will have the best zone stored.
// We will then choose our role for this zoning part (Coach vs. Minion).
+!receivedAllReplies:
    .count(foreignBestZone(_, _), BroadcastRepliesAmount)
    & ia.getIdleZoner(AvailableZoners)
    & BroadcastRepliesAmount == .length(AvailableZoner)
    <- !choseZoningRole.

// Or if all agents have replied with either a broadcast or a refusal. This
// achievement goal is intended for zoners who appear after the first broadcast
// storm.
+!receivedAllReplies:
    .count(foreignBestZone(_, _), BroadcastRepliesAmount)
    & .count(broadcastAgentList(BroadcastList), AgentsAmount) // or use 28 as a static measure :Þ
    & AgentsAmount ==  BroadcastRepliesAmount
    & isCoach(false)
    & isMinion(false)
    <- !choseZoningRole.

// We still have to wait and fail this achievement goal silently as true.
+!receivedAllReplies
    <- true.

// If we get told (or decide ourselves) to cancel our current process and start
// a different approach.
// The current behaviour is just a placeholder.
+!foundNewZone
    <- !cancelledZoneBuilding.