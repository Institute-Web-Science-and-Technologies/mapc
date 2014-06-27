// Agent cartographer in project mako
{ include("cartographer.shortestPath.asl") }
/* Initial beliefs and rules */

/* Initial goals */

!start.

/* Plans */

//Received surveyed visibleVertex percept -> check whether the vertex is already surveyed

+visibleVertex(Vertex)
	<-  if(.findall(SurveyedNode, surveyed(SurveyedNode), SurveyedNodes) & .member(Vertex, SurveyedNodes)){
			+surveyed(Vertex);
		} else{
			+unsurveyed(Vertex);
		}.

+surveyed(Vertex)
	<-  -unsurveyed(Vertex).

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
       +probed(Vertex, Value)[source(self)];
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

// If already surveyed or if there is unsurveyed adjacent edge - do nothing.
+!isVertexSurveyed(Vertex):
    surveyed(Vertex) | (edge(Vertex, _, Weight) & Weight == 1000).

// At least one edge with weight less than 1000 exists - mark as surveyed
+!isVertexSurveyed(Vertex):
    edge(Vertex, _, Weight)
    <-
    +surveyed(Vertex).

// Have a zero condition goal to prevent errors:
+!isVertexSurveyed(Vertex).

+!start : true <- .print("I am the cartographer. How may I help you?").
