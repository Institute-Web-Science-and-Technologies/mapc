{ include("agent.asl") }
//for attack, attack enemy team agent who is on the same Vertex
+!dealWithEnemy(Vehicle)
	<-
	!doAttack(Vehicle).
    
// If energy is not enough - recharge  
+!doAttack(Vehicle):
	energy(Energy)
	& Energy < 2
	<-
	.print("I have ", Energy, " energy, but I need 2 energy to attack. Going to recharge first.");
    recharge.
      
// If energy is enough - attack         
+!doAttack(Vehicle)
	<-
	.print("Attacking (", Vehicle, ")."); 
    attack(Vehicle).
