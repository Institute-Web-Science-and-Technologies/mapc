{ include("agent.asl") }
{ include("../actions/parry.asl")}
/* Initial beliefs and rules */

/* Initial goals */

/* Plans */
//for repair, repair our team disabled agent who is on the same Vertex. spend 3 energy
//ToDo: repairer can also repair the agent who is undisabled,and spend 2 energy
+help(Vehicle, Vertex) 
	<- //!goto(Vertex);
 	   !doRepair(Vehicle).
      

// If energy is not enough - recharge 
//ToDo: repairer can also repair the agent who is undisabled,and spend 2 energy    
+ !doRepair(Vehicle):
energy(CurrEnergy) & CurrEnergy < 3
<-
     .print("I have ", CurrEnergy, " energy, but I need 3 to repair. Going to recharge first.");
      -+intendedAction(recharge).
      
// If energy is enough - repair    
+ !doRepair(Vehicle)
<-
    .print("Repairing ", Vehicle);
     -+intendedAction(repair(Vehicle)).