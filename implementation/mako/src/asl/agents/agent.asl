{ include("../misc/storeBeliefs.asl") }
{ include("../actions/explore.asl") }
{ include("../actions/goto.asl") }
{ include("../actions/doSurveying.asl") }
{ include("../actions/avoidEnemy.asl") }
{ include("../actions/getRepaired.asl") }
// zoning might be split down onto concrete agents e.g. because the explorer
// should prefer probing instead of zoning:
// { include("../actions/zoning.asl") }
// { include("../actions/zoning.minion.asl") }
// { include("../actions/zoning.coach.asl") }
{ include("../misc/initialization.asl") }

zoneMode(false).

// Try to do an action in every step.
+requestAction:
    position(Position)
	& lastActionResult(Result)
	& lastAction(Action)
	& step(Step)
	& energy(Energy)
    <-
//	We have to abolish here because we need to make sure that requestAction
//	gets processed in every step.
    .abolish(requestAction);
//  The ignoreEnemy belief is used by agents to ignore "harmless" agents (all
//	enemy agents that aren't saboteurs) and must be abolished every step as well.
    .abolish(ignoreEnemy);
	.print("[Step ", Step, "] My position is ", Position, ". My last action was '", Action,"'. Result was ", Result,". My energy is ", Energy ,".");
    !doAction.

// If agent is disabled - get repaired.
 +!doAction:
 	 health(Health)
 	 & Health == 0
    <-
    !getRepaired.
    
// If I have closestRepairer belief - I've just been repaired. Need to update the repairer on that.
 +!doAction:
    closestRepairer(Repairer)
    <-
    .abolish(closestRepairer(_));
    .send(Repairer, tell, successfullyRepaired);
    !!doAction.

// If a friendly disabled agent is within half of the visibility range of a repairer,
// repair it.
+!doAction:
	role(repairer)
	& position(MyPosition)
	& myTeam(MyTeam)
	& visibleEntity(Vehicle, VehiclePosition, MyTeam, disabled)
	& ia.getDistance(MyPosition, VehiclePosition, Distance)
	& visRange(MyRange)
	& Distance <= (MyRange / 2)
	<-
	.print("I see the disabled agent ", Vehicle, " on ", VehiclePosition, " - will try to repair it.");
	!doRepair(Vehicle, VehiclePosition).
    
//Fallback action in the case where we didn't pay attention and tried to perform
//an action without having the energy for it.
+!doAction:
	lastActionResult(failed_resources)
	<-
	.print("Warning! I tried to perform an action without having enough energy to do so. Will recharge.");
	recharge.

//Test plan for buying: What happens if saboteurs extend their visiblity range?
+!doAction:
	role(saboteur)
	& money(Money)
	& Money > 11
	& visRange(VisRange)
	& VisRange < 2
	& energy(MyEnergy)
	& MyEnergy > 2
	& health(MyHealth)
	& MyHealth > 0
	<-
	.print("Current money is ", Money, ". Current visRange is ", VisRange, ". Will buy sensor upgrade.");
	buy(sensor).
	
// If an agent sees an enemy on its position, it has to deal with the enemy.

// If an inspector sees an enemy that currently doesn't count as inspected, inspect it.
// We keep track of the inspected state for enemy agents in the MapAgent.
+!doAction:
	role(inspector)
	& visibleEntity(Vehicle, VehiclePosition, Team, _)
	& myTeam(MyTeam)
	& MyTeam \== Team
	& ia.isNotInspected(Vehicle)
	& position(MyPosition)
	& ia.getDistance(MyPosition, VehiclePosition, Distance)
	& visRange(MyRange)
	& Distance <= (MyRange / 2)
	<-
	.print("Inspecting ", Vehicle, " at ", VehiclePosition);
	!doInspecting(Vehicle, VehiclePosition).

// Saboteur In defending zone mode 
+!doAction:
	role(saboteur)
	& strategy(zoneDefence)
 	<- .print("I'm in zone defending mode.");
 	   !defendZone.


// In the case where we have sent a saboteur (or any other agent) to an enemy 'ghost' location (a location
// where an enemy agent used to be, but no longer occupies), we need to tell the
// MapAgent to update its list of enemy positions.
+!doAction:
	position(MyPosition)
	& ia.getClosestEnemy(MyPosition, EnemyPosition, Enemy)
	& not visibleEntity(Enemy, EnemyPosition, _, _)
	& ia.getDistance(MyPosition, EnemyPosition, Distance)
	& visRange(MyRange)
	& Distance <= MyRange
	<-
	.print("Expected ", Enemy, " at ", EnemyPosition, ", but I don't see him from ", MyPosition, ". Informing MapAgent.");
	ia.removeEnemyGhost(Enemy);
	!doAction.
	
// Saboteurs attack active enemy agents when they see them.
+!doAction:
	role(saboteur)
 	& position(MyPosition)
	& ia.getClosestEnemy(MyPosition, EnemyPosition, Enemy)
	& ia.getDistance(MyPosition, EnemyPosition, Distance)
	& visRange(MyRange)
	& Distance <= MyRange
	& myTeam(MyTeam)
	& MyTeam \== Team
 	<- .print("Attacking ", Enemy, " on ", EnemyPosition, " from my position ", MyPosition);
 	   !doAttack(Enemy, EnemyPosition).
 	   
