// Agent baseAgent in project mako
{ include("../misc/storeBeliefs.asl") }
{ include("../actions/explore.asl") }
{ include("../misc/initialization.asl") }
// zoning might be split down onto concrete agents e.g. because the explorer
// should prefer probing instead of zoning:
//{ include("../actions/zoning.asl") }
//{ include("../actions/zoning.minion.asl") }
//{ include("../actions/zoning.coach.asl") }

zoneMode(false).

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
	.print("[Step ", Step, "] My position is (", Position, "). My last action was '", Action,"'. Result was ", Result,". My energy is ", Energy ,".");
    !doAction.

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

// Saboteurs attack active enemy agents when they see them.
+!doAction:
 	position(Position)
	& role(saboteur)
	& visibleEntity(Vehicle, Vertex, Team, normal)
	& myTeam(MyTeam)
	& MyTeam \== EnemyTeam
 	<- .print("Attacking ", Vehicle, " on ", Vertex, "from my position ", Position);
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
	& ia.isSaboteur(Vehicle)
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
+achievement(surveyed640)[source(self)]
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

	
// If the agent has enough energy, then survey. Otherwise recharge.
+!doSurveying:
	energy(Energy)
	& Energy < 1
	<- .print("I don't have enough energy to survey. I'll recharge first.");
    	recharge.

+!doSurveying:
	position(Position)
	<-
	.print("Surveying ", Position);
	survey.

// In the case where we for some reason get told to move to the node we're already on,
// we perform a recharge action instead.
+!goto(Destination):
	position(Position)
	& Destination == Position
	<-
	.print("Warning! I was told to move to the node I am already on (", Position, "). Will recharge instead.");
	recharge.
	
// Want to goto, but don't have enough energy? Recharge.
+!goto(Destination):
    position(Position)
    & energy(Energy)
    & ia.getEdgeCost(Position, Destination, Costs)
    & Costs > Energy
    <-
	.print("I have ", Energy, " energy, but need ", Costs, " to move to ", Destination, ", going to recharge first.");
    recharge.

// This is the default goto action if we want to move to one of our neighbour nodes.
+!goto(Destination):
	position(Position)
	& ia.getBestHopToVertex(Position, Destination, NextHop)
    <-
    .print("I will move to ", Destination, " by way of ", NextHop);
	goto(NextHop).

// To avoid an enemy agent, ask the MapAgent for best position.
// TODO: Currently, an agent will cycle between two adjacent vertices when
// avoiding an enemy that does not move.
+!avoidEnemy:
	position(Position) 
	& ia.getVertexToAvoidEnemy(Position, Destination)
	<- 
	!goto(Destination).