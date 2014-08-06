// Agent baseAgent in project mako
{ include("storeBeliefs.asl") }
{ include("../map/explore.asl") }
{ include("../map/initialization.asl") }

zoneMode(false).

/* Map Related Stuff */
// Whenever an agent gets a new position percept, update the belief base, 
// if the new position belief is not already in belief base. 
// Furthermore tell the cartographer about agents current position.
+position(Vertex)[source(percept)]:
	not position(Vertex)[source(self)]
    <- .print("Received percept position(", Vertex, ")."); 
       -+position(Vertex).

/*Actions*/
// Try to do an action in every step.
// Since all other incoming percepts and beliefs
//should be handled before the step belief is processed, we delay the step by assigning a low priority value to it.                        
@delayExecution[priority(-10)]
+!executeAction
	:
    position(Position)
	& lastActionResult(Result)
	& lastAction(Action)
	& step(Step)
    <-
	.print("[Step ", Step, "] My position is (", Position, "). My last action was '", Action,"'. Result was ", Result,".");
    if (Result == successful & Action == survey) {
    	.send(cartographer,tell,vertex(Position, true))
	}
//	Why doesn't this work?
//	.findall([From, To], visibleEdge(From, To), visibleEdgeList);
//	.print("List of visible edges: ", visibleEdgeList);
    !doAction.
//    We have to abolish here, or the agent will ignore 
//    .abolish(requestAction).

// If an agent sees an enemy on its position, it has to deal with the enemy.       
 +!doAction:
 	position(Position)
	& myTeam(MyTeam)
	& visibleEntity(Vehicle, Position, EnemyTeam, Disabled)
	& EnemyTeam \== MyTeam
 	<- .print("Enemy at my position! Disabled -> ", Disabled, ". Vehicle: ", Vehicle );
 		!dealWithEnemy.
 
// If an inspector sees an enemy
 +!doAction:
 	visibleEntity(Vehicle, Position, EnemyTeam, Disabled)
	& myTeam(MyTeam)
	& EnemyTeam \== MyTeam
	& not enemy(Vehicle, _)
	& role(inspector)
 	<- .print("Enemy at my position! Trying to inspect ", Vehicle );
 		!doInspecting(Vehicle).

// If an explorer is on a unprobed vertex, probe it. 
+!doAction:
	zoneMode(false)
	& not probedVertex(Vertex, _)
	& position(Vertex)
	& role(explorer)
	<- .print("I will probe ", Vertex, ".");
	 	!doProbing.
	 	
// If an agent is on a vertex which has an edge to an adjacent not surveyed vertex, survey the position.
+!doAction:
	zoneMode(false)
	& position(Position)
	& (visibleEdge(Position, Vertex) | visibleEdge(Vertex, Position))
	& not surveyedEdge(Position, Vertex, _)
	<-
	.print("Found an unsurveyed edge ", Position, " - ", Vertex, ", will survey.");
	!doSurveying.

// If the energy of the agent is over a threshold of 10 the agent can move to another node.	
+!doAction:
	zoneMode(false) & energy(Energy) & Energy > 10
	<- .print("I want to go to another vertex. My energy is ", Energy, ".");
		!doExploring. 

// If the agent has nothing to do, it can just recharge.
+!doAction:
	energy(Energy) & maxEnergy(Max) & Energy <= Max
	<- .print("I'm idle. I'm recharging.");
		recharge.

// To avoid an enemy agent, we select a destination to go to.
+!avoidEnemy:
	position(Position) & surveyedEdge(Position, _, _) | surveyedEdge(_, Position, _)
	<-
	.findall([Weight, Neighbour], surveyedEdge(Position, Neighbour, Weight), Neighbours);
	.nth(0, Neighbours, Destination); 
	!goto(Destination).

+!avoidEnemy:
	position(Position) & visibleEdge(Position, Destination)
	<- .print("Avoiding enemy over visible edge."); 
	!goto(Destination).
	
+!avoidEnemy:
	position(Position) & visibleEdge(Destination, Position)
	<- .print("Avoiding enemy over visible edge."); 
	!goto(Destination).

// If the agent has enough energy than survey. Otherwise recharge.
+!doSurveying:
 energy(Energy) & Energy < 1
	<- .print("I have not enough energy to survey. I'll recharge first.");
    	recharge.
    	
+!doSurveying:
	position(Position)
	<- .print("Surveying from vertex ", Position, ".");
		survey.
    	
//+!doExploring <- !exploreGraph.

+enemy(Name, Role):
	enemy(Name, Role)
	<- .print("I already was informed about this enemy");
		true.

// Want to goto, but don't have enough energy -> recharge.
+!goto(NextVertex):
    position(CurrVertex) & energy(CurrEnergy) & edge(CurrVertex, NextVertex, Weight) & CurrEnergy < Weight
    <- .print("I have ", CurrEnergy, " energy, but need ", Weight, " to go, going to recharge first.");
       recharge.

// Otherwise just goto.
+!goto(NextVertex)
    <-
    .print("I will move to vertex ", NextVertex, ".");
	goto(NextVertex).