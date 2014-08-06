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
       
+surveyedEdge(VertexA, VertexB, Weight)
    <-
    .print("Received percept surveyedEdge(", VertexA, ",", VertexB, ",", Weight, ").");
   	.abolish(edge(VertexA, VertexB, _));
   	.abolish(edge(VertexB, VertexA, _));
   	+edge(VertexA, VertexB, Weight);
   	+edge(VertexB, VertexA, Weight);
   	!informedNodeAgentsAboutEdge(VertexA, VertexB, Weight).
       
+visibleEdge(VertexA, VertexB)[source(percept)]:
    maxEdgeWeight(Weight)
    <-
	.print("Received percept visibleEdge(", VertexA, ",", VertexB, ").");
   	+edge(VertexA, VertexB, Weight);
   	+edge(VertexB, VertexA, Weight);
   	!informedNodeAgentsAboutEdge(VertexA, VertexB, Weight).
    
+?unsurveyedNeighbours(Vertex, Result)
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
+vertex(Vertex, true)[source(Sender)]
	<-
	.print("I learned that ", Vertex, " has been surveyed by ", Sender, ".");
	-vertex(Vertex, false).

//Generate a vertex belief when informed about a new visible vertex. Assume
//that it has not been surveyed yet.
+visibleVertex(Vertex, Team)[source(Sender)]:
	not vertex(Vertex, _)
	<-
	.print("Received percept visibleVertex(", Vertex, ",", Team, ").");
	+vertex(Vertex, false).