// Agent baseAgent in project mako
{ include("storeBeliefs.asl") }
{ include("exploreGraph.asl") }
{ include("initialization.asl") }

// The max costs an edge can have is 10. So unknown costs of edges will be one higher.
maxEdgeCost(11).
idle(true).

+simStart <- .print("Simulation started.").

/* TODO */
+edges(AmountEdges)[source(percept)] <- true.
//TODO: send to cartographer

+vertices(AmountVertices)[source(percept)]
    <- true. 
//TODO: send to cartographer

//TODO: visibleEntity, zoneScore

/* Map Related Stuff */
// Whenever an agent gets a new position percept, update the belief base, 
// if the new position belief is not already in belief base. 
// Furthermore tell the cartographer about agents current position. 
+position(Vertex)[source(percept)]
    <- .print("I'm now at position: (", Vertex, ")."); 
       .send(cartographer, tell, position(Vertex));
       -+position(Vertex)[source(self)].

// Whenever an agent get a new probedVertex percept, 
// tell the cartographer about the vertex and the probed value.
+probedVertex(Vertex, Value)[source(percept)]:
    .number(Value)
    <-//.print("I found a new probed vertex (", Vertex, ") with value of ", Value); 
      .send(cartographer, tell, probed(Vertex, Value));
      -+probedVertex(Vertex, Value)[source(self)].
// TODO: do we still want/need to set local knowledge?

// Whenever an agent gets a visibleVertex percept, 
// tell the cartographer about the vertex and the occupying team.
+visibleVertex(Vertex, Team)[source(percept)]:
    .literal(Team)
    <- //.print("I see the vertex (", Vertex, "). It is occupied by team ", Team, ".");
    .send(cartographer, tell, occupied(Vertex, Team)). 
// TODO: merge this with probed (minimum node value is 1)? Maybe also merge it with position

// Whenever an agent gets a new visibleEdge percept, tell the cartographer about adjacent vertices. 
// Assume the traversing costs of the edge are the max edge costs. 
+visibleEdge(VertexA, VertexB)[source(percept)]:
    maxEdgeCost(Costs)
    <- //.print("I see an edge from (", VertexA, ") to (", VertexB, ") with unknown costs."); 
       .send(cartographer, tell, edgePercept(VertexA, VertexB, Costs)).

// Whenever an agent gets a new surveyedEdge percept, 
// tell the cartographer about adjacent vertices and the traversing costs of the edge.
+surveyedEdge(VertexA, VertexB, Costs)[source(percept)]
    <- //.print("I see an edge from (", VertexA, ") to (", VertexB, "). Costs are ", Costs, ".");
       .send(cartographer, tell, edgePercept(VertexA, VertexB, Costs)).

/*Actions*/                              
 +step(Step)[source(self)]:
    position(Position) & lastActionResult(Result) & lastAction(Action) & idle(Idle)
    <- .print("[Step ", Step, "] My position is (", Position, "). My last action was '", Action,"'. Result was ", Result,".");
       .abolish(iWantToGoTo(_, _, _, _)[source(_)]);
       .perceive;
       .wait(200); // wait until all percepts have been added.
        // Continue with DFS:
        if (Idle) {
        	-+idle(false);
        	!exploreGraph;
        	-+idle(true);
        	.print("I'm idle.");
        }.
