// Agent baseAgent in project mako
{ include("storeBeliefs.asl") }
{ include("../map/exploreGraph.asl") }
{ include("../map/initialization.asl") }

zoneMode(false).

+simStart <- .print("Simulation started.").

/* TODO */
//TODO: visibleEntity, zoneScore

/* Map Related Stuff */
// Whenever an agent gets a new position percept, update the belief base, 
// if the new position belief is not already in belief base. 
// Furthermore tell the cartographer about agents current position.
// Reset the agents flags for exploring, probing and surveying.
+position(Vertex)[source(percept)]:
	position(Vertex)[source(self)]
	<- //.print("I already sent my position to the cartographer.");
		true.
	
+position(Vertex)[source(percept)]
    <- .print("I'm now at position: (", Vertex, ")."); 
       .send(cartographer, tell, position(Vertex));
       -+position(Vertex)[source(self)];
       -+isSurveying(false);
	   -+isProbing(false);
	   -+isExploring(false)
	   -+doneSurveying(false).

/*Actions*/                              
 +step(Step)[source(self)]:
    position(Position) & lastActionResult(Result) & lastAction(Action)
    <- .print("[Step ", Step, "] My position is (", Position, "). My last action was '", Action,"'. Result was ", Result,".");
       !doAction.

// If an agent sees an enemy on its position, it has to deal with the enemy.       
 +!doAction:
 	position(Position) & myTeam(MyTeam) & visibleEntity(Vehicle, Position, EnemyTeam, Disabled) & EnemyTeam \== MyTeam //& Vehicle = "saboteur"
 	<- .print("Enemy at my position! Disabled -> ", Disabled, ". Vehicle: ", Vehicle );
 		!dealWithEnemy.

+!doAction:
	zoneMode(false) & isProbing(false) & role(explorer)
	<- .print("I'm trying to probe.");
		-+isProbing(true); 
	 	!doProbing.
	 	
+!doAction:
	zoneMode(false) & isSurveying(false)
	<- .print("I'm trying to survey.");
		-+isSurveying(true);
		!doSurveying.
	
+!doAction:
	zoneMode(false) & isExploring(false)
	<- .print("I want to go to another vertex.");
		-+isExploring(true); 
		!doExploring. 
	
+!doAction:
	energy(Energy) & maxEnergy(Max) & Energy < Max
	<- .print("I'm idle. I'm recharging.");
		recharge.

+!doAction:
	zoneMode(false)
	<- .print("I'm idle. I'm waiting for answers.").
	
+!doAction:
	zoneMode(true)
	<- .print("I'm idle. I'm staying at my Zone.").

// To avoid an enemy agent, we select a destination to go to.
// TODO: Optimization -> select the cheapest edge with "findAll". 
+!avoidEnemy:
	position(Position) & surveyedEdge(Position, Destination, Weight)
	<- !goto(Destination).

+!avoidEnemy:
  position(Position) & surveyedEdge(Position, Destination, Weight)
  <- !goto(Destination).

+!avoidEnemy:
	position(Position) & surveyedEdge(Destination, Position, Weight)
	<- !goto(Destination).
	
	
+!doSurveying <- true.
+!doExploring <- true.
