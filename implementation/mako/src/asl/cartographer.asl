// Agent cartographer in project mako

/* Initial beliefs and rules */

/* Initial goals */

!start.

/* Plans */

+edge(VertexA, VertexB, Weight)
    <- .print("I was notified about an edge from ", VertexA, " to ", VertexB, " having weight ", Weight).
    
+position(Name, Vertex)
    <- .print("Agent ", Name, " is at ", Vertex).
    
+probed(Vertex, Value)
    <- .print(Vertex, " is worth ", Value).


+!start : true <- .print("I am the cartographer. How may I help you?").

/* Rules */
    
isVertexSurveyed(Vertex) :-
    // find edges connected to this Vertex:
    .findall(Vertex, edge(Vertex, _, Weight), UnifiedEdges)
    // there is at least one edge known:
    & .length(UnifiedEdges, EdgesAmount) & EdgesAmount > 0
    // all edges have a weight lower than 1000:
    & UnifiedEdges < 1000.