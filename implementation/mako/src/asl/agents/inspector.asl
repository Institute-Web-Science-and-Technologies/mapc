{ include("agent.asl") }

+!dealWithEnemy <- !avoidEnemy.

// For Inspecting, Inspecting the enemy and broadcast there parameters of enemy agent
+step(S):
    visibleEntity(Vehicle,Vertex,Team,Disabled)[source(percept)] & role(Role) & Role == inspector &   position(MyCurrVertex) & Vertex==MyCurrVertex  
    & Team==teamB & lastActionResult(Result) & lastAction(Action) &  not roleOfAgent(Vehicle,_)
        <- .print("Current step is ", S, ". Result of last action '", Action,"' is ", Result, ").");
           .print("I want to inspect (", Vehicle, ").");
           !doInspect(Vehicle).
           
+inspectedEntity(Energy, Health, MaxEnergy, MaxHealth,Name,Node,Role,Strength,Team,VisRange)
    <- .print("Enemy agent role is ", Role,". Enemy agent name is ", Name, ". Enemy team is ", Team, "."); 
        .broadcast(tell, parametersOfEnemyAgent(Name,Node,Role,Strength,Team,VisRange));       
       +roleOfAgent(Vehicle, Role).
           
 // if energy is not enough - recharge  
+ !doInspect(Vehicle):
energy(CurrEnergy) & CurrEnergy < 2
<-
     .print("I have ", CurrEnergy, " energy, but I need 2 to inspect. Going to recharge first.");
      recharge.
// If energy is enough - attack         
+ !doInspect(Vehicle)
<-
    .print("Inspecting ", Vehicle); 
     inspect(Vehicle).