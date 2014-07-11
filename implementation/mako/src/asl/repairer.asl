{ include("base.agent.asl") }
{ include("actions.parry.asl")}

/* Initial beliefs and rules */

/* Initial goals */

/* Plans */
//for repair, repair our team disabled agent who is on the same Vertex. spend 3 energy
//ToDo: repairer can also repair the agent who is undisabled,and spend 2 energy
+step(S):
visibleEntity(Vehicle,CurrVertex,Team,Disabled)[source(percept)]
  & position(MyCurrVertex) & Team == teamA & role(Role) & Role == repairer & Disabled == disabled & lastActionResult(Result) & lastAction(Action) & MyCurrVertex == CurrVertex
    <- .print("I'm on the Vertex (", MyCurrVertex, ") and I will repair ", Vehicle, "who is on the Vertex (", CurrVertex, "). Status is: ", Disabled);
       .perceive;
       .wait(200); // wait until all percepts have been added.          
       !doRepair(Vehicle).
      

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