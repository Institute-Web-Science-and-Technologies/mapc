+!dealWithEnemy <- !doParry.

// If energy is lower than 2, recharge  
+ !doParry:
energy(Energy) & Energy < 2
<- .print("I have ", Energy, " energy, but I need 2 energy to parry. Going to recharge first.");
	recharge.
      
// If energy is enough, parry        
+ !doParry
	<- .print("Parry."); 
		parry.