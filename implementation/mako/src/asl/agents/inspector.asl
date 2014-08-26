{ include("agent.asl") }

// If the agent has enough energy, then inspect. Otherwise recharge.
+!doInspecting(Vehicle):
	energy(Energy)
	& Energy < 2
	<-
    .print("I have ", CurrEnergy, " energy, but I need 2 to inspect. Going to recharge first.");
    recharge.

// Inspect if enough energy.
+!doInspecting(Vehicle)
	<-
    .print("Inspecting ", Vehicle);
    inspect(Vehicle).

// Inspector has to flee from enemy saboteurs
+!dealWithEnemy(Vehicle):
	ia.isSaboteur(Vehicle)
	<-
	!avoidEnemy.

+!dealWithEnemy(Vehicle)
	<-
	+ignoreEnemy(Vehicle);
	!doAction.
