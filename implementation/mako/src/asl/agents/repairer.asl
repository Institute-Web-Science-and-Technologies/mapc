{ include("agent.asl") }
{ include("../actions/parry.asl")}

role(repairer).

// If energy is not enough - recharge 
//Todo: repairer can also repair the agent who is undisabled,and spend 2 energy    
+!doRepair(Vehicle, VehiclePosition):
	energy(Energy)
	& position(MyPosition)
	& ia.getDistance(MyPosition, VehiclePosition, Distance)
	& Energy < (3 + Distance)
	<-
    .print("I have ", Energy, " energy, but I need ", 3 + Distance, " to repair ", Vehicle, ". Will recharge.");
    recharge.
      
// If energy is enough - repair    
+!doRepair(Vehicle, _)
	<-
    .print("Repairing ", Vehicle);
    repair(Vehicle).