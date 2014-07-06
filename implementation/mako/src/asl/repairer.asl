{ include("base.agent.asl") }

/* Initial beliefs and rules */

/* Initial goals */

/* Plans */
// if energy is not enough - recharge 
//ToDo: repairer can also repair the agent who is undisabled,and spend 2 energy    
+ !doRepair(Vehicle):
energy(CurrEnergy) & CurrEnergy < 3
<-
     .print("I have ", CurrEnergy, " energy, but need 3 to repair,going to recharge first.");
      recharge.
      
// if energy is enough - repair    
+ !doRepair(Vehicle)
<-
    .print("repairing", Vehicle);
     repair(Vehicle).
     
     

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