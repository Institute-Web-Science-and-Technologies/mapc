{ include("agent.asl") }
//for attack, attack enemy team agent who is on the same Vertex
+!dealWithVisRangeOneEnemy(Vehicle,Vertex)<- !doAttack(Vehicle,Vertex).

+ !doAttack(Vehicle,Vertex):
 lastActionResult(Result) & Result == failed_in_range
 & lastAction(Action) & Action == attack
<- .print("I failed to attack", Vehicle, " I will follow it.");
   !goto(Vertex).        
    
// If energy is not enough - recharge  
+ !doAttack(Vehicle, Vertex):
energy(Energy) & Energy < 2
<- .print("I have ", Energy, " energy, but I need 2 energy to attack. Going to recharge first.");
   recharge.
      
// If energy is enough - attack         
+ !doAttack(Vehicle, Vertex)
<- .print("Attacking (", Vehicle, ")."); 
    attack(Vehicle).

    