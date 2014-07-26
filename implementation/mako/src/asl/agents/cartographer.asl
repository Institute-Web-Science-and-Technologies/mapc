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
       
// Whenever the server tells about a surveyed edge, handle it. If the edge is already known with its weight, ignore it.
// Otherwise remove unsurveyed edge beliefs and add belief as new edge belief with updated costs. Also inform the NodeAgents about it.
+surveyedEdge(VertexA, VertexB, Weight)[source(percept)]:
	edge(VertexA, VertexB, Weight) & maxEdgeWeight(Max) & Weight \== Max
	<- //.print("I already know about this surveyed edge.");
		true.

+surveyedEdge(VertexA, VertexB, Weight)[source(percept)]
    <- .print("I was informed about an edge from (", VertexA, ") to (", VertexB, "). Weight is ", Weight, ".");
       -edge(VertexA, VertexB, _);
       -edge(VertexB, VertexA, _);
       +edge(VertexA, VertexB, Weight);
       +edge(VertexB, VertexA, Weight);
       !informedNodeAgentsAboutEdge(VertexA, VertexB, Weight).
       
// Whenever the server tells about a visible edge, handle it. If the edge is already known, ignore it. 
// Otherwise add the edge with assuming max weight for edge costs. Add both directions of edge traversing and inform the NodeAgents.
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
    
// Whenever the server tells about a probed vertex, handle it. If the vertex is already known with its value, ignore it.
// Otherwise update the belief of the vertex.
+probedVertex(Vertex, Value)[source(percept)]:
	vertex(Vertex, _, Value, _) & Value \== 0
	<- //.print("I already know about this probed vertex.");
		true.

+probedVertex(Vertex, Value)[source(percept)]:
	vertex(Vertex, IsVisited, 0, Team)
	<- .print("I was informed about a probed vertex ", Vertex, " with value of ", Value);
		-vertex(Vertex, IsVisited, 0, Team);
		+vertex(Vertex, IsVisited, Value, Team).
	
+probedVertex(Vertex, Value)[source(percept)]
    <-.print("I was informed about a new probed vertex ", Vertex, " with value of ", Value);
      +vertex(Vertex, false, Value, unknown).

// Whenever an Agent tells the cartographer about its position, handle it. If the position of this agent is already known, ignore it.
// Otherwise update the position belief of this agent, and change the state of the vertex to visited. 
+position(Vertex)[source(Sender)]:
	position(Sender, Vertex)
	<- .print("I already know that Agent ", Sender, " is on Vertex ", Vertex);
		true. 
	
+position(Vertex)[source(Sender)]:
	vertex(Vertex, true, _, _)
    <- .print("I was informed that Agent ", Sender, " is now on Vertex ", Vertex);
    	-position(Sender, _);
    	+position(Sender, Vertex).
       	
+position(Vertex)[source(Sender)]:
	vertex(Vertex, false, Value, Team)
    <- .print("I was informed that Agent ", Sender, " is now on Vertex ", Vertex ,". This Vertex was not visited before, but was already probed.");
    	-position(Sender, _);
    	+position(Sender, Vertex);
    	-vertex(Vertex, false, Value, Team);
       	+vertex(Vertex, true, Value, Team).
       	
+position(Vertex)[source(Sender)]
    <- .print("I was informed that Agent ", Sender, " is now on Vertex ", Vertex, ". This Vertex was not visited before.");
    	-position(Sender, _);
    	+position(Sender, Vertex);
       	+vertex(Vertex, true, 0, unknown).
       	
// Whenever the server tells about a visible vertex, handle it. If the vertex is already known with its occupying team, ignore it.
// Otherwise if the team changes, update the vertex belief. If the vertex belief is not in the belief base, add it.      
+visibleVertex(Vertex, Team)[source(percept)]:
	vertex(Vertex, _, _, Team)
	<- //.print("I already know about this visible vertex.");
		true.       
       
+visibleVertex(Vertex, Team)[source(percept)]:
	vertex(Vertex, IsVisited, Value, OldTeam) & OldTeam \== Team
	<- .print("I was informed that now Team ", Team, " occupies the Vertex");
		-vertex(Vertex, _, _, OldTeam);
		+vertex(Vertex, IsVisited, Value, Team).
       
+visibleVertex(Vertex, Team)[source(percept)]
	<- .print("I was informed about a new visible Vertex ", Vertex, " and it is occupied by Team ", Team);
		+vertex(Vertex, false, 0, Team).

+?unvisitedNeighbours(Vertex, Result)[source(SenderAgent)]:
	edge(Vertex, _, Weight) & maxEdgeWeight(Max) & Weight \== Max
    <- .send(SenderAgent, tell, surveyed(Vertex));
       // putting Weight before DestinationVertex allows a natural order in
       // favour of the weights:
       .findall([Weight, Destination], edge(Vertex, Destination, Weight) & vertex(Destination, false, _, _), Result).

+?unvisitedVertices(Result)
    <- .findall(Vertex, vertex(Vertex, false, _, _), Result).

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

	
