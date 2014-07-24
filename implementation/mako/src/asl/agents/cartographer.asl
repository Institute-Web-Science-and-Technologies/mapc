// Agent cartographer in project mako
/* Initial beliefs and rules */
maxEdgeWeight(11).
maxNodesAmount(625).

/* Initial goals */
!start.

/* Plans */
// Remove knowledge about non-existing nodeAgents as quickly as possible:
+vertices(VerticesAmount)[source(percept)]:
    maxNodesAmount(Amount)
    <- for (.range(ControlVariable, VerticesAmount, Amount - 1)) {
         .concat("v", ControlVariable, NodeAgent);
         .kill_agent(NodeAgent);
         // uncomment the next line if you also want to destroy the respective
         // agents. This is not done in hope for a better performance:
         // -existingNodeAgents(NodeAgent);
       };
       .print("Only ", VerticesAmount, " existing nodes. Removed beliefs. You should only see this message once per simulation.").

// Whenever an agent gets a new visibleEdge percept, tell the cartographer about adjacent vertices. 
// Assume the traversing costs of the edge are the max edge costs.
+visibleEdge(VertexA, VertexB)[source(percept)]:
	edge(VertexA, VertexB, _)
	<- //.print("I already know about this edge");
		true.

+visibleEdge(VertexA, VertexB)[source(percept)]:
    maxEdgeWeight(Weight)
    <- .print("I was informed about an edge from (", VertexA, ") to (", VertexB, ") with unknown weight.");
       +edge(VertexA, VertexB, Weight);
       +edge(VertexB, VertexA, Weight);
       !informedNodeAgentsAboutEdge(VertexA, VertexB, Weight).
       
// Whenever an agent gets a new surveyedEdge percept, 
// tell the cartographer about adjacent vertices and the traversing costs of the edge.
+surveyedEdge(VertexA, VertexB, Weight)[source(percept)]:
	edge(VertexA, VertexB, Weight)
	<- //.print("I already know about this surveyed edge.");
		true.

+surveyedEdge(VertexA, VertexB, Weight)[source(percept)]
    <- .print("I was informed about an edge from (", VertexA, ") to (", VertexB, "). Weight is ", Weight, ".");
       .abolish(edge(VertexA, VertexB, _));
       .abolish(edge(VertexB, VertexA, _));
       +edge(VertexA, VertexB, Weight);
       +edge(VertexB, VertexA, Weight);
       !informedNodeAgentsAboutEdge(VertexA, VertexB, Weight).

+position(Vertex)[source(Sender)]:
    Sender \== self
    <- -position(Sender, Vertex)[source(Sender)];
       -position(Sender, _)[source(self)];
       +position(Sender, Vertex)[source(self)];
       +visited(Vertex)[source(self)].
    
// If the cartographer already knows about the probed vertex, ignore it. 
+probedVertex(Vertex, Value)[source(Source)]:
	.number(Value) & probedVertex(Vertex, Value)[source(self)]
	<- //.print("I already know about this probed vertex.");
		true.

// If the agent probed the vertex, inform the cartographer about it.
+probedVertex(Vertex, Value)[source(percept)]:
    .number(Value)
    <-.print("I know about a new probed vertex (", Vertex, ") with value of ", Value);
      +probedVertex(Vertex, Value)[source(self)].

// Whenever an agent gets a visibleVertex percept, 
// tell the cartographer about the vertex and the occupying team.       
+visibleVertex(Vertex, Team)[source(percept)]:
	.literal(Team) & visibleVertex(Vertex, Team)[source(self)]
	<- //.print("I already know about this visible vertex.");
		true.       
       
+visibleVertex(Vertex, Team)[source(percept)]:
    .literal(Team)
    <- .print("I was informed of the vertex (", Vertex, "). It is occupied by team ", Team, ".");
    	.abolish(visibleVertex(Vertex, _)[source(self)]);
    	+visibleVertex(Vertex, Team)[source(self)].

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
    surveyed(Vertex) | (edge(Vertex, _, Weight) & maxEdgeWeight(Weight)).

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

// Tells the NodeAgents about this edge. Also add the other vertex to the list
// of neighbours:
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

	