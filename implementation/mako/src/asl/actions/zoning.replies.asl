// Inform the sender about our zone - if we still know our zone.
+bestZoneRequest[source(Sender)]:
    isAvailableForZoning
    & bestZone(ZoneValue, CentreNode, ClosestAgents)[source(self)]
    <- +zoneReply[source(Sender)];
       .send(Sender, tell, foreignBestZone(ZoneValue, CentreNode, ClosestAgents));
       !receivedAllReplies.

// Reply "no", clear possible duplicate beliefs and check if we are done
// waiting.
+bestZoneRequest[source(Sender)]:
    isAvailableForZoning
    <- +zoneReply[source(Sender)];
       .send(Sender, tell, negativeZoneReply);
       !receivedAllReplies.

// Reply "no" in any other case as a non-zoner.
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
    <- +zoneReply[source(Coach)];
       +bestZone(Value, CentreNode, ClosestAgents)[source(Coach)];
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
    <- +zoneReply[source(Coach)];
       .send(FormerCoach, tell, negativeZoneReply);
       -bestZone(FormerZoneValue, FormerZoneCentreNode, BestZoneClosestAgents)[source(FormerCoach)]; // or use .abolish(bestZone(_,_,_))
       +bestZone(Value, CentreNode, ClosestAgents)[source(Coach)];
       !receivedAllReplies.

// We were informed about a worse zone.
+foreignBestZone(_, _, _)[source(Sender)]:
    isAvailableForZoning
    <- +zoneReply[source(Sender)];
       .send(Sender, tell, negativeZoneReply);
       !receivedAllReplies.

// It doesn't matter if we aren't interested in zoning. We have to reply no in
// any case.
+foreignBestZone(_, _, _)[source(Sender)]
    <- .send(Sender, tell, negativeZoneReply).

//  We will test if we now got replies from every agent.
+negativeZoneReply[source(Sender)]:
    isAvailableForZoning
    <- +zoneReply[source(Sender)];
       !receivedAllReplies.

// We aren't zoning so we ignore this message.
+negativeZoneReply[source(_)].

// If all agents have replied with either a broadcast or a refusal, we're done
// waiting.
//
// We also set a lock to make sure that this does not get interrupted by
// further messages and for coaches so that assignededAgentsTheirPosition can be
// called without the periodic zone breakup interfering with it.
@testForAllReplies[priority(1), atomic]
+!receivedAllReplies:
    isAvailableForZoning
    & .count(zoneReply[source(_)], RepliesAmount)
    & broadcastAgentList(BroadcastList)
    & .length(BroadcastList, AgentsAmount)
    & AgentsAmount ==  RepliesAmount
    <- -+isLocked(true);
       !choseZoningRole.

// TODO: remove this goal if we are sure it never gets called.
@miscountedReplies
+!receivedAllReplies:
    isAvailableForZoning
    & .count(zoneReply[source(_)], RepliesAmount)
    & broadcastAgentList(BroadcastList)
    & .length(BroadcastList, AgentsAmount)
    & AgentsAmount <  RepliesAmount
    <- .print("[zoning] If you can read this, then the code does not work properly. We counted more replies than agents.").
       
// We still have to wait and fail this achievement goal silently as true.
+!receivedAllReplies.