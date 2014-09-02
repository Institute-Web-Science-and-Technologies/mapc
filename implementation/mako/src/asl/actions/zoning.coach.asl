/* Initial beliefs and rules */

/* Plans */

// Use an internal action that determines where to place the agents the best
// and tell them. The agents include all the coach's minions as well as himself.
//
//After that all remaining idle agents are told to start a new round of zoning.
+!assignededAgentsTheirPosition:
    isCoach(true)
    & bestZone(_, CentreNode, ClosestAgents)
    & broadcastAgentList(BroadcastList)
    <- ia.placeAgentsOnZone(CentreNode, ClosestAgents, AgentPositionMapping);
    
       .difference(BroadcastList, ClosestAgents, NonZoners);
       .send(NonZoners, achieve, preparedNewZoningRound);
       
       .length(AgentPositionMapping, MappingLength);
       .print("[zoning] I'm going to place the agents as follows: ", AgentPositionMapping, " with the CentreNode being ", CentreNode);
       for (.range(ControlVariable, 0, MappingLength - 1)) {
           .nth(ControlVariable, AgentPositionMapping, [Agent, PositionVertex]);
           .send(Agent, tell, zoneGoalVertex(PositionVertex));
       };
       -+isLocked(false).

// The achievement goal failed for some reason. Tell all agents to restart
// zoning.
+!assignededAgentsTheirPosition:
    .my_name(Coach)
    <- ?broadcastAgentList(BroadcastList);
       .send(BroadcastList, achieve, preparedNewZoningRound);
       
       ?bestZone(_, _, ClosestAgents);
       .difference(ClosestAgents, [Coach], Minions);
       .send(Minions, achieve, cancelledZoneBuilding);
       
       -+isLocked(false);
       !!preparedNewZoningRound;
       .print("[zoning] Assigning agents a position failed.").

// If s.o. or ourselves cancelled the zone, we have to inform all our minions
// about it and go back to start zoning from scratch.
// The lock should make sure that this goal is not processed before
// !assignededAgentsTheirPosition was processed.
// Also resets the currentRange.
+!cancelledZoneBuilding[source(Sender)]:
    isCoach(true)
    & isLocked(false)
    & .my_name(Coach)
    & bestZone(_, CentreNode, ClosestAgents)
    & .length(ClosestAgents, ZoneSize)
    <- ia.destroyZone(CentreNode, ZoneSize);
       .difference(ClosestAgents, [Coach, Sender], UnawareMinions);
       .send(UnawareMinions, achieve, cancelledZoneBuilding);
       .print("[zoning] I am destroying this zone and informing ", UnawareMinions);
       -+isCoach(false);
       
       ?defaultRangeForSingleZones(Range);
       -+currentRange(Range);
       -zoneGoalVertex(_)[source(_)]; // self should be the only source
       
       // Cancel ordered saboteurs if any. Can't be "!!" because it needs
       // bestZone(_,CentreNode,_):
       !informedSaboteursAboutZoneBreakup;
       
       !preparedNewZoningRound.

// Coaches choose to ignore the periodic zone breakup calls when they haven't
// even started building this zone.
+!cancelledZoneBuilding[source(Sender)]:
    isCoach(true)
    & isLocked(true)
    <- .print("[zoning] The periodic zone breakup interfered with me just having started building a zone. Ignoring it.").

// TODO: This goal should be removable once the iAs work properly.
+!cancelledZoneBuilding[source(_)]:
    isCoach(true)
    & bestZone(_, CentreNode, ClosestAgents)
    <- !informedSaboteursAboutZoneBreakup;
       .print("[zoning] Zone destruction failed. I have no idea how to react on that. Doing nothing.").

// TODO: This goal should be removable once the iAs work properly.
// I think this happens due to destroyZone failing because it only happens after it has been called once.
+!cancelledZoneBuilding[source(_)]:
    isCoach(true)
    <- !informedSaboteursAboutZoneBreakup;
       .print("[zoning] I forgot about my bestZone belief. I have no idea how this can happen. Doing nothing.").

// If the enemy is inside the zone - call saboteur to help if the request was not send earlier.
+!checkZoneUnderAttack:
    isCoach(true)
    & bestZone(_, CentreNode, _)
    & ia.getClosestEnemy(CentreNode, EnemyPosition, _)
    & ia.getDistance(CentreNode, EnemyPosition, Distance)
    & (Distance <= 3) & (Distance >= 0)
    & not zoneProtectRequestSent
    & saboteurList(SaboteurList)
    <-
	.send(SaboteurList, tell, requestZoneDefence(CentreNode));
    +zoneProtectRequestSent. 

// If the enemy left the zone, but we called the saboteur to help - cancel help request.     
+!checkZoneUnderAttack:
    isCoach(true)
    & bestZone(_, CentreNode, _)
    & ia.getClosestEnemy(CentreNode, EnemyPosition, _)
    & ia.getDistance(CentreNode, EnemyPosition, Distance)
    & (Distance > 3)
    & zoneProtectRequestSent
    & saboteurList(SaboteurList)
    <-
	.send(SaboteurList, tell, cancelZoneDefence(CentreNode));
    .abolish(zoneProtectRequestSent).

// Fallback plan
+!checkZoneUnderAttack.

// If the coach ordered saboteurs, he must cancel it because there isn't any
// zone to defent anymore.
+!informedSaboteursAboutZoneBreakup:
    zoneProtectRequestSent
    & bestZone(_, CentreNode, _)
    & saboteurList(SaboteurList)
    <- .send(SaboteurList, tell, cancelZoneDefence(CentreNode));
       .abolish(zoneProtectRequestSent).

// If no saboteurs are on the way, do nothing.
+!informedSaboteursAboutZoneBreakup.
    