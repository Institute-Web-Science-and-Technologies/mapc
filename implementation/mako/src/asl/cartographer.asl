// Agent cartographer in project mako

/* Initial beliefs and rules */

/* Initial goals */

!start.

/* Plans */

// Received surveyed edge percept -> delete previous edge beliefs, add new one
+edge(VertexA, VertexB, Weight)[source(PerceptSource)]: 
    PerceptSource \== self & Weight < 1000
    <- -edge(VertexA, VertexB, Weight)[source(PerceptSource)];
//       .print("Added edge: ", VertexA, " ", VertexB, " ", Weight);   
//       .print("Added edge: ", VertexB, " ", VertexA, " ", Weight);   
       -edge(VertexA, VertexB, _)[source(self)];
       -edge(VertexB, VertexA, _)[source(self)];
       +edge(VertexA, VertexB, Weight)[source(self)];
       +edge(VertexB, VertexA, Weight)[source(self)].
    
// Received unsurveyed edge belief, but the edge is already in our beliefs -> delete new unsurveyed edge belief.    
+edge(VertexA, VertexB, Weight)[source(PerceptSource)]:
    PerceptSource \== self & edge(VertexA, VertexB, BelievedWeight)[source(self)] 
    <- -edge(VertexA, VertexB, Weight)[source(PerceptSource)].

// Received unsurveyed edge belief -> add to belief base. 
+edge(VertexA, VertexB, Weight)[source(PerceptSource)]:
    PerceptSource \== self
    <- -edge(VertexA, VertexB, Weight)[source(PerceptSource)];
       +edge(VertexA, VertexB, Weight)[source(self)];
       +edge(VertexB, VertexA, Weight)[source(self)].

+position(Vertex)[source(Sender)]:
    Sender \== self
    <- -position(Sender, Vertex)[source(Sender)];
       -position(Sender, _)[source(self)];
       +position(Sender, Vertex)[source(self)];
       +visited(Vertex)[source(self)].
    
+probed(Vertex, Value)[source(PerceptSource)]:
    .number(Value) & PerceptSource \== self
    <- -probed(Vertex, Value)[source(PerceptSource)];
       -+probed(Vertex, Value)[source(self)];
       .print(Vertex, " is worth ", Value).
       
+occupied(Vertex, Team)[source(PerceptSource)]:
    .literal(Team) & PerceptSource \== self
    <- -occupied(Vertex, Team)[source(PerceptSource)];
       -+occupied(Vertex, Team)[source(self)].
       
+surveyed[source(Sender)]
    <- position(Sender, Vertex);
       -surveyed(Vertex)[source(_)];
       -+surveyed(Vertex).
    
// Find all adjacent vertices which have not been surveyed and store them as a list e.g. [v123, v482].
+?unsurveyedNeighbours(CurrentVertex, UnsurveyedNeighbours)
    <-
    .findall(DestinationVertex, edge(CurrentVertex, DestinationVertex, _) & not surveyed(DestinationVertex), UnsurveyedNeighbours).

+!start : true <- .print("I am the cartographer. How may I help you?").


