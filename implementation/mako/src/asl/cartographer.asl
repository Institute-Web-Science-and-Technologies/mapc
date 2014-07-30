// Agent cartographer in project mako
/* Initial beliefs and rules */
maxEdgeCost(11).
maxNodesAmount(625).
/* Initial goals */

!start.

/* Plans */
// Remove knowledge about non-existing nodeAgents as quickly as possible:
+vertices(VerticesAmount)[source(percept)]:
    maxNodesAmount(Amount)
    <- for (.range(ControlVariable, VerticesAmount, Amount - 1)) {
         .concat("v", ControlVariable, NodeAgent);
         -existingNodeAgents(NodeAgent);
         // uncomment the next line if you also want to destroy the respective
         // agents. This is not done in hope for a better performance:
         // .kill_agent(NodeAgent);
       };
       .print("Only ", VerticesAmount, " existing nodes. Removed beliefs. You should only see this message once per simulation.").

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
       +visited(Vertex)[source(self)].
    
+probed(Vertex, Value)[source(PerceptSource)]:
    .number(Value) & PerceptSource \== self
    <- -probed(Vertex, Value)[source(PerceptSource)];
       +probed(Vertex, Value)[source(self)];
       .print(Vertex, " is worth ", Value).
       
+occupied(Vertex, Team)[source(PerceptSource)]:
    .literal(Team) & PerceptSource \== self
    <- -occupied(Vertex, Team)[source(PerceptSource)];
       -+occupied(Vertex, Team)[source(self)].

//TODO: do something fancy with it:
+edges(AmountEdges)[source(percept)] <- true.

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

+?unvisitedVertices(UnsurveyedNeighbours)
    <-
    .findall(Vertex, existingNodeAgents(Vertex) & not visited(Vertex), UnsurveyedNeighbours).

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

// Before the simulation is started, create as many nodeAgents as maxNodesAmount
// specified:
+!start:
    maxNodesAmount(Amount)
    <- 
    for (.range(ControlVariable, 0, Amount - 1)) {
    	.concat("v", ControlVariable, NodeAgent);
    	.create_agent(NodeAgent, "src/asl/nodeAgent.asl");
    	+existingNodeAgents(NodeAgent);
    };
    .print("I created ", Amount, " nodeAgents.").

// Creates agent nodes if necessary and tells them about this edge.
// Also add the other vertex to the list of neighbours.
+!informedNodeAgentsAboutEdge(VertexA, VertexB, Weight)
    <- .send(VertexA, tell, path(VertexB, Weight));      
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

	
