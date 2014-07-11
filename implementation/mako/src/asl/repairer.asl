{ include("base.agent.asl") }

/* Initial beliefs and rules */

/* Initial goals */

/* Plans */
// If energy is not enough - recharge 
//ToDo: repairer can also repair the agent who is undisabled,and spend 2 energy    
+ !doRepair(Vehicle):
energy(CurrEnergy) & CurrEnergy < 3
<-
     .print("I have ", CurrEnergy, " energy, but I need 3 to repair. Going to recharge first.");
      recharge.
      
// If energy is enough - repair    
+ !doRepair(Vehicle)
<-
    .print("Repairing ", Vehicle);
     repair(Vehicle).
     
{ include("actions.parry.asl")}