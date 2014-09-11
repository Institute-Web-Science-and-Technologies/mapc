// Inform the sender about our zone - if we still know our zone.
// We also directly set the acknowledgement because we know the receiver won't
// reply anymore as he can't have a better zone himself.
+bestZoneRequest(_)[source(Sender)]:
    isInZoningRound
    & bestZone(ZoneValue, CentreNode, ClosestAgents)[source(self)]
    <- ia.generateId(Id);
       .send(Sender, tell, foreignBestZone(Id, ZoneValue, CentreNode, ClosestAgents));
       +broadcastAcknowledgement[source(Sender)].

// Acknowledge receiving of the message and check if we are done waiting.
+bestZoneRequest(_)[source(Sender)]:
    isInZoningRound
    <- .send(Sender, tell, broadcastAcknowledgement);
       +broadcastAcknowledgement[source(Sender)].

// Just acknowledge this message but don't do anything with it as a non-zoner.
+bestZoneRequest(_)[source(Sender)]
    <- .send(Sender, tell, broadcastAcknowledgement).

// If we didn't find a bestZone in our 1HNH ourselves, we will thankfully take
// the first foreignBestZone that is offered to us.
//
// Although it is highly likely that this message was a reply to a
// bestZoneRequest, we can't be sure. It could have after this agent was ready
// for zoning and prior to him determining his own best zone. Hence, an
// acknowledgement is needed.
@onlyAddOneFirstBestZone[atomic]
+foreignBestZone(_, Value, CentreNode, ClosestAgents)[source(Coach)]:
    isInZoningRound
    & not bestZone(_, _, _)
    <- .send(Coach, tell, broadcastAcknowledgement);
       +bestZone(Value, CentreNode, ClosestAgents)[source(Coach)];
       +broadcastAcknowledgement[source(Coach)].

// We have received a zone better than the one we know, so we throw our zone
// away. Also, we acknowledge this message and check whether we now got all
// replies.
@onlyAddOneBetterZoneAtATime[atomic]
+foreignBestZone(_, Value, CentreNode, ClosestAgents)[source(Coach)]:
    isInZoningRound
    & position(PositionVertex)
    & bestZone(FormerValue, FormerCentreNode, BestZoneClosestAgents)[source(FormerCoach)]
    // my zone is worse:
    & FormerValue < Value
    // or the zones are identical but my name is alphabetically bigger:
    | (FormerValue == Value
        & .my_name(MyName)
        & .sort([Coach, MyName], [Coach, MyName])
    )
    <- .send(Coach, tell, broadcastAcknowledgement);
       -bestZone(FormerValue, FormerCentreNode, BestZoneClosestAgents)[source(FormerCoach)];
       +bestZone(Value, CentreNode, ClosestAgents)[source(Coach)];
       +broadcastAcknowledgement[source(Coach)].

// We were informed about a worse zone. We inform the sender about our better
// zone and wait for his acknowledgement.
+foreignBestZone(_, _, _, _)[source(Sender)]:
    isInZoningRound
    & bestZone(ZoneValue, CentreNode, ClosestAgents)[source(self)]
    <- ia.generateId(Id);
       .send(Sender, tell, foreignBestZone(Id, ZoneValue, CentreNode, ClosestAgents)).

// We were informed about a worse zone. We can check whether we have received
// all replies. We don't have to wait for an acknowledgement as our bestZone
// isn't from us (!= self).
+foreignBestZone(_, _, _, _)[source(Sender)]:
    isInZoningRound
    <- .send(Sender, tell, broadcastAcknowledgement);
       +broadcastAcknowledgement[source(Sender)].

// It doesn't matter if we aren't interested in zoning. We have to reply in any
// case.
+foreignBestZone(_, _, _, _)[source(Sender)]
    <- .send(Sender, tell, broadcastAcknowledgement).

// If all agents have replied with either a broadcast or a refusal, we're done
// waiting.
@testForAllReplies[priority(1), atomic]
+broadcastAcknowledgement[source(Sender)]:
    isInZoningRound
    & .count(broadcastAcknowledgement[source(_)], RepliesAmount)
    & broadcastAgentList(BroadcastList)
    & .length(BroadcastList, AgentsAmount)
    & AgentsAmount ==  RepliesAmount
    <- !choseZoningRole.

// We aren't zoning or haven't received all replies, so we ignore this.
+broadcastAcknowledgement[source(_)].