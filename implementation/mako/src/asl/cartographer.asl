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
    !isVertexSurveyed(VertexA);
    !isVertexSurveyed(VertexB).
    
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
    +edge(VertexB, VertexA, Weight)[source(self)].

+position(Name, Vertex)[source(PerceptSource)]:
    PerceptSource \== self
    <- 
    -position(Name, Vertex)[source(PerceptSource)];
    -position(Name, _)[source(self)];
    +position(Name, Vertex)[source(self)].
    
+probed(Vertex, Value)[source(PerceptSource)]:
    PerceptSource \== self
    <- 
    -probed(Vertex, Value)[source(PerceptSource)];
    +probed(Vertex, Value)[source(self)];
    .print(Vertex, " is worth ", Value).

+!start : true <- .print("I am the cartographer. How may I help you?").

/* Test goals */
    
+!isVertexSurveyed(Vertex):
    surveyed(Vertex) |
    // find edges connected to this Vertex with weight less than 1000:
    .findall(Weight, edge(Vertex, _, Weight) & Weight < 1000, UnifiedWeights)
    // at least one such edge exists:
    & .length(UnifiedWeights, EdgesAmount) & EdgesAmount > 0
    <-
    +surveyed(Vertex).

// have a zero condition goal to prevent errors:
+!isVertexSurveyed(Vertex).