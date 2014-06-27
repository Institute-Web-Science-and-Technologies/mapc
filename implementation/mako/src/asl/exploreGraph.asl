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

// Ask cartographer about opportunities
+!exploreGraph:
    position(CurrVertex)
    <-
    .send(cartographer, achieve, calculateDFSOpportunities(CurrVertex)).

// (DFS) If the vertex is not surveyed -> survey.    
+doneCalculatingOpportunities(Vertex)[source(SourceAgent)]:
    not surveyed(Vertex)
    <-
    .print(Vertex, " not surveyed, surveying.");
    !survey;
    -doneCalculatingOpportunities(Vertex)[source(SourceAgent)].

// (DFS) If the vertex is surveyed -> go further.
+doneCalculatingOpportunities(Vertex)[source(SourceAgent)]
    <-
    !findNextVertex(CurrVertex, NextVertex); 
    .print("Next vertex: ", NextVertex);
    !goto(NextVertex);
    .abolish(edge(Vertex, _, _));
    //-surveyed(Vertex);
    -doneCalculatingOpportunities(Vertex)[source(SourceAgent)].    

// Fallback goal.
+!exploreGraph
    <-
    fail_goal(exploreGraph).

// Return not visited vertex from the neighborhood.
 +!findNextVertex(CurrVertex, NextVertex):
     edge(CurrVertex, NextVertex, _)
     <-
     true.
 
 // If there are no unvisited vertices in the neighborhood -> return previous position.     
 +!findNextVertex(CurrVertex, NextVertex):
     dfspath(CurrPath) & .length(CurrPath) > 1
     <- 
     .nth(1, CurrPath, NextVertex);
     .print("Can't find unvisited vertices in the neighborhood - returning one step back."). 
 
 // DFS is completed. 
 +!findNextVertex(CurrVertex, NextVertex)
     <-
     .print("I'm done with exploring!").

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
    <- survey.
       // TODO check if it was successful
