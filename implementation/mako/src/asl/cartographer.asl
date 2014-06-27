// Agent cartographer in project mako
{ include("cartographer.shortestPath.asl") }
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
    PerceptSource \== self & edge(VertexA, VertexB, _)[source(self)]
    <- -edge(VertexA, VertexB, Weight)[source(PerceptSource)].

// Received unsurveyed edge belief -> add to belief base. 
+edge(VertexA, VertexB, Weight)[source(PerceptSource)]:
    PerceptSource \== self 
    <- -edge(VertexA, VertexB, Weight)[source(PerceptSource)];
       -edge(VertexA, VertexB, _)[source(self)];
       -edge(VertexB, VertexA, _)[source(self)];
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
       
// Checking is the vertex is surveyed and are there unvisited edges in the neighborhood.
+!calculateDFSOpportunities(Vertex)[source(SenderAgent)]
    <-
    !isVertexSurveyed(Vertex); // first check if thevertex is surveyed.
    !processOpportunities(Vertex)[source(SenderAgent)];
    .send(SenderAgent, tell, doneCalculatingOpportunities(Vertex)).
//    .print("I'm done calculating opportunities for ", SenderAgent).

// If surveyed - send back suitable edges.  
+!processOpportunities(Vertex)[source(SenderAgent)]:
    surveyed(Vertex)
    <-
    .send(SenderAgent, tell, surveyed(Vertex));
    +processedVertices([])[source(SenderAgent)];
    !sendOpportunities(Vertex, SenderAgent);
    -processedVertices(_)[source(SenderAgent)];
    .send(SenderAgent, tell, doneCalculatingOpportunities(Vertex)).

// Not surveyed - do not return any edges.    
+!processOpportunities(Vertex)[source(SenderAgent)].
    
// Recursively send opportunities.     
+!sendOpportunities(Vertex, SenderAgent):
    edge(Vertex, NextVertex, Weight) & not visited(NextVertex) 
    & processedVertices(ProcessedList)[source(SenderAgent)] & not .member(NextVertex, ProcessedList)
    <-
    .send(SenderAgent, tell, edge(Vertex, NextVertex, Weight));
    .concat([NextVertex], ProcessedList, NewProcessedList);
    -+processedVertices(NewProcessedList)[source(SenderAgent)];
    !sendOpportunities(Vertex, SenderAgent).

// No more opportunities, stop.
+!sendOpportunities(_, _).

// If already surveyed or if there is unsurveyed adjacent edge - do nothing.
+!isVertexSurveyed(Vertex):
    surveyed(Vertex) | (edge(Vertex, _, Weight) & Weight == 1000).

// At least one edge with weight less than 1000 exists - mark as surveyed    
+!isVertexSurveyed(Vertex):
    edge(Vertex, _, Weight) & Weight < 1000
    <- 
    +surveyed(Vertex).

// Have a zero condition goal to prevent errors:
+!isVertexSurveyed(Vertex).

+!start : true <- .print("I am the cartographer. How may I help you?").

//+!announceStep(Step)[source(Agent)]
//	<- .print("Current step:", Step, " said ", Agent).

