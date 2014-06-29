// Agent baseAgent in project mako
{ include("actions.goto.asl") }
{ include("actions.repair.asl") }
{ include("actions.recharge.asl") }
{ include("storeBeliefs.asl") }
{ include("exploreGraph.asl") }

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
    <- internalActions.setGlobalEdgesAmount(AmountEdges). //TODO: send to cartographer

+vertices(AmountVertices)[source(percept)]
    <- internalActions.setGlobalVerticesAmount(AmountVertices).

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
    
+step(Step)[source(self)]:
    position(CurrVertex) & lastActionResult(Result) & lastAction(Action) & role(Role)
    <- .print("Current step is ", Step, " current position is ", CurrVertex, " result of last action ", Action," is ", Result);
   //     .send(cartographer, achieve, announceStep(Step));
       .perceive;
       .wait(200); // wait until all percepts have been added.
        // Continue with DFS:
        if(Role == explorer)
        {  .send(cartographer, askOne, probed(CurrVertex,Value));       	
             .wait(100);   
             .print("probing",CurrVertex);           
             !doProbing(CurrVertex)                     
        }
     else {
              !exploreGraph
           }.
// If not probed - probe
+ !doProbing(Vertex):
energy(CurrEnergy) & CurrEnergy > 1 & not probed(Vertex,Value)
<-
   .print(Vertex,"is not probed,do probing");
    probe.
    
//if energy is not enough - recharge
+ !doProbing(Vertex):
energy(CurrEnergy) & CurrEnergy < 1
<-
     .print("I have ", CurrEnergy, " energy, but need 1 to probe going to recharge first.");
      recharge.
      
// If probed - do graph exploring      
+ !doProbing(Vertex)
<-
    .print(Vertex,"has been probed,it will do graph exploring!");
    !exploreGraph.

/* Plans */
