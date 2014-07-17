// Agent cartographer in project mako
/* Initial beliefs and rules */
maxEdgeCost(11).
/* Initial goals */

!start.

/* Plans */

// Received surveyed edge percept -> delete previous edge beliefs, add new one.
// Atomic to avoid adding surveyed and unsurveyed edges in parallel.
@addEdges[atomic]
+edgePercept(VertexA, VertexB, Weight)[source(PerceptSource)]: 
    maxEdgeCost(N) & Weight < N
    <- -edgePercept(VertexA, VertexB, Weight)[source(PerceptSource)];
       .abolish(edge(VertexA, VertexB, _));
       .abolish(edge(VertexB, VertexA, _));
       +edge(VertexA, VertexB, Weight);
       +edge(VertexB, VertexA, Weight);
       !informedNodeAgentsAboutEdge(VertexA, VertexB, Weight).
       
// Received unsurveyed edge belief, but the edge is already in our beliefs -> delete new unsurveyed edge belief.
+edgePercept(VertexA, VertexB, Weight)[source(PerceptSource)]:
    edge(VertexA, VertexB, _)
    <- -edgePercept(VertexA, VertexB, Weight)[source(PerceptSource)].

// Received unsurveyed edge belief -> add to belief base.
@addUnsurveyedEdges[atomic]
+edgePercept(VertexA, VertexB, Weight)[source(PerceptSource)]
    <- -edgePercept(VertexA, VertexB, Weight)[source(PerceptSource)];
       +edge(VertexA, VertexB, Weight);
       +edge(VertexB, VertexA, Weight);
       !informedNodeAgentsAboutEdge(VertexA, VertexB, Weight).

+position(Vertex)[source(Sender)]:
    Sender \== self
    <- -position(Sender, Vertex)[source(Sender)];
       -position(Sender, _)[source(self)];
       +position(Sender, Vertex)[source(self)];
       +visited(Vertex)[source(self)];
       !addedNodeAgent(Vertex).
    
+probed(Vertex, Value)[source(PerceptSource)]:
    .number(Value) & PerceptSource \== self
    <- -probed(Vertex, Value)[source(PerceptSource)];
       +probed(Vertex, Value)[source(self)];
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
     	// putting Weight before DestinationVertex allows a natural order in
     	// favour of the weights:
        .findall([Weight, DestinationVertex], 
     	   edge(Vertex, DestinationVertex, Weight) & not visited(DestinationVertex), 
     	   UnsurveyedNeighbours);
     }.

/* Additional goals */

// If already surveyed or if there is unsurveyed adjacent edge - do nothing.
+!isVertexSurveyed(Vertex):
    surveyed(Vertex) | (edge(Vertex, _, Weight) & maxEdgeCost(Weight)).

// At least one edge exists - mark as surveyed    
+!isVertexSurveyed(Vertex):
    edge(Vertex, _, _)
    <- 
    +surveyed(Vertex).

// Have a zero condition goal to prevent errors:
+!isVertexSurveyed(Vertex).

+!start : true <- .print("I am the cartographer. How may I help you?").

// Agent with given name existed, nothing to do:
+!addedNodeAgent(Vertex):
    existingNodeAgents(Vertex)
    <- true.

// If an agent with given name does not exist yet, create it:
@addNodeAgentIfNeeded[atomic]
+!addedNodeAgent(Vertex)
    <- .create_agent(Vertex, "src/asl/nodeAgent.asl");
       +existingNodeAgents(Vertex).

// Creates agent nodes if necessary and tells them about this edge.
// Also add the other vertex to the list of neighbours.
+!informedNodeAgentsAboutEdge(VertexA, VertexB, Weight)
    <- !addedNodeAgent(VertexA);
       !addedNodeAgent(VertexB);
       
       .send(VertexA, tell, path(VertexB, Weight));      
       .send(VertexB, tell, path(VertexA, Weight)).


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

	