{ include("../misc/storeBeliefs.asl") }
{ include("../actions/explore.asl") }
{ include("../actions/goto.asl") }
{ include("../actions/doSurveying.asl") }
{ include("../actions/avoidEnemy.asl") }
{ include("../actions/getRepaired.asl") }
// zoning might be split down onto concrete agents e.g. because the explorer
// should prefer probing instead of zoning:
{ include("../actions/zoning.asl") }
{ include("../misc/initialization.asl") }

zoneMode(false).

//We only receive the failed_status result if we tried to perform an action that
//we can't perform while disabled - from this we can infer that our health is
//0. Ideally, we wouldn't need this plan because we should always receive the
//health(Health) percepts properly - but from what I've seen, this isn't the
//case, so this plan serves as an alternative to finding out whether or not
//we're disabled.
+lastActionResult(failed_status) <-
	.print("Received lastActionResult(failed_status) - which means I didn't know that I'm disabled!")
	-+health(0)[source(self)].

+health(0)[source(self)]:
	step(Step)
	& not disabledSince(_)
	<-
	+disabledSince(Step);
	.print("I have been disabled.").
	
+health(Health)[source(self)]:
	Health > 0
	& step(Step)
	& disabledSince(DisabledSinceStep)
	& StepsDisabled = Step - DisabledSinceStep
	<-
	.print("I am no longer disabled. It took ", StepsDisabled, " steps to repair me.");
	.abolish(disabledSince(_)).
	
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
    .abolish(ignoreEnemy(_));
//  The nothingToExplore belief is used to catch the case where we tried to
//	explore, but there were no nodes left to explore. We have to abolish this
//	every turn because it might be the case that a new part of the map has been
//	revealed and we can explore once again. 
    .abolish(nothingToExplore);
	.print("[Step ", Step, "] My position is ", Position, ". My last action was '", Action,"'. Result was ", Result,". My energy is ", Energy ,".");
    !doAction.

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
	& myName(MyName)
	& MyName \== Vehicle
	<-
	.print("I see the disabled agent ", Vehicle, " on ", VehiclePosition, " - will try to repair it.");
	!doRepair(Vehicle, VehiclePosition).

//// If agent is disabled - get repaired. If he was in zoneMode, it is properly
//// terminated.
// +!doAction:
// 	health(0)
//    <- !quitZoneMode;
//       !preparedGettingRepaired.

// If a friendly non-disabled agent requiring repair is within half of the visibility range of a repairer,
// repair it.
+!doAction:
	role(repairer)
	& position(MyPosition)
	& myTeam(MyTeam)
	& visibleEntity(Vehicle, VehiclePosition, MyTeam, _)
	& ia.needsRepair(Vehicle)
	& visRange(MyRange)
	& Distance <= (MyRange / 2)
	& myName(MyName)
	& MyName \== Vehicle
	<-
	.print("I see a non-disabled agent ", Vehicle, " on ", VehiclePosition, " which require repair - will try to repair it.");
	!doRepair(Vehicle, VehiclePosition).
    
//Fallback action in the case where we didn't pay attention and tried to perform
//an action without having the energy for it.
+!doAction:
	lastActionResult(failed_resources)
	<-
	.print("Warning! I tried to perform an action without having enough energy to do so. Will recharge.");
	recharge.
	
// If an inspector sees an enemy that currently doesn't count as inspected, inspect it.
// We keep track of the inspected state for enemy agents in the Java agent class.
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
	
// If you're in range of what could be an active enemy saboteur, get out of there.
// But only if you're not in zone mode.
+!doAction:
	position(MyPosition)
	& visibleEntity(Vehicle, VehiclePosition, VehicleTeam, normal)
	& myTeam(MyTeam)
	& MyTeam \== VehicleTeam
	& not ignoreEnemy(Vehicle)
	& ia.couldBeSaboteur(Vehicle, VehicleVisRange)
	& ia.getDistance(MyPosition, VehiclePosition, Distance)
	& Distance <= VehicleVisRange
	& not zoneNode(_)
	& not health(0)
	<-
	.print("Danger! Active enemy could-be saboteur ", Vehicle, " on ", VehiclePosition, " is in attacking range!");
	!avoidEnemy(Vehicle).
    

//Test plan for buying: What happens if we have an uber-saboteur?
+!doAction:
	.my_name(saboteur1)
	& myName(MyName)
	& energy(MyEnergy)
	& MyEnergy >= 2
	& ia.buyUpgrade(MyName, Upgrade)
	<-
	.print("Buying upgrade: ", Upgrade);
	buy(Upgrade).
	
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

// If there is a not reserved disabled agent to repair and not in zone mode - go to its position.
// Prevent going if the disabled agent in the neigbourhood
+!doAction:
	role(repairer)
	& zoneMode(false)
	& .my_name(MyName)
	& ia.getClosestDisabledAgent(MyName, DisabledAgent, DisabledAgentPosition)
	& position(Position)
	& Position \== DisabledAgentPosition 
    & not (visibleEdge(Position, DisabledAgentPosition) | visibleEdge(DisabledAgentPosition, Position))
	<-
	.print("Will move towards disabled agent ", DisabledAgent, " at ", DisabledAgentPosition, " while exploring.");
	!goto(DisabledAgentPosition).

// If a disabled agent is in the neigbourhood - wait for him and recharge.
+!doAction:
	role(repairer)
	& zoneMode(false)
	& .my_name(MyName)
	& ia.getClosestDisabledAgent(MyName, DisabledAgent, DisabledAgentPosition)
	<-
	.print("I see the disabled agent ", DisabledAgent, " coming to me in the neighbourhood, will wait for him and recharge.");
	recharge.

// Inspectors should inspect aggressively, that is, actively seek out enemy agents
// that are not inspected yet.
+!doAction:
	role(inspector)
	& zoneMode(false)
	& position(MyPosition)
	& ia.getClosestUninspectedEnemy(MyPosition, Enemy, EnemyPosition)
	<-
	.print("Attempting to inspect ", Enemy, " on ", EnemyPosition, ". I'm on ", MyPosition);
	!doInspecting(Enemy, EnemyPosition).
	
// If we're not in zone mode yet, explore.
+!doAction:
	zoneMode(false)
	& not nothingToExplore
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
    <- .abolish(zoneGoalVertex(GoalVertex)[source(_)]);
       -+zoneNode(GoalVertex);
       .print("[zoning] I am now standing on my zone node.");
       !doAction.

// If we got a zoneGoalVertex, which is a node we should move to to build a
// zone, we will move there.
// This method is used by minions and coaches alike. Coaches will only have to
// to 0-1 steps to reach their goal though.
+!doAction:
    zoneGoalVertex(GoalVertex)
    <- .print("[zoning] I'm going to a zone node at ", GoalVertex);
       !goto(GoalVertex).

// If the agent has nothing to do, it should recharge instead of doing nothing.
+!doAction:
	energy(Energy)
	& maxEnergy(Max)
	& Energy < Max
	<-
	.print("I'm recharging because I don't know what else to do.");
	recharge.

+!doAction:
	energy(Energy)
	& maxEnergy(Max)
	& Energy == Max
	& zoneMode(false)
    & achievement(surveyed640)
    & not role(saboteur)
	<- .print("[zoning] There is nothing left that I could do. 640 edges are already surveyed. So I switch to zoneMode.");
	-+zoneMode(true);
    !doAction.

+!doAction
    <- .print("I have nothing to do. I'll skip."); 
    skip.
