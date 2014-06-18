// Agent cartographer in project mako

/* Initial beliefs and rules */

/* Initial goals */

!start.

/* Events */

+edge(VertexA, VertexB, Weight)
    <- .print("I was notified about an edge from ", VertexA, " to ", VertexB, " having weight ", Weight).
    
+position(Name, Vertex)
    <- .print("Agent ", Name, " is at ", Vertex).
    
+probed(Vertex, Value)
    <- .print(Vertex, " is worth ", Value).

/* Plans */

+!start : true <- .print("I am the cartographer. How may I help you?").