// Agent baseAgent in project mako
{ include("storeBeliefs.asl") }
{ include("exploreGraph.asl") }
{ include("initialization.asl") }

/* Initial beliefs and rules */
maxEdgeCost(11).

/* Initial goals */

/* Events */

+position(Vertex)[source(percept)]
    <- .send(cartographer, tell, position(Vertex));
       .print("My new position is (", Vertex, ")");
       -+position(Vertex)[source(self)].

+visibleEdge(VertexA, VertexB)[source(percept)]
    <- ?maxEdgeCost(N);
       .send(cartographer, tell, edgePercept(VertexA, VertexB, N)).

+surveyedEdge(VertexA, VertexB, Weight)[source(percept)]
    <- .send(cartographer, tell, edgePercept(VertexA, VertexB, Weight)).

+edges(AmountEdges)[source(percept)]
    <- true. //TODO: send to cartographer

+vertices(AmountVertices)[source(percept)]
    <- true. //TODO: send to cartographer

+probedVertex(Vertex, Value)[source(percept)]:
    .number(Value)
    <- .send(cartographer, tell, probed(Vertex, Value));
       .print("Found new probed vertex (", Vertex, ") with value: ", Value);
       -+probedVertex(Vertex, Value)[source(self)]. // TODO: do we still want/need to set local knowledge?

+visibleVertex(Vertex, Team)[source(percept)]:
    .literal(Team)
    <- //.send(cartographer, tell, visibleVertex(Vertex));
       .send(cartographer, tell, occupied(Vertex, Team)). // TODO: merge this with probed (minimum node value is 1)? Maybe also merge it with position

//TODO: visibleEntity, zoneScore


+simStart
    <- .print("Simulation started.").
    
//TODO: Move to explorer.asl
//For Probe
+step(Step)[source(self)]:
     position(CurrVertex) & lastActionResult(Result) & lastAction(Action) & role(Role) & Role == explorer
    <- .print("Current step is ", Step, ". Current position is (", CurrVertex, "). Result of last action '", Action,"' is ", Result);
       .abolish(iWantToGoTo(_, _, _, _)[source(_)]);
       .perceive;
       .wait(200); // wait until all percepts have been added.
       // Continue with DFS:
       .send(cartographer, askOne, probed(CurrVertex,Value), Reply);
       .print("Try to probe Vertex (", CurrVertex, "). Reply is: ", Reply);           
       !doProbing(CurrVertex, Reply).
       
//TODO: Move to inspector.asl       
// For Inspecting, Inspecting the enemy and broadcast there parameters of enemy agent
+step(S):
    visibleEntity(Vehicle,Vertex,Team,Disabled)[source(percept)] & role(Role) & Role == inspector &   position(MyCurrVertex) & Vertex==MyCurrVertex  
    & Team==teamB & lastActionResult(Result) & lastAction(Action) &  not roleOfAgent(Vehicle,_)
        <- .print("Current step is ", S, ". Result of last action '", Action,"' is ", Result, ").");
           .print("I want to inspect (", Vehicle, ").");
           !doInspect(Vehicle).
           
//TODO: Move to inspector.asl                
+inspectedEntity(Energy, Health, MaxEnergy, MaxHealth,Name,Node,Role,Strength,Team,VisRange)
    <- .print("Enemy agent role is ", Role,". Enemy agent name is ", Name, ". Enemy team is ", Team, "."); 
        .broadcast(tell, parametersOfEnemyAgent(Name,Node,Role,Strength,Team,VisRange));       
       +roleOfAgent(Vehicle, Role).
             
//TODO: Move to saboteur.asl                   
//for attack, attack enemy team agent who is on the same Vertex
+step(S):
visibleEntity(Vehicle,CurrVertex,Team,Disabled)[source(percept)]
  & position(MyCurrVertex) & Team == teamB & role(Role) & Role == saboteur & Disabled == normal & MyCurrVertex == CurrVertex 
    <- .print("I'm on the Vertex (", MyCurrVertex, ") and I will attack ", Vehicle," who is on the Vertex (", CurrVertex, ").");
       .perceive;
       .wait(200); // wait until all percepts have been added.                  
        !doAttack(Vehicle).
   
//TODO: Move to actions.parry.asl                    
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

//TODO: Move to repairer.asl
//for repair, repair our team disabled agent who is on the same Vertex. spend 3 energy
//ToDo: repairer can also repair the agent who is undisabled,and spend 2 energy
+step(S):
visibleEntity(Vehicle,CurrVertex,Team,Disabled)[source(percept)]
  & position(MyCurrVertex) & Team == teamA & role(Role) & Role == repairer & Disabled == disabled & lastActionResult(Result) & lastAction(Action) & MyCurrVertex == CurrVertex
    <- .print("I'm on the Vertex (", MyCurrVertex, ") and I will repair ", Vehicle, "who is on the Vertex (", CurrVertex, "). Status is: ", Disabled);
       .perceive;
       .wait(200); // wait until all percepts have been added.          
       !doRepair(Vehicle).
                                                 
 +step(Step)[source(self)]:
    position(CurrVertex) & lastActionResult(Result) & lastAction(Action)
    <- .print("Current step is ", Step, ". Current position is (", CurrVertex, "). Result of last action '", Action,"' is ", Result);
       .abolish(iWantToGoTo(_, _, _, _)[source(_)]);
       .perceive;
       .wait(200); // wait until all percepts have been added.
        // Continue with DFS:      
        !exploreGraph.
        

        
/* Plans */
