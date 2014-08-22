//TODO: Only parry if the enemy saboteur is in range and has the energy to attack me.
+!dealWithEnemy(Vehicle): 
	ia.isSaboteur(Vehicle)
	<-
	!doParry.

+!dealWithEnemy(Vehicle)
	<-
	+ignoreEnemy(Vehicle);
	!doAction.

// If energy is lower than 2, recharge  
+!doParry:
	energy(Energy)
	& Energy < 2
	<-
	.print("I have ", Energy, " energy, but I need 2 energy to parry. Going to recharge first.");
	recharge.
      
// If energy is enough, parry        
+!doParry
	<-
	.print("Parrying."); 
	parry.