{ include("base.agent.asl") }

/* Initial beliefs and rules */

/* Initial goals */

/* Plans */ 
// if probed before - do exploreGraph
+ !doProbing(Vertex,Reply): 
//probed(Vertex,Value)
Reply \== false
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
   
    

