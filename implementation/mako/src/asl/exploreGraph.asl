// Implementation of Depth First Search for graph exploration.

/* Initial beliefs and rules */
isVertexSurveyed(Vertex) :- .send(cartographer, askOne, surveyed(Vertex)).

/* Initial goals */

!start.

/* Plans */

// Initialization
+!start
    <-
    +dfspath([])[source(self)]. // Initial path for DFS (needed for backtracking)

// (DFS) If went ahead -> add current position to the path.
+!exploreGraph:
    dfspath(CurrPath) & position(CurrVertex) & not .member(CurrVertex, CurrPath)
    <-
    .concat([CurrVertex], CurrPath, NewPath);
    -+dfspath(NewPath);
    .print("Went ahead, current path: ", NewPath);
    !exploreGraph.

// (DFS) If went back -> remove last position from the path.
+!exploreGraph:
    position(CurrVertex) & dfspath(CurrPath) & .nth(0, CurrPath, TopVertex) & TopVertex \== CurrVertex
    <-
    CurrPath = [_|PrevPath];
    -+dfspath(PrevPath);
    .print("Went back, current path: ", PrevPath);
    !exploreGraph.

// (DFS) If the vertex is surveyed -> go further.
+!exploreGraph:
    position(CurrVertex) & isVertexSurveyed(CurrVertex)
    <-
    !findNextVertex(CurrVertex, NextVertex); 
    .print("Next vertex: ", NextVertex);
    !goto(NextVertex).

// (DFS) If the vertex is not surveyed -> survey.
+!exploreGraph:
    position(CurrVertex) & not isVertexSurveyed(CurrVertex)
    <-
    .print(CurrVertex, " not surveyed, surveying.");
    !survey.

// Fallback goal.
+!exploreGraph
    <-
    fail_goal(exploreGraph).

// Return not visited vertex from the neighbourhood.
+!findNextVertex(CurrVertex, NextVertex)
    <-
    -unsurveyedNeighbours(CurrVertex, _);
    .send(cartographer, askOne, unsurveyedNeighbours(CurrVertex, _));
    wait(3000);
    // retrieve reply from askOne:
    ?unsurveyedNeighbours(CurrVertex, UnsurveyedNeighbours);
    if (.length(UnsurveyedNeighbours) > 0) { // there exists at least one Vertex
    .print("Landed in if with following vertices: ", UnsurveyedNeighbours);
       .nth(1, UnsurveyedNeighbours, NextVertex);
    } else {
    .print("Reached else branch, because of ", UnsurveyedNeighbours);
       dfspath(CurrPath);
       if (.length(CurrPath) > 1) { // there is a backtrack history
          dfspath(CurrPath) & .length(CurrPath) > 1 
       } else { // DFS completed
          .print("I'm done with exploring!");
          .succeed_goal(exploreGraph)
       }
    }.

// Want to goto, but don't have enough energy -> recharge.
+!goto(NextVertex):
    position(CurrVertex) & energy(CurrEnergy) & edge(CurrVertex, NextVertex, Weight) & CurrEnergy < Weight
    <- .print("I have ", CurrEnergy, " energy, but need ", Weight, " to go, going to recharge first.");
       recharge.

// Otherwise just goto.
+!goto(NextVertex)
    <- goto(NextVertex).

// Want to survey, but don't have energy -> recharge.
+!survey:
    energy(CurrEnergy) & CurrEnergy < 1
    <- .print("I have ", CurrEnergy, " energy, but need 1 to survey going to recharge first.");
       recharge.
 
// Otherwise just survey.
+!survey   
    <- survey;
       // TODO check if it was successful
       .send(cartographer, tell, surveyed).