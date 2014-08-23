// Agent baseAgent in project mako
{ include("storeBeliefs.asl") }
{ include("explore.asl") }
{ include("initialization.asl") }

zoneMode(false).

/* Map Related Stuff */
// Condition to start zoning phase
+achievement(Identifier)[source(self)]:
    Identifier == surveyed640 // not very dynamic, but whatever
    <-
    .print("Done with surveying. Entering zone mode.");
    -+zoneMode(true).

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

// If an agent sees an enemy on its position, it has to deal with the enemy
// before doing anything else.       
 +!doAction:
 	position(Position)
	& myTeam(MyTeam)
	& visibleEntity(Vehicle, Position, EnemyTeam, State)
	& EnemyTeam \== MyTeam
	& not ignoreEnemy(Vehicle)
	& State \== disabled
 	<-
	.print("Enemy ", Vehicle, " at my position! Vehicle state: ", State);
 	!dealWithEnemy(Vehicle).

// To avoid an enemy agent, ask the MapAgent for best position.
// TODO: Currently, an agent will cycle between two adjacent vertices when
// avoiding an enemy that does not move.
+!avoidEnemy:
	position(Position) 
	& ia.getVertexToAvoidEnemy(Position, Destination)
	<- 
	!goto(Destination).
 
// If an inspector sees an enemy (that we haven't inspected before), inspect it.
 +!doAction:
 	visibleEntity(Vehicle, Position, EnemyTeam, Disabled)
	& myTeam(MyTeam)
	& EnemyTeam \== MyTeam
	& step(CurrentStep)
	& ia.isNotInspected(Vehicle, CurrentStep)
	& role(inspector)
 	<- .print("Enemy in range! Trying to inspect ", Vehicle );
 		!doInspecting(Vehicle).

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

// If the agent has enough energy, then survey. Otherwise recharge.
+!doSurveying:
 energy(Energy) & Energy < 1
	<- .print("I don't have enough energy to survey. I'll recharge first.");
    	recharge.
    	
+!doSurveying:
	position(Position)
	<-
	.print("Surveying vertex: ", Position, ".");
	survey.

// If the energy of the agent is over a threshold of 10 the agent can move to another node.	
+!doAction:
	zoneMode(false)
	& energy(Energy)
	& Energy > 10
	<-
	.print("I want to go to another vertex. My energy is ", Energy, ".");
	!doExploring. 

// If the agent has nothing to do, it should recharge instead of doing nothing.
+!doAction:
	energy(Energy)
	& maxEnergy(Max)
	& Energy <= Max
	<-
	.print("I'm recharging.");
	recharge.

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
    .print("I will move to vertex ", Destination, " through vertex ", NextHop);
	goto(NextHop).