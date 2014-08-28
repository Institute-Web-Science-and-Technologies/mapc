{ include("agent.asl") }

role(explorer).

!start.

// TODO: Switch the interest for zoning to true when the map is fully probed.
+!start <- -+isInterestedInZoning(false).

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

// An explorer should flee from enemy saboteurs.
+!dealWithEnemy(Vehicle): 
	ia.isSaboteur(Vehicle)
	<-
	!avoidEnemy.

+!dealWithEnemy(Vehicle)
	<-
	+ignoreEnemy(Vehicle);
	!doAction.