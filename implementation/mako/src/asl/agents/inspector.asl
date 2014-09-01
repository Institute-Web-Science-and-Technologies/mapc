{ include("agent.asl") }

role(inspector).

// If the agent has enough energy, then inspect. Otherwise recharge.
+!doInspecting(Vehicle, VehiclePosition):
	energy(Energy)
	& position(MyPosition)
	& ia.getDistance(MyPosition, VehiclePosition, Distance)
	& Energy < (2 + Distance)
	<-
    .print("I have ", Energy, " energy, but I need ", 2 + Distance, " to inspect ", Vehicle, ". Will recharge.");
    recharge.

// Inspect if enough energy.
+!doInspecting(Vehicle, _)
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
