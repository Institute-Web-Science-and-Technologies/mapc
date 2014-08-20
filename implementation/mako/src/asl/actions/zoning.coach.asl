/* Initial beliefs and rules */
zoneBuildingMode(false).

/* Plans */
@becomeACoach
+!choseZoningRole:
    bestZone(_, _, _)[source(self)]
    <- !isZonePossible.

// Test whether this zone could actually be built, regarding available zoners
// and minions. If not, cancel this zone building process.
+!isZonePossible:
    bestZone(_, _, NeededAgents)
    & .count(idleZoner(_), IdleZonersAmount)
    & .count(availableMinions(_), AvailableMinionsAmount)
    & .length(NeededAgents) -1 >  (IdleZonersAmount + AvailableMinionsAmount)
    <- !cancelledZoneBuilding.

// If we are already building a zone (which means directing minions, moving to
// the centre mode and so on), we deny any further minions.
//
// TODO: We could have them extend our zone!
+positiveZoneReply(_)[source(Minion)]:
    zoneBuildingMode(true)
    <- .send(Sender, achieve, foundNewZone).

// If s.o. agreed to build a zone with us, we memorise his availability if he is
// replying to the zone which we still try to build right now. Also, we do
// nothing, as we have not acquired sufficiently many minions to build our zone.
//
// NOTE: The CentreNode should be sufficient as an identifier. It might even be
// left out. The worst thing that could happen then is that Distances would be
// wrongly associated with a new zone and a minion would walk further than
// planned.
// TODO: is positiveZoneReply added instantly or after triggering and evaluating this code?
+positiveZoneReply(CentreNode)[source(Minion)]:
    bestZone(_, CentreNode, NeededAgents)
    & .count(positiveZoneReply(_), MinionsAmount)
    & (.length(NeededAgents) -1 > MinionsAmount)
    <- true.

// If s.o. agreed to build our zone with us and we have sufficiently many
// minions, we start building it. First, we switch into zoneBuildMode and
// start moving towards the CentreNode. After that, we direct Minions where to
// go.
// We can directly go to the CentreNode as it is somewhere in our 1HNH.
+positiveZoneReply(CentreNode)[source(Minion)]:
    bestZone(_, CentreNode, _)
    <- -+zoneBuildMode(true);
       goto(CentreNode); // may fail if we are standing on it already.
       !toldMinionsTheirPosition.

// If s.o. wants to build a zone with us that we don't want to build (anymore),
// we tell him to look for a new zone.
+positiveZoneReply(_)[source(Minion)]
    <- .send(Sender, achieve, foundNewZone).

// Use an internal action that determines where to place the minions the best
// and tell them:
+!toldMinionsTheirPosition:
    .findall(Minion, positiveZoneReply(_)[source(Minion)], Minions)
    & .bestZone(_, _, UsedNodes)
    & .getPositionsForMinionsPLACEHOLDERiA(UsedNodes, Minions, PositionMinionMapping)
    & .length(PositionMinionMapping, MappingLength)
    <- for (.range(ControlVariable, 0, MappingLength - 1)) {
           .nth(ControlVariable, PositionMinionMapping, [Position, Minion]);
           .send(Minion, tell, zoneNode(Position));
       }.

// If we get a negative zone reply, we remove the sender from
// our minions list – if he was on it.
+negativeZoneReply(CentreNode)[source(PossibleMinion)]:
    bestZone(BestZoneValue, BestZoneCentreNode, BestZoneUsedNodes)
    // TODO: is the CentreNode as an identifier sufficient?:
    & BestZoneCentreNode == CentreNode
    <- -negativeZoneReply(CentreNode)[source(PossibleMinion)];
       -availableMinion(PossibleMinion).



//TODO: test if zoning is still possible:
//+negativeZoneReply //TODO: test if we got too many negative replies already