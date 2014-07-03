// Agent cartographer in project mako
{ include("cartographer.findPath.asl") }
/* Initial beliefs and rules */

/* Initial goals */

!start.

/* Plans */

// Received surveyed edge percept -> delete previous edge beliefs, add new one.
// Atomic to avoid adding surveyed and unsurveyed edges in parallel.
@addEdges[atomic]
+edgePercept(VertexA, VertexB, Weight)[source(PerceptSource)]: 
    Weight < 1000
    <- -edgePercept(VertexA, VertexB, Weight)[source(PerceptSource)];
       .abolish(edge(VertexA, VertexB, _));
       .abolish(edge(VertexB, VertexA, _));
       +edge(VertexA, VertexB, Weight);
       +edge(VertexB, VertexA, Weight).
    
// Received unsurveyed edge belief, but the edge is already in our beliefs -> delete new unsurveyed edge belief.    
+edgePercept(VertexA, VertexB, Weight)[source(PerceptSource)]:
    edge(VertexA, VertexB, _)
    <- -edgePercept(VertexA, VertexB, Weight)[source(PerceptSource)].

// Received unsurveyed edge belief -> add to belief base. 
+edgePercept(VertexA, VertexB, Weight)[source(PerceptSource)]
    <- -edgePercept(VertexA, VertexB, Weight)[source(PerceptSource)];
       +edge(VertexA, VertexB, Weight);
       +edge(VertexB, VertexA, Weight).

+position(Vertex)[source(Sender)]:
    Sender \== self
    <- -position(Sender, Vertex)[source(Sender)];
       -position(Sender, _)[source(self)];
       +position(Sender, Vertex)[source(self)];
       +visited(Vertex)[source(self)];
       if (not knownNodes(Vertex)) {
           .create_agent(Vertex, "src/asl/nodeAgent.asl");
           +knownNodes(Vertex);
       }.
    
+probed(Vertex, Value)[source(PerceptSource)]:
    .number(Value) & PerceptSource \== self
    <- -probed(Vertex, Value)[source(PerceptSource)];
       -+probed(Vertex, Value)[source(self)];
       .print(Vertex, " is worth ", Value).
       
+occupied(Vertex, Team)[source(PerceptSource)]:
    .literal(Team) & PerceptSource \== self
    <- -occupied(Vertex, Team)[source(PerceptSource)];
       -+occupied(Vertex, Team)[source(self)].


+?unvisitedNeighbours(Vertex, UnsurveyedNeighbours)[source(SenderAgent)]
    <-
     !isVertexSurveyed(Vertex); // make sure this information exists for later use from the base agent.
     if(surveyed(Vertex)){
     	.send(SenderAgent, tell, surveyed(Vertex));
        .findall([DestinationVertex, Weight], 
     	   edge(Vertex, DestinationVertex, Weight) & not visited(DestinationVertex), 
     	   UnsurveyedNeighbours);
     }.

// If already surveyed or if there is unsurveyed adjacent edge - do nothing.
+!isVertexSurveyed(Vertex):
    surveyed(Vertex) | (edge(Vertex, _, Weight) & Weight == 1000).

// At least one edge exists - mark as surveyed    
+!isVertexSurveyed(Vertex):
    edge(Vertex, _, _)
    <- 
    +surveyed(Vertex).

// Have a zero condition goal to prevent errors:
+!isVertexSurveyed(Vertex).

+!start : true <- .print("I am the cartographer. How may I help you?").

// Announce step, do some tests
//+!announceStep(Step)[source(Agent)]:
//    Agent == explorer1
//	<- .print("Current step:", Step, " said ", Agent);
//	!testEdgesDuplicate.
	
//+!announceStep(Step)[source(Agent)].

// Test for duplicate edge beliefs
//+!testEdgesDuplicate
//    <-
//    .findall(edge(VertexA, VertexB), edge(VertexA, VertexB, Weight) & Weight < 1000, SurveyedEdges);
//    .findall(edge(VertexA, VertexB), edge(VertexA, VertexB, Weight) & Weight == 1000, UnsurveyedEdges);
//    .intersection(SurveyedEdges, UnsurveyedEdges, DuplicateEdges);
//    .print("Duplicate edges found: ", DuplicateEdges).

	