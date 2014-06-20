// Agent cartographer in project mako

/* Initial beliefs and rules */

/* Initial goals */

!start.

/* Plans */

// Received surveyed edge percept -> delete previous edge beliefs, add new one
+edge(VertexA, VertexB, Weight)[source(PerceptSource)]: 
    PerceptSource \== self & Weight < 1000
    <- 
    -edge(VertexA, VertexB, Weight)[source(PerceptSource)];
    -edge(VertexA, VertexB, _)[source(self)];
    -edge(VertexB, VertexA, _)[source(self)];   
    +edge(VertexA, VertexB, Weight)[source(self)];
    +edge(VertexB, VertexA, Weight)[source(self)];
    .print("I was notified about surveyed edge from ", VertexA, " to ", VertexB, " having weight ", Weight).

// Received unsurveyed edge belief, but the edge is already in our beliefs -> delete new unsurveyed edge belief.    
+edge(VertexA, VertexB, Weight)[source(PerceptSource)]:
    PerceptSource \== self & edge(VertexA, VertexB, BelievedWeight)[source(self)] 
    <-
    -edge(VertexA, VertexB, Weight)[source(PerceptSource)].

// Received unsurveyed edge belief -> add to belief base. 
+edge(VertexA, VertexB, Weight)[source(PerceptSource)]:
    PerceptSource \== self
    <- 
    -edge(VertexA, VertexB, Weight)[source(PerceptSource)];
    +edge(VertexA, VertexB, Weight)[source(self)];
    +edge(VertexB, VertexA, Weight)[source(self)];
    .print("I was notified about unsurveyed edge from ", VertexA, " to ", VertexB, " having weight ", Weight).

+position(Name, Vertex)[source(PerceptSource)]:
    PerceptSource \== self
    <- 
    -position(Name, Vertex)[source(PerceptSource)];
    -position(Name, _)[source(self)];
    +position(Name, Vertex)[source(self)];
    .print("Agent ", Name, " is at ", Vertex).
    
+probed(Vertex, Value)[source(PerceptSource)]:
    PerceptSource \== self
    <- 
    -probed(Vertex, Value)[source(PerceptSource)];
    +probed(Vertex, Value)[source(self)];
    .print(Vertex, " is worth ", Value).

+!start : true <- .print("I am the cartographer. How may I help you?").

/* Rules */
    
isVertexSurveyed(Vertex) :-
    // find edges connected to this Vertex:
    .findall(Vertex, edge(Vertex, _, Weight), UnifiedEdges)
    // there is at least one edge known:
    & .length(UnifiedEdges, EdgesAmount) & EdgesAmount > 0
    // all edges have a weight lower than 1000:
    & UnifiedEdges < 1000.