{ include("base.agent.asl") }

/* Initial beliefs and rules */

/* Initial goals */

/* Plans */
 // if energy is not enough - recharge  
+ !doInspect(Vehicle):
energy(CurrEnergy) & CurrEnergy < 2
<-
     .print("I have ", CurrEnergy, " energy, but need 2 to inspect,S going to recharge first.");
      recharge.
// If energy is enough - attack         
+ !doInspect(Vehicle)
<-
    .print("inspecting",Vehicle); 
     inspect(Vehicle).