{ include("base.agent.asl") }

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
       !doProbing(CurrVertex).

// if energy is enough - probe     
+ !doProbing(Vertex): 
probed(Vertex, Value)
<-
    .print("(", Vertex,") has been probed. I will do graph exploring!");
    !exploreGraph.
    
// If not probed - probe
+ !doProbing(Vertex,Reply):
 energy(E) & E > 1
<-
   .print("(", Vertex, ") is not probed. Do probing");
    probe.
    
//if energy is not enough - recharge
+ !doProbing(Vertex, Reply):
 energy(E) & E < 1
<-
     .print("I have ", E, " energy, but I need 1 energy to probe. Going to recharge first.");
      recharge.
   
    

