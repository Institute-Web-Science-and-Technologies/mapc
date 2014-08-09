{ include("agent.asl") }
+!dealWithEnemy <- !avoidEnemy.

 // if energy is not enough - recharge  
+!doInspect(Vehicle):
	energy(CurrEnergy)
	& CurrEnergy < 2
	<-
	.print("I have ", CurrEnergy, " energy, but I need 2 to inspect. Going to recharge first.");
	recharge.

// If energy is enough - attack
+!doInspect(Vehicle) <-
    .print("Inspecting vehicle ", Vehicle, "."); 
     inspect(Vehicle).