/* Initial beliefs and rules */
zoneBuildingMode(false).

/* Plans */

// Use an internal action that determines where to place the minions the best
// and tell them:
+!toldMinionsTheirPosition:
    bestZone(_, CentreNode, Minions)
    & ia.placeAgentsOnZone(CentreNode, Minions, PositionMinionMapping)
    & .length(PositionMinionMapping, MappingLength)
    <- for (.range(ControlVariable, 0, MappingLength - 1)) {
           .nth(ControlVariable, PositionMinionMapping, [PositionVertex, Minion]);
           .send(Minion, tell, zoneGoalVertex(PositionVertex));
       }.

// Negative zone replies have no meaning for coaches. Hence they are ignored.
+negativeZoneReply(_)[source(_)]:
    isCoach(true)
    <- true.

// If s.o. or ourselves cancelled the zone, we have to inform all our minions
// about it and go back to start zoning from scratch.
+!cancelledZoneBuilding[source(Sender)]:
    isCoach(true)
    & bestZone(_, _, Minions)
    <- .difference(Minions, Sender, UnawareMinions);
       .send(UnawareMinions, achieve, cancelledZoneBuilding);
       -+isCoach(false);
       !builtZone(true).

// This is a part @manuelmittler started to tackle extension of zones. It is not
// finished yet.
+!asyncForeignBestZone(Value, CentreNode, Minions)[source(Sender)]:
	isCoach(true)
	& bestZone(BestZoneValue, BestZoneCentreNode, BestZoneMinions)
	& position(PositionVertex)
	& .length(Minions, ZoneSize)
	& .length(BestZoneMinions, BestZoneSize)
	& Value > BestZoneValue
	& ZoneSize <= BestZoneSize+1
	<- //?plannedZoneTimeInSteps(Steps);
	   //ia.getDistance(PositionVertex, CentreNode, Distance);
       //ia.calculateLongTermZoneValue(Value, Distance, Steps, PrognosedValue);
       //.length(BestZoneUsedNodes, CurrentSize);
       //If the zone of the idleZoner is better regarding the PrognosedValue and the number of agents that would be needed
       //build that zone is less or equal than the number of agents in the current zone + 1 (because we have the agents
       //from the old zone and the newly available idleZoner), then we want to move the agents to the new zone.
       -+bestZone(Value, CentreNode); // TODO: nb that this does not work if [source(Sender \== self)].
       //+positiveZoneReply(CentreNode)[source(Sender)];
       .send(Sender, tell, isMinion(true));
       .send(Sender, untell, isCoach(true));
       !movedToNewZone(CentreNode, UsedNodes).
    	
+!asyncForeignBestZone(Value, CentreNode, Minions)[source(Sender)]:
    isCoach(true)
    & bestZone(_, BestZoneCentreNode, BestZoneMinions)
    & .length(BestZoneMinions, BestZoneSize)
    <- //If the zone of the idleZoner is not better than the current zone, the idleZoner will join the group
       //and extend the zone by moving to an optional vertex.
       //TODO: pass correct parameters/return values: make sure to use the right method signature
       ia.getExtraZoneSpot(BestZoneCenterNode, BestZoneSize+1, OptionalVertex);
       .send(Sender, tell, zoneGoalVertex(OptionalVertex)).

+!movedToNewZone(CentreNode, UsedNodes)
	<-!goto(CentreNode);
	  !toldMinionsTheirPosition.