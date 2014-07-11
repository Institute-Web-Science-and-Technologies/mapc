{ include("base.agent.asl") }

/* Initial beliefs and rules */

/* Initial goals */

/* Plans */      
// if energy is enough - probe     
+ !doProbing(Vertex): 
probed(Vertex, Value)
<-
    .print("(", Vertex,") has been probed. I will do graph exploring!");
    !exploreGraph.
    
// If not probed - probe
+ !doProbing(Vertex):
 not probed(Vertex,Value) & energy(E) & E > 1
<-
   .print("(", Vertex, ") is not probed. Do probing");
    probe.
    
//if energy is not enough - recharge
+ !doProbing(Vertex):
not probed(Vertex,Value) & energy(E) & E < 1
<-
     .print("I have ", E, " energy, but I need 1 energy to probe. Going to recharge first.");
      recharge.
