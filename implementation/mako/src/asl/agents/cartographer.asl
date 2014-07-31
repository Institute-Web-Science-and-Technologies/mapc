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
         -existingNodeAgents(NodeAgent);
         // uncomment the next line if you also want to destroy the respective
         // agents. This is not done in hope for a better performance:
         // .kill_agent(NodeAgent);
       };
       .print("Only ", VerticesAmount, " existing nodes. Removed beliefs. You should only see this message once per simulation.").
       
// Whenever the server tells about a surveyed edge, handle it. If the edge is already known with its weight, ignore it.
// Otherwise remove unsurveyed edge beliefs and add belief as new edge belief with updated costs. Also inform the NodeAgents about it.
+surveyedEdge(VertexA, VertexB, Weight):
	edge(VertexA, VertexB, Weight) & edge(VertexB,VertexA, Weight) & maxEdgeWeight(Max) & Weight \== Max
	<-
	.print("I already knew about the surveyed edge from (", VertexA, ") to (", VertexB, ") with weight ", Weight, ".").

+surveyedEdge(VertexA, VertexB, Weight)
    <- .print("I was informed about an edge from (", VertexA, ") to (", VertexB, ") with weight ", Weight, ".");
       -edge(VertexA, VertexB, _);
       -edge(VertexB, VertexA, _);
       +edge(VertexA, VertexB, Weight);
       +edge(VertexB, VertexA, Weight);
       !informedNodeAgentsAboutEdge(VertexA, VertexB, Weight).
       
// Whenever the server tells about a visible edge, handle it. If the edge is already known, ignore it. 
// Otherwise add the edge with assuming max weight for edge costs. Add both directions of edge traversing and inform the NodeAgents.
+visibleEdge(VertexA, VertexB)[source(percept)]:
	edge(VertexA, VertexB, _)
	<- .print("I already know about this edge").

+visibleEdge(VertexA, VertexB)[source(percept)]:
    maxEdgeWeight(Weight)
    <- .print("I was informed about an edge from (", VertexA, ") to (", VertexB, ") with unknown weight.");
       +edge(VertexA, VertexB, Weight);
       +edge(VertexB, VertexA, Weight);
       !informedNodeAgentsAboutEdge(VertexA, VertexB, Weight).
    
+?unsurveyedNeighbours(Vertex, Result)[source(SenderAgent)]
	<-
       // putting Weight before DestinationVertex allows a natural order in
       // favour of the weights:
       .findall([Weight, Destination], edge(Vertex, Destination, Weight) & vertex(Destination, false), Result);
       .print("Calculated the list of unsurveyed neighbours for vertex ", Vertex, ": ", Result)
       .

+?unsurveyedVertices(Result)
    <- .findall(Vertex, vertex(Vertex, false), Result).

// Before the simulation is started, create as many nodeAgents as maxNodesAmount
// specified:
+!start:
    maxNodesAmount(Amount)
    <- 
    for (.range(ControlVariable, 0, Amount - 1)) {
        .concat("v", ControlVariable, NodeAgent);
        .create_agent(NodeAgent, "src/asl/map/nodeAgent.asl");
        +existingNodeAgents(NodeAgent);
    };
    .print("I created ", Amount, " nodeAgents.").

// Tells the NodeAgents about this edge. Also add the other vertex to the list
// of neighbours:
+!informedNodeAgentsAboutEdge(VertexA, VertexB, Weight)
    <-
    .print("Sending information about edge ", VertexA, " - ", VertexB, " to node agents.");
    .send(VertexA, tell, path(VertexB, Weight));     
    .send(VertexB, tell, path(VertexA, Weight)).
    
//Handle the case where an agent who has surveyed a node lets the cartographer know
//that he did.
+vertex(Vertex, true) <- -+vertex(Vertex, true).

//Generate a vertex belief when informed about a new visible vertex. Assume
//that it has not been surveyed yet.
+visibleVertex(Vertex, _):
	not vertex(Vertex, _)
	<-
	+vertex(Vertex, false).