// Saboteurs should perform aggressively, preferring to attack enemy agents over exploring.
// In the case where they can't see an enemy agent themselves, they get help
// from the MapAgent.
+!doAction:
	role(saboteur)
	& position(MyPosition)
	& ia.getClosestEnemy(MyPosition, EnemyPosition, Enemy)
	& not ignoreEnemy(Vehicle)
	<-
	.print("Moving to attack ", Enemy, " on ", EnemyPosition, " from my position ", MyPosition);
	!doAttack(Enemy, EnemyPosition).

// If you're in range of what could be an active enemy saboteur, get out of there.
+!doAction:
	position(MyPosition)
	& visibleEntity(Vehicle, VehiclePosition, VehicleTeam, normal)
	& myTeam(MyTeam)
	& MyTeam \== VehicleTeam
	& not ignoreEnemy(Vehicle)
	& ia.couldBeSaboteur(Vehicle, VehicleVisRange)
	& ia.getDistance(MyPosition, VehiclePosition, Distance)
	& Distance <= VehicleVisRange
	<-
	.print("Danger! Active enemy saboteur", Vehicle, "on ", VehiclePosition, " is in attacking range!");
	!avoidEnemy.
	
// Print a warning if an active enemy is on our position.
// TODO: Call a saboteur to deal with the enemy.
+!doAction:
 	position(Position)
	& visibleEntity(Vehicle, Position, Team, normal)
	& myTeam(MyTeam)
	& MyTeam \== Team
	& not ignoreEnemy(Vehicle)
 	<-
	.print("Non-disabled, non-saboteur enemy ", Vehicle, " at my position!");
 	!dealWithEnemy(Vehicle).
 	
 // When saboteur, sentinel,and repairer are attacked,
 //and they are not disabled, they do parrying
 +!doAction:
 	 health(Health)
 	 & (role(sentinel) | role(saboteur) | role(repairer))
 	 & lastActionResult(failed_attacked)
 	 & Health \== 0
 	<- .print("I was attacked,my health is:",Health, " and I will do parry ");
 	   !doParry.

//If a sentinel stands on a zoneNode,
//and see normal enemy saboteur on its position or on its neighbourhood,do parry
+!doAction:
 	 role(sentinel)
 	& zoneMode(true)
 	& zoneNode(Position)
 	& position(MyPosition)
 	& Position == MyPosition
	& myTeam(MyTeam)
	& visibleEntity(Vehicle, EnemyPosition, Team, normal)
	& MyTeam \== Team
	& (visibleEdge(MyPosition, EnemyPosition) | visibleEdge(EnemyPosition, MyPosition) | EnemyPosition == MyPosition)
	& ia.isSaboteur(Vehicle)
 	<- .print(" I see the enemy Saboteur:", Vehicle, "at position:", EnemyPosition, "so I parry ");
 	   !doParry.

// If an explorer is on an unprobed vertex, probe it.
+!doAction:
	zoneMode(false)
	& position(Position)
	& ia.isNotProbed(Position)
	& role(explorer)
	<- .print(Position, " is not probed. I will probe.");
	 	!doProbing.

// If an agent is on an unsurveyed vertex, survey it
+!doAction:
	zoneMode(false)
	& position(Position)
	& ia.isNotSurveyed(Position)
	<-
	.print(Position, " is not surveyed. I will survey.");
	!doSurveying.

// If we're not in zone mode yet, explore.
+!doAction:
	zoneMode(false)
	<-
	.print("I will explore.");
	!doExploring.

// If we are finally standing on our zone vertex, we clear the command to get
// there and remember our node. Also, we look what else there is left for us to
// do.
+!doAction:
    zoneGoalVertex(GoalVertex)
    & position(PositionVertex)
    & GoalVertex == PositionVertex
    <- -zoneGoalVertex(GoalVertex)[source(_)];
       -+zoneNode(GoalVertex);
       .print("[zoning] I am now standing on my zone node.");
       !doAction.

// If we got a zoneGoalVertex, which is a node we should move to to build a
// zone, we will move there.
// This method is used by minions and coaches alike. Coaches will only have to
// to 0-1 steps to reach their goal though.
+!doAction:
    zoneGoalVertex(GoalVertex)
    <- .print("[zoning] I'm going to build a zone at ", GoalVertex);
       !goto(GoalVertex).

// If the agent has nothing to do, it should recharge instead of doing nothing.
+!doAction:
	energy(Energy)
	& maxEnergy(Max)
	& Energy < Max
	<-
	.print("I'm recharging because I don't know what else to do.");
	recharge.
	
// If we can't reach the goal vertex, we are not going to build this zone.
-!goto(_):
    zoneGoalVertex(GoalVertex)
    <- .print("[zoning] This should not be triggered because only agents get selected that could reach their zone node.");
       -zoneGoalVertex(GoalVertex)[source(_)];
       !cancelledZoneBuilding.
	
+!doAction:
	energy(Energy)
	& maxEnergy(Max)
	& Energy == Max
	& zoneMode(false)
	<- 
	-+zoneMode(true).

+!doAction
    <- .print("I have nothing to do. I'll skip."); 
    skip.