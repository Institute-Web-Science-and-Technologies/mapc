{ include("base.agent.asl") }

/* Initial beliefs and rules */

/* Initial goals */

/* Plans */
   
//for attack, attack enemy team agent who is on the same Vertex
+step(S):
visibleEntity(Vehicle,CurrVertex,Team,Disabled)[source(percept)]
  & position(MyCurrVertex) & Team == teamB & role(Role) & Role == saboteur & Disabled == normal & MyCurrVertex == CurrVertex 
    <- .print("I'm on the Vertex (", MyCurrVertex, ") and I will attack ", Vehicle," who is on the Vertex (", CurrVertex, ").");
       .perceive;
       .wait(200); // wait until all percepts have been added.                  
        !doAttack(Vehicle).
    
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
    
{ include("actions.parry.asl")}