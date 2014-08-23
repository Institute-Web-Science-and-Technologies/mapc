/* Initial beliefs and rules */
zoneBuildingMode(false).

/* Plans */
@becomeACoach
+!choseZoningRole:
    bestZone(_, _, _, _)[source(self)]
    & broadcastAgentList(BroadcastList)
    & .my_name(MyName)
    <- .send(BroadcastList, untell, idleZoner(MyName));
       -+isCoach(true);
       !isZonePossible.

// Test whether this zone could actually be built, regarding available zoners
// and minions. If not, cancel this zone building process.
+!isZonePossible:
    bestZone(_, _, _, UsedNodes)
    & .count(idleZoner(_), IdleZonersAmount)
    & .count(positiveZoneReply(_), AvailableMinionsAmount)
    & .length(UsedNodes) -1 >  (IdleZonersAmount + AvailableMinionsAmount)
    <- !cancelledZoneBuilding.

+!isZonePossible
    <- true.
    
//positiveZoneReply from a newly available minion
+positiveZoneReply(CentreNode):
	newMinion(NewMinion)
	<-true.
// If we are already building a zone (which means directing minions, moving to
// the centre mode and so on), we deny any further minions.
//
// TODO: We could have them extend our zone!
+positiveZoneReply(_)[source(Minion)]:
    isCoach(true)
    & zoneBuildingMode(true)
    <- .send(Sender, achieve, foundNewZone);
       -positiveZoneReply(_)[source(Minion)].

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
    & bestZone(_, _, CentreNode, UsedNodes)
    & .count(positiveZoneReply(_), MinionsAmount)
    & (.length(UsedNodes) -1 > MinionsAmount)
    <- true.

// If s.o. agreed to build our zone with us and we have sufficiently many
// minions, we start building it. First, we switch into zoneBuildingMode and
// start moving towards the CentreNode. After that, we direct Minions where to
// go.
// We can directly go to the CentreNode as it is somewhere in our 1HNH.
// We also set the zoneNode belief to express that we have reached our zoneNode
// and to express which one it is – maybe needed if we happen to move away from
// it.
+positiveZoneReply(CentreNode)[source(Minion)]:
    isCoach(true)
    & bestZone(_, _, CentreNode, _)
    & position(MyPosition)
    <- -+zoneBuildingMode(true);
       if(MyPosition \== CentreNode){
       	 goto(CentreNode);
       }
       -+zoneNode(CentreNode);
       !toldMinionsTheirPosition.

// If s.o. wants to build a zone with us that we don't want to build (anymore),
// we tell him to look for a new zone.
+positiveZoneReply(_)[source(Minion)]:
    isCoach(true)
    <- .send(Sender, achieve, foundNewZone)
       -positiveZoneReply(_)[source(Minion)].

// Use an internal action that determines where to place the minions the best
// and tell them:
+!toldMinionsTheirPosition:
    .findall(Minion, positiveZoneReply(CentreNode)[source(Minion)], Minions)
    & bestZone(_, _, CentreNode, _)
    & ia.placeAgentsOnZone(CentreNode, Minions, PositionMinionMapping)
    & .length(PositionMinionMapping, MappingLength)
    <- for (.range(ControlVariable, 0, MappingLength - 1)) {
           .nth(ControlVariable, PositionMinionMapping, [PositionVertex, Minion]);
           .send(Minion, tell, zoneGoalVertex(PositionVertex));
       }
       if(.length(Minions) > MappingLength){
       	.range(ControlVariable, MappingLength - 1, .length(Minions));
       	.nth(ControlVariable, PositionMinionMapping, [PositionVertex, Minion]);
       	.send(Minion, achieve, foundNewZone);
       }.

// If we get a negative reply about a zone we don't want to build (anymore), we
// remove the percept so that we don't count it as a refusal.
+negativeZoneReply(CentreNode)[source(Sender)]:
    isCoach(true)
    & not bestZone(_, _, CentreNode, _)
    <- -negativeZoneReply(CentreNode)[source(Sender)].

// If we get a negative zone reply and there aren't enough possible agents to
// build this zone, we cancel this zone building.
//
// TODO: remove negativeZoneReplies from our BB?!
+negativeZoneReply(CentreNode)[source(_)]:
    isCoach(true)
    & .count(negativeZoneReply(_), RefusalAmount)
    & .count(broadcastAgentList(BroadcastList), AgentsAmount) // or use 28 as a static measure :Þ
    & bestZone(_, _, CentreNode, UsedNodes)
    & .length(UsedNodes) >  AgentsAmount - RefusalAmount
    <- !cancelledZoneBuilding.

// The sender won't be a part of our zone but there are still enough possible
// minions. So we continue to wait for more replies.
+negativeZoneReply(_)[source(_)]:
    isCoach(true)
    <- true.

// If s.o. or ourselves cancelled the zone, we have to inform all our minions
// about it and go back to start zoning from scratch.
+!cancelledZoneBuilding[source(Sender)]:
    isCoach(true)
    & .findall(Minion, positiveZoneReply(_)[source(Minion)], Minions)
    <- .difference(Minions, Sender, UnawareMinions);
       .send(UnawareMinions, achieve, cancelledZoneBuilding);
       -+isCoach(false);
       !builtZone(true).
       
+!asyncForeignBestZone(Value, CentreNode, UsedNodes)[source(Sender)]:
	isCoach(true)
	& bestZone(BestZoneValue, BestZonePrognosedValue, BestZoneCentreNode, BestZoneUsedNodes)
	& position(PositionVertex)
	<- ?plannedZoneTimeInSteps(Steps);
	   ia.getDistance(PositionVertex, CentreNode, Distance);
       ia.calculateLongTermZoneValue(Value, Distance, Steps, PrognosedValue);
       .length(BestZoneUsedNodes, CurrentSize);
       //If the zone of the idleZoner is better regarding the PrognosedValue and the number of agents that would be needed
       //build that zone is less or equal than the number of agents in the current zone + 1 (because we have the agents
       //from the old zone and the newly available idleZoner), then we want to move the agents to the new zone.
       if (PrognosedValue > BestZonePrognosedValue & UsedNodes <= BestZoneUsedNodes+1){
       	-+bestZone(Value, PrognosedValue, CentreNode, UsedNodes);
       	+newMinion(Sender);
       	+positiveZoneReply(CentreNode)[source(Sender)];
       	.send(Sender, tell, isMinion(true));
       	.send(Sender, untell, isCoach(true));
       	!movedToNewZone(CentreNode, UsedNodes);
       }
       else{
       	//If the zone of the idleZoner is not better than the current zone, the idleZoner will join the group
       	//and extend the zone by moving to an optional vertex.
       	//TODO: pass correct parameters/return values: make sure to use the right method signature
       	ia.getExtraZoneSpot(BestZoneCenterNode, CurrentSize, OptionalVertex);
       	.send(Sender, tell, zoneGoalVertex(OptionalVertex));
       }.
       
+!movedToNewZone(CentreNode, UsedNodes)
	<-!goto(CentreNode);
	  !toldMinionsTheirPosition;
	  -newMinion(_).