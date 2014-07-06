{ include("base.agent.asl") }

/* Initial beliefs and rules */

/* Initial goals */

/* Plans */
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