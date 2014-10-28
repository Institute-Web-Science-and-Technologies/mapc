{ include("agent.asl") }

role(explorer).

// If the agent has enough energy, then probe. Otherwise recharge.
+!doProbing:
	energy(Energy)
	& Energy > 1
	& position(Position)
	<-
	.print("Probing vertex ", Position, ".");
	probe.
	
+!doProbing<-
	.print("I do not have enough energy to probe. I'll recharge first.");
    recharge.

// If the vertex to probe is outside of our probing range, perform a goto instead
// of probe.
+!doRangedProbing(Vertex):
	position(MyPosition)
	& ia.getDistance(MyPosition, Vertex, Distance)
	& visRange(MyRange)
	& Distance > (MyRange / 2)
	<-
	.print("I want to probe ", Vertex, ", but it is ", Distance, " steps away, and my visibility range is only ", MyRange);
	!goto(Vertex).

// If not enough energy - recharge
+!doRangedProbing(Vertex):
    energy(MyEnergy)
    & position(MyPosition)
    & ia.getDistance(MyPosition, Vertex, Distance)
	& MyEnergy < (1 + Distance)
    <- .print("I have ", MyEnergy, " energy, but I need ", 1 + Distance, " energy to remotely probe ", Vertex, ". I will recharge.");
       recharge.

// All requirements are met - do ranged probing.
+!doRangedProbing(Vertex)
	<-
	.print("Probing remote vertex ", Vertex, ".");
	probe(Vertex).

// An explorer should flee from enemy saboteurs.
+!dealWithEnemy(Vehicle): 
	ia.isSaboteur(Vehicle)
	<-
	!avoidEnemy(Vehicle).
// The explorer will ignore all other enemy agent types.
+!dealWithEnemy(Vehicle)
	<-
	+ignoreEnemy(Vehicle);
	!doAction.