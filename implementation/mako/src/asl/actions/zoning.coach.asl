/* Initial beliefs and rules */

/* Plans */

// Use an internal action that determines where to place the agents the best
// and tell them. The agents include all the coach's minions as well as himself.
//
//After that all remaining idle agents are told to start a new round of zoning.
+!assignededAgentsTheirPosition:
    bestZone(_, CentreNode, ClosestAgents)
    & broadcastAgentList(BroadcastList)
    <- ia.placeAgentsOnZone(CentreNode, ClosestAgents, AgentPositionMapping);
    
       .difference(BroadcastList, ClosestAgents, NonZoners);
       .send(NonZoners, achieve, preparedNewZoningRound);
       
       .length(AgentPositionMapping, MappingLength);
       .print("[zoning] I'm going to place the agents as follows: ", AgentPositionMapping, " with the CentreNode being ", CentreNode);
       for (.range(ControlVariable, 0, MappingLength - 1)) {
           .nth(ControlVariable, AgentPositionMapping, [Agent, PositionVertex]);
           .send(Agent, tell, zoneGoalVertex(PositionVertex));
       }.

// The achievement goal failed for some reason. Tell all agents to restart
// zoning.
+!assignededAgentsTheirPosition
    <- ?broadcastAgentList(BroadcastList);
       .send(BroadcastList, achieve, preparedNewZoningRound);
       !preparedNewZoningRound;
       .print("[zoning] Assigning agents a position failed.").

// If s.o. or ourselves cancelled the zone, we have to inform all our minions
// about it and go back to start zoning from scratch.
+!cancelledZoneBuilding[source(Sender)]:
    isCoach(true)
    & .my_name(Coach)
    & bestZone(_, CentreNode, ClosestAgents)
    & .length(ClosestAgents, ZoneSize)
    <- ia.destroyZone(CentreNode, ZoneSize);
       .difference(ClosestAgents, [Coach, Sender], UnawareMinions);print(">>>>6!");
       .send(UnawareMinions, achieve, cancelledZoneBuilding);
       .print("[zoning] I am destroying this zone and informing ", UnawareMinions);
       -+isCoach(false);
       !preparedNewZoningRound.

+!cancelledZoneBuilding[source(_)]:
    isCoach(true)
    & bestZone(_, CentreNode, ClosestAgents)
    <- .print("[zoning] Zone destruction failed. I have no idea how to react on that. Doing nothing.").
    
+!cancelledZoneBuilding[source(_)]:
    isCoach(true)
    <- .print("[zoning] I forgot about my bestZone belief. I have no idea how this can happen. Doing nothing.").

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
    