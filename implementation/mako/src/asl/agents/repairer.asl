{ include("agent.asl") }
{ include("../actions/parry.asl")}

role(repairer).

// If energy is not enough - recharge 
+!doRepair(Vehicle, VehiclePosition):
	energy(Energy)
	& position(MyPosition)
	& ia.getDistance(MyPosition, VehiclePosition, Distance)
	& Energy < (4 + Distance)
	<-
    .print("I have ", Energy, " energy, but I need ", 4 + Distance, " to repair ", Vehicle, ". Will recharge.");
    recharge.
      
// If energy is enough - repair    
+!doRepair(Vehicle, _)
	<-
    .print("Repairing ", Vehicle);
    repair(Vehicle).

    