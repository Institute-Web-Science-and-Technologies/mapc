/* Initial beliefs and rules */

/* Initial goals */

/* Plans */
// for parry. When see an enemy Saboteur in the same Vertex, then do parry. 
// But the role of enemy agent returns some numbers which we don't know the meaning
// here I just assum role 6 is Saboteur.
+step(S):
role(MyRole) & MyRole \== explorer & MyRole \==inspector &
visibleEntity(Vehicle,CurrVertex,Team,Disabled)[source(percept)]
  & position(MyCurrVertex) & MyCurrVertex == CurrVertex & Team == teamB & parametersOfEnemyAgent(Vehicle,Node,Role,Strength,Team,VisRange) & Role == 6
    <- .print("I'm on the Vertex (", MyCurrVertex, ") and I will parry the attack from ", Vehicle, " who is on the Vertex (", CurrVertex, ").");
       .perceive;
       .wait(200); // wait until all percepts have been added.          
       !doParry. 

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