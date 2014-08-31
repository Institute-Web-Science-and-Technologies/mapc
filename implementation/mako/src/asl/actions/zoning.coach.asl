/* Initial beliefs and rules */
zoneBuildingMode(false).

/* Plans */

// Use an internal action that determines where to place the agents the best
// and tell them. The agents include all the coach's minions as well as himself.
//
//After that all remaining idle agents are told to start a new round of zoning.
+!assignededAgentsTheirPosition:
    bestZone(_, CentreNode, ClosestAgents)
    & broadcastAgentList(BroadcastList)
    <- ia.placeAgentsOnZone(CentreNode, ClosestAgents, PositionAgentMapping);
       .length(PositionAgentMapping, MappingLength);
       for (.range(ControlVariable, 0, MappingLength - 1)) {
           .nth(ControlVariable, PositionAgentMapping, [Agent, PositionVertex]);
           .send(Agent, tell, zoneGoalVertex(PositionVertex));
       }
       .difference(BroadcastList, ClosestAgents, NonZoners);
       .send(NonZoners, achieve, preparedNewZoningRound).

+!assignededAgentsTheirPosition
    <- .print("[zoning] Assigning agents a position failed.").

// Negative zone replies have no meaning for coaches. Hence they are ignored.
+negativeZoneReply[source(_)]:
    isCoach(true)
    <- true.

// If s.o. or ourselves cancelled the zone, we have to inform all our minions
// about it and go back to start zoning from scratch.
+!cancelledZoneBuilding[source(Sender)]:
    isCoach(true)
    & .my_name(Coach)
    & bestZone(CentreVertex, _, ClosestAgents)
    & .length(ClosestAgents, ZoneSize)
    <- ia.destroyZone(CentreVertex, ZoneSize);
       .difference(ClosestAgents, [Coach, Sender], UnawareMinions);
       .send(UnawareMinions, achieve, cancelledZoneBuilding);
       -+isCoach(false);
       !preparedNewZoningRound.

// This is a part @manuelmittler started to tackle extension of zones. It is not
// finished yet.
+!asyncForeignBestZone(Value, CentreNode, ClosestAgents)[source(Sender)]:
	isCoach(true)
	& bestZone(BestZoneValue, BestZoneCentreNode, BestZoneClosestAgents)
	& position(PositionVertex)
	& .length(ClosestAgents, ZoneSize)
	& .length(BestZoneClosestAgents, BestZoneSize)
	& Value > BestZoneValue
	& ZoneSize <= BestZoneSize+1
	<- //If the zone of the idleZoner is better regarding the PrognosedValue and the number of agents that would be needed
       //build that zone is less or equal than the number of agents in the current zone + 1 (because we have the agents
       //from the old zone and the newly available idleZoner), then we want to move the agents to the new zone.
       -+bestZone(Value, CentreNode); // TODO: nb that this does not work if [source(Sender \== self)].
       //+positiveZoneReply(CentreNode)[source(Sender)];
       .send(Sender, tell, isMinion(true));
       .send(Sender, untell, isCoach(true));
       !movedToNewZone(CentreNode, UsedNodes).
    	
+!asyncForeignBestZone(Value, CentreNode, ClosestAgents)[source(Sender)]:
    isCoach(true)
    & bestZone(_, BestZoneCentreNode, BestZoneClosestAgents)
    & .length(BestZoneClosestAgents, BestZoneSize)
    <- //If the zone of the idleZoner is not better than the current zone, the idleZoner will join the group
       //and extend the zone by moving to an optional vertex.
       //TODO: pass correct parameters/return values: make sure to use the right method signature
       ia.getExtraZoneSpot(BestZoneCenterNode, BestZoneSize+1, OptionalVertex);
       .send(Sender, tell, zoneGoalVertex(OptionalVertex)).

+!movedToNewZone(CentreNode, UsedNodes)
	<-!goto(CentreNode);
	  !assignededAgentsTheirPosition.

// If the enemy is inside the zone - call saboteur to help if the request was not send earlier.
+!checkZoneUnderAttack(CentreNode):
    isCoach(true)
    & ia.getClosestEnemy(CentreNode, EnemyPosition, _)
    & ia.getDistance(CentreNode, EnemyPosition, Distance)
    & (Distance <= 2) & (Distance >= 0)
    & not zoneProtectRequestSend(CentreNode)
    & saboteurList(SaboteurList)
    <-
	.send(SaboteurList, tell, requestZoneDefence(ZoneCentre));
    +zoneProtectRequestSend(CentreNode). 

// If the enemy left the zone, but we called the saboteur to help - cancel help request.     
+!checkZoneUnderAttack(CentreNode):
    isCoach(true)
    & ia.getClosestEnemy(CentreNode, EnemyPosition, _)
    & ia.getDistance(CentreNode, EnemyPosition, Distance)
    & (Distance > 2)
    & zoneProtectRequestSend(CentreNode)
    & saboteurList(SaboteurList)
    <-
	.send(SaboteurList, tell, cancelZoneDefence(ZoneCentre));
    .abolish(zoneProtectRequestSend(CentreNode)).

// Fallback plan
+!checkZoneUnderAttack(CentreNode).
    