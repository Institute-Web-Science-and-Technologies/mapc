// Agent baseAgent in project mako
{ include("../misc/storeBeliefs.asl") }
{ include("../actions/explore.asl") }
{ include("../actions/goto.asl") }
{ include("../actions/doSurveying.asl") }
{ include("../actions/avoidEnemy.asl") }
{ include("../misc/initialization.asl") }
// zoning might be split down onto concrete agents e.g. because the explorer
// should prefer probing instead of zoning:
//{ include("../actions/zoning.asl") }
//{ include("../actions/zoning.minion.asl") }
//{ include("../actions/zoning.coach.asl") }

zoneMode(false).
// Agents in general want to form zones. Some agents like explorers might want
// to stay busier for longer and may turn this flag false. Others dynamically
// flip this switch depending on their availability. 
isInterestedInZoning(true).

/* Map Related Stuff */


/*Actions*/
// Try to do an action in every step.
+requestAction:
    position(Position)
	& lastActionResult(Result)
	& lastAction(Action)
	& step(Step)
	& energy(Energy)
    <-
//	.print("Received percept requestAction.");
//	We have to abolish here because we need to make sure that requestAction
//	gets processed in every step.
    .abolish(requestAction);
//  The ignoreEnemy belief is used by agents to ignore "harmless" agents (all
//	agents that aren't explorers) and must be abolished every step as well.
    .abolish(ignoreEnemy);
	.print("[Step ", Step, "] My position is ", Position, ". My last action was '", Action,"'. Result was ", Result,". My energy is ", Energy ,".");
    !doAction.

//Fallback action in the case where we didn't pay attention and tried to perform
//an action without having the energy for it.
+doAction:
	lastActionResult(failed_resources)
	<-
	.print("Warning! I tried to perform an action without having enough energy to do so. Will recharge.");
	recharge.

// If an agent sees an enemy on its position, it has to deal with the enemy.

// If an inspector sees an enemy that currently doesn't count as inspected, inspect it.
// We keep track of the inspected state for enemy agents in the MapAgent.
// TODO: Remove CurrentStep argument from isNotInspected (and get the current step from the MapAgent instead)
+!doAction:
	visibleEntity(Vehicle, Vertex, Team, State)
	& myTeam(MyTeam)
	& MyTeam \== Team
	& role(inspector)
	& ia.isNotInspected(Vehicle)
	<-
	.print("Inspecting ", Vehicle, " at ", Vertex);
	!doInspecting(Vehicle).

// Plan to deal with the enemy if the enemy is not only in range, but currently on our position.
// Ideally, this should never happen.
+!doAction:
 	position(Position)
	& visibleEntity(Vehicle, Position, Team, normal)
	& myTeam(MyTeam)
	& MyTeam \== Team
	& not ignoreEnemy(Vehicle)
 	<-
	.print("Non-disabled enemy ", Vehicle, " at my position!");
 	!dealWithEnemy(Vehicle).

// Saboteurs in defending zone mode should go directly to the disturbing enemy
+!doAction:
	role(saboteur)
	& strategy(zoneDefence)
	& defendingZone(ZoneCentre)  
	& not reached_disturbing_enemy &
	ia.getClosestEnemyPosition(ZoneCentre, EnemyPosition)
 	<- .print("Going to the disturbing enemy on vertex ", EnemyPosition);
 	   !goto(EnemyPosition).

// In defending zone mode saboteur should attack the disturbing enemy once he sees it
+!doAction:
	role(saboteur)
	& strategy(zoneDefence)
	& defendingZone(ZoneCentre)  
	& ia.getClosestEnemyPosition(ZoneCentre, EnemyPosition)
	& myTeam(MyTeam)
	& visibleEntity(Vehicle, EnemyPosition, Team, _)
	& MyTeam \== Team
 	<- .print("Attacking ", Vehicle, " disturbing zone on ", EnemyPosition);
 	   !doAttack(Vehicle, EnemyPosition).

// Saboteurs attack active enemy agents when they see them.
// We need two plans here because saboteurs should prefer attacking enemy
// agents that are on their own node.
+!doAction:
 	position(Position)
	& role(saboteur)
	& visibleEntity(Vehicle, Position, Team, normal)
	& myTeam(MyTeam)
	& MyTeam \== Team
 	<- .print("Attacking ", Vehicle, " on my position ", Position);
 	   !doAttack(Vehicle, Position).
 	   
+!doAction:
 	position(Position)
	& role(saboteur)
	& visibleEntity(Vehicle, Vertex, Team, normal)
	& (visibleEdge(Position, Vertex) | visibleEdge(Vertex, Position))
	& myTeam(MyTeam)
	& MyTeam \== Team
 	<- .print("Attacking ", Vehicle, " on ", Vertex, " from my position ", Position);
 	   !doAttack(Vehicle, Vertex).
 	
// In the case where we have sent a saboteur (or any other agent) to an enemy 'ghost' location (a location
// where an enemy agent used to be, but no longer occupies), we need to tell the
// MapAgent to update its list of enemy positions.
+!doAction:
	position(MyPosition)
//	& role(saboteur)
	& ia.getClosestEnemy(MyPosition, EnemyPosition, Enemy)
	& ia.getDistance(MyPosition, EnemyPosition, Distance)
	& Distance == 1 //better: < visibilityRange
	& not visibleEntity(Enemy, EnemyPosition, _, normal)
	<-
	.print("No enemy agent found at ", EnemyPosition, ". Informing MapAgent.");
	ia.removeEnemyGhost(Enemy);
	!doAction.
	
// Saboteurs should perform aggressively, preferring to attack enemy agents over exploring.
// In the case where they can't see an enemy agent themselves, they get help
// from the MapAgent.
+!doAction:
	role(saboteur)
	& position(MyPosition)
	& ia.getClosestEnemyPosition(MyPosition, EnemyPosition)
	& not ignoreEnemy(Vehicle)
	<-
	.print("Moving to attack enemy on ", EnemyPosition, " from my position ", Position);
	!doAttack(Vehicle, Vertex).

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
//and see normal enemy on its position or on its neighbourhood,do parry
+!doAction:
 	position(Position)
 	& role(sentinel)
 	& zoneNode(Position)
	& (visibleEdge(Position, Vertex) | visibleEdge(Vertex, Position))
	& (visibleEntity(Vehicle, Position, Team, normal) | visibleEntity(Vehicle, Vertex, Team, normal))
	& myTeam(MyTeam)
	& MyTeam \== Team
 	<- .print("I am standing on a zoneNode, and I see enemy nearby. so I parry ");
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

// If the agent has nothing to do, it should recharge instead of doing nothing.
+!doAction:
	energy(Energy)
	& maxEnergy(Max)
	& Energy < Max
	<-
	.print("I'm recharging because I don't know what else to do.");
	recharge.

// Condition to start zoning phase
// TODO: Make entering zone mode more dynamic
+achievement(surveyed640)[source(self)]:
    zoneMode(false)
    <-
    .print("Done with surveying. Entering zone mode.");
    -+zoneMode(true).

+!doAction:
	energy(Energy)
	& maxEnergy(Max)
	& Energy == Max
	& zoneMode(false)
	<- 
	-+zoneMode(true).

+!doAction
    <- skip.