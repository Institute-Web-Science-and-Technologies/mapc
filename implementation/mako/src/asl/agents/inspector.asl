{ include("agent.asl") }

role(inspector).

// If the agent we want to inspect is out of range, we move towards it instead.
+!doInspecting(Vehicle, VehiclePosition):
	position(MyPosition)
	& ia.getDistance(MyPosition, VehiclePosition, Distance)
	& visRange(MyRange)
	& Distance > (MyRange / 2)
	<-
	.print("I want to inspect ", Vehicle, ", but it is ", Distance, " steps away, and my visibility range is only ", MyRange);
	!goto(VehiclePosition).
	
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
	!avoidEnemy(Vehicle).

+!dealWithEnemy(Vehicle)
	<-
	+ignoreEnemy(Vehicle);
	!doAction.
