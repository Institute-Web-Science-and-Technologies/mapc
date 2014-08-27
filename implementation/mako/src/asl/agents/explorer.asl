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
	
+!doProbing:
	energy(Energy)
	& Energy < 1
	<-
	.print("I do not have enough energy to probe. I'll recharge first.");
    recharge.

// An explorer should flee from enemy saboteurs.
+!dealWithEnemy(Vehicle): 
	ia.isSaboteur(Vehicle)
	<-
	!avoidEnemy.

+!dealWithEnemy(Vehicle)
	<-
	+ignoreEnemy(Vehicle);
	!doAction.