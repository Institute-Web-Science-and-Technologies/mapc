/* Initial beliefs and rules */
zoneBuildingMode(false).

/* Plans */
@becomeACoach
+!choseZoningRole:
    bestZone(_, _, _)[source(self)]
    & broadcastAgentList(BroadcastList)
    & .my_name(MyName)
    <- .send(BroadcastList, untell, idleZoner(MyName));
       -+isCoach(true);
       !isZonePossible.

// Test whether this zone could actually be built, regarding available zoners
// and minions. If not, cancel this zone building process.
+!isZonePossible:
    bestZone(_, _, UsedNodes)
    & .count(idleZoner(_), IdleZonersAmount)
    & .count(availableMinions(_), AvailableMinionsAmount)
    & .length(UsedNodes) -1 >  (IdleZonersAmount + AvailableMinionsAmount)
    <- !cancelledZoneBuilding.

// If we are already building a zone (which means directing minions, moving to
// the centre mode and so on), we deny any further minions.
//
// TODO: We could have them extend our zone!
+positiveZoneReply(_)[source(Minion)]:
    isCoach(true)
    & zoneBuildingMode(true)
    <- .send(Sender, achieve, foundNewZone).

// If s.o. agreed to build a zone with us, we memorise his availability if he is
// replying to the zone which we still try to build right now. Also, we do
// nothing, as we have not acquired sufficiently many minions to build our zone.
//
// NOTE: The CentreNode should be sufficient as an identifier. It might even be
// left out. The worst thing that could happen then is that Distances would be
// wrongly associated with a new zone and a minion would walk further than
// planned.
+positiveZoneReply(CentreNode)[source(Minion)]:
    isCoach(true)
    & bestZone(_, CentreNode, UsedNodes)
    & .count(positiveZoneReply(_), MinionsAmount)
    & (.length(UsedNodes) -1 > MinionsAmount)
    <- true.

// If s.o. agreed to build our zone with us and we have sufficiently many
// minions, we start building it. First, we switch into zoneBuildingMode and
// start moving towards the CentreNode. After that, we direct Minions where to
// go.
// We can directly go to the CentreNode as it is somewhere in our 1HNH.
+positiveZoneReply(CentreNode)[source(Minion)]:
    isCoach(true)
    & bestZone(_, CentreNode, _)
    <- -+zoneBuildingMode(true);
       goto(CentreNode); // may fail if we are standing on it already.
       !toldMinionsTheirPosition.

// If s.o. wants to build a zone with us that we don't want to build (anymore),
// we tell him to look for a new zone.
+positiveZoneReply(_)[source(Minion)]:
    isCoach(true)
    <- .send(Sender, achieve, foundNewZone).

// Use an internal action that determines where to place the minions the best
// and tell them:
+!toldMinionsTheirPosition:
    .findall(Minion, positiveZoneReply(CentreNode)[source(Minion)], Minions)
    & bestZone(_, CentreNode, _)
    & ia.placeAgentsOnZone(CentreNode, Minions, PositionMinionMapping)
    & .length(PositionMinionMapping, MappingLength)
    <- for (.range(ControlVariable, 0, MappingLength - 1)) {
           .nth(ControlVariable, PositionMinionMapping, [PositionVertex, Minion]);
           .send(Minion, tell, zoneNode(PositionVertex));
       }.

// If we get a negative reply about a zone we don't want to build (anymore), we
// remove the percept so that we don't count it as a refusal.
+negativeZoneReply(CentreNode)[source(Sender)]:
    not bestZone(_, CentreNode, _)
    & isCoach(true)
    <- -negativeZoneReply(CentreNode)[source(Sender)].

// If we get a negative zone reply and there aren't enough possible agents to
// build this zone, we cancel this zone building.
//
// TODO: remove negativeZoneReplies from our BB?!
+negativeZoneReply(CentreNode)[source(_)]:
    isCoach(true)
    & .count(negativeZoneReply(_), RefusalAmount)
    & .count(broadcastAgentList(BroadcastList), AgentsAmount) // or use 28 as a static measure :Þ
    & bestZone(_, CentreNode, UsedNodes)
    & .length(UsedNodes) >  AgentsAmount - RefusalAmount
    <- !cancelledZoneBuilding.

// The sender won't be a part of our zone but there are still enough possible
// minions. So we continue to wait for more replies.
+negativeZoneReply(_)[source(_)]
    <- true.

// If s.o. or ourselves cancelled the zone, we have to inform all our minions
// about it and go back to start zoning from scratch.
+!cancelledZoneBuilding[source(Sender)]:
    isCoach(true)
    & .findall(Minion, positiveZoneReply(_)[source(Minion)], Minions)
    <- .difference(Minions, Sender, UnawareMinions);
       .send(UnawareMinions, achieve, cancelledZoneBuilding);
       -+isCoach(false);
       !builtZone.