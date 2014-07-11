{ include("base.agent.asl") }

/* Initial beliefs and rules */

/* Initial goals */

/* Plans */
    
// If energy is not enough - recharge  
+ !doAttack(Vehicle):
energy(CurrEnergy) & CurrEnergy < 2
<-
     .print("I have ", CurrEnergy, " energy, but I need 2 energy to attack. Going to recharge first.");
      recharge.
      
// If energy is enough - attack         
+ !doAttack(Vehicle)
<-
    .print("Attacking (", Vehicle, ")."); 
    attack(Vehicle).
    
    

// If energy is not enough - recharge  
+ !doParry:
energy(CurrEnergy) & CurrEnergy < 2
<-
     .print("I have ", CurrEnergy, " energy, but I need 2 energy to parry. Going to recharge first.");
      recharge.
      
// If energy is enough - parry        
+ !doParry
<-
    .print("Parry."); 
     parry.