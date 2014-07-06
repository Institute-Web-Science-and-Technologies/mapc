{ include("base.agent.asl") }

/* Initial beliefs and rules */

/* Initial goals */

/* Plans */


// if energy is not enough - recharge  
+ !doParry:
energy(CurrEnergy) & CurrEnergy < 2
<-
     .print("I have ", CurrEnergy, " energy, but need 2 to parry,S going to recharge first.");
      recharge.
// If energy is enough - parry        
+ !doParry
<-
    .print("parry"); 
     parry.