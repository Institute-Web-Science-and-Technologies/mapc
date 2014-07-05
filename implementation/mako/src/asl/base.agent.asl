// Agent baseAgent in project mako
{ include("actions.goto.asl") }
{ include("actions.repair.asl") }
{ include("actions.recharge.asl") }
{ include("storeBeliefs.asl") }
{ include("exploreGraph.asl") }
{ include("initialization.asl") }

//Print all received beliefs. Used for debugging. (comment this out if your simulation crashes immediately)
//@debug[atomic] +Belief <-
//    .print("Received new belief from percept: ", Belief);
//    for (B) {
//        .print("    When ", Belief, " is added, this is another belief in the belief base: ", B);
//    }
//    -Belief;
//    .print("        Removed ", Belief, " from belief base.").


/* Initial beliefs and rules */
lowEnergy :- energy(Energy)[source(percept)] & Energy < 8.

/* Initial goals */

/* Events */

+position(Vertex)[source(percept)]
    <- .send(cartographer, tell, position(Vertex));
       .print("New position(", Vertex, ")");
       -+position(Vertex)[source(self)].

+visibleEdge(VertexA, VertexB)[source(percept)]
    <- .send(cartographer, tell, edgePercept(VertexA, VertexB, 1000)).

+surveyedEdge(VertexA, VertexB, Weight)[source(percept)]
    <- .send(cartographer, tell, edgePercept(VertexA, VertexB, Weight)).

+edges(AmountEdges)[source(percept)]
    <- true. //TODO: send to cartographer

+vertices(AmountVertices)[source(percept)]
    <- true. //TODO: send to cartographer

+probedVertex(Vertex, Value)[source(percept)]:
    .number(Value)
    <- .send(cartographer, tell, probed(Vertex, Value));
       .print("New probedVertex: ", Vertex, " with value: ", Value);
       -+probedVertex(Vertex, Value)[source(self)]. // TODO: do we still want/need to set local knowledge?

+visibleVertex(Vertex, Team)[source(percept)]:
    .literal(Team)
    <- //.send(cartographer, tell, visibleVertex(Vertex));
       .send(cartographer, tell, occupied(Vertex, Team)). // TODO: merge this with probed (minimum node value is 1)? Maybe also merge it with position

//TODO: visibleEntity, zoneScore


+simStart
    <- .print("Simulation started.").
    
//For Probe
+step(Step)[source(self)]:
     position(CurrVertex) & lastActionResult(Result) & lastAction(Action) & role(Role) & Role == explorer
    <- .print("Current step is ", Step, " current position is ", CurrVertex, " result of last action ", Action," is ", Result);
       .abolish(iWantToGoTo(_, _, _, _)[source(_)]);
       .perceive;
       .wait(200); // wait until all percepts have been added.
       // Continue with DFS:
       .send(cartographer, askOne, probed(CurrVertex,Value));       	
       .wait(100);   
       .print("probing",CurrVertex);           
       !doProbing(CurrVertex).
       
// For Inspecting, Inspecting the enemy and broadcaste there parametersofenemyagent
+step(S):
    visibleEntity(Vehicle,Vertex,Team,Disabled)[source(percept)] & role(Role) & Role == inspector &   position(MyCurrVertex) & Vertex==MyCurrVertex  
    & Team==teamB & lastActionResult(Result) & lastAction(Action) &  not roleOfAgent(Vehicle,_)
        <- .print("Current step is ", S, " result of last action ", Action," is ", Result);
           .print("I want to inspect", Vehicle);
           !doInspect(Vehicle).
          
+inspectedEntity(Energy,Health,MaxEnergy,MaxHealth,Name,Node,Role,Strength,Team,VisRange)
    <- .print("enemy agent role ", Role,"enemy agent name", Name, "Enemy team is",Team); 
        .broadcast(tell, parametersOfEnemyAgent(Name,Node,Role,Strength,Team,VisRange));       
       +roleOfAgent(Vehicle, Role).
             
//for attack, attack enemy team agent who is on the same Vertex
+step(S):
visibleEntity(Vehicle,CurrVertex,Team,Disabled)[source(percept)]
  & position(MyCurrVertex) &myName(Name)& Team==teamB & role(Role) & Role == saboteur & Disabled == normal & MyCurrVertex == CurrVertex 
    <- .print(Name, "is on the Vertex:",MyCurrVertex, "and it will attack",Vehicle,"who is on the",CurrVertex, Disabled );
       .perceive;
       .wait(200); // wait until all percepts have been added.                  
        !doAttack(Vehicle).
              
//for parry. When see an enemy Saboteur in the same Vertex, then do parry. 
// But the role of enemy agent returns some numbers which we don't know the meaning
// here I just assum role 6 is Saboteur.
+step(S):
role(MyRole) & MyRole \== explorer & MyRole \==inspector &
visibleEntity(Vehicle,CurrVertex,Team,Disabled)[source(percept)]
  & position(MyCurrVertex) &myName(Name)& MyCurrVertex == CurrVertex & Team==teamB & parametersOfEnemyAgent(Vehicle,Node,Role,Strength,Team,VisRange) & Role == 6
    <- .print(Name, "is on the Vertex:",MyCurrVertex, "and it will parry the attack from",Vehicle,"who is on the",CurrVertex);
       .perceive;
       .wait(200); // wait until all percepts have been added.          
       !doParry. 

//for repair, repair our team disabled agent who is on the same Vertex. spend 3 energy
//ToDo: repairer can also repair the agent who is undisabled,and spend 2 energy
+step(S):
visibleEntity(Vehicle,CurrVertex,Team,Disabled)[source(percept)]
  & position(MyCurrVertex) &myName(Name)& Team==teamA & role(Role) & Role == repairer & Disabled == disabled & lastActionResult(Result) & lastAction(Action) & MyCurrVertex == CurrVertex
    <- .print(Name, "is on the Vertex:",MyCurrVertex, "and it will repair",Vehicle,"who is on the",CurrVertex, Disabled,  " result of last action ", Action," is ", Result);
       .perceive;
       .wait(200); // wait until all percepts have been added.          
       !doRepair(Vehicle).
                                                 
 +step(Step)[source(self)]:
    position(CurrVertex) & lastActionResult(Result) & lastAction(Action)
    <- .print("Current step is ", Step, " current position is ", CurrVertex, " result of last action ", Action," is ", Result);
       .abolish(iWantToGoTo(_, _, _, _)[source(_)]);
       .perceive;
       .wait(200); // wait until all percepts have been added.
        // Continue with DFS:      
        !exploreGraph.

// If not probed - probe
+ !doProbing(Vertex):
 not probed(Vertex,Value)
<-
   .print(Vertex,"is not probed,do probing");
    probe.
    
//if energy is not enough - recharge
+ !doProbing(Vertex):
energy(CurrEnergy) & CurrEnergy < 1
<-
     .print("I have ", CurrEnergy, " energy, but need 1 to probe going to recharge first.");
      recharge.
      
// if energy is enough - probe     
+ !doProbing(Vertex)
<-
    .print(Vertex,"has been probed,it will do graph exploring!");
    !exploreGraph.
    
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
    
// if energy is not enough - recharge  
+ !doAttack(Vehicle):
energy(CurrEnergy) & CurrEnergy < 2
<-
     .print("I have ", CurrEnergy, " energy, but need 2 to attack,S going to recharge first.");
      recharge.
// If energy is enough - attack         
+ !doAttack(Vehicle)
<-
    .print("attacking",Vehicle); 
    attack(Vehicle).
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
/* Plans */
