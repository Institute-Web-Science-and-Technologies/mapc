//Only parry if the enemy agent seems to be a saboteur.
+!dealWithEnemy(Vehicle): 
	ia.isSaboteur(Vehicle)
	<-
	.print(Vehicle, " is a saboteur! I will parry.");
	!doParry.

//Ignore the enemy agent if it's not a saboteur and do something else.
+!dealWithEnemy(Vehicle)
	<-
	.print(Vehicle, " isn't a saboteur! I will ignore him.");
	+ignoreEnemy(Vehicle);
	!doAction.

// If energy is lower than 2, perform recharge action.  
+!doParry:
	energy(Energy)
	& Energy < 2
	<-
	.print("I have ", Energy, " energy, but I need 2 energy to parry. Going to recharge first.");
	recharge.
      
// If energy is enough, perform parry action.        
+!doParry
	<-
	.print("Parrying."); 
	parry.