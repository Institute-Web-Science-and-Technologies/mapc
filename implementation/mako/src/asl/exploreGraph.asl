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
    position(CurrVertex) & .my_name(Name)
    <-
//    .send(cartographer, achieve, calculateDFSOpportunities(CurrVertex)).
    .send(cartographer, askOne, unvisitedNeighbours(CurrVertex, _)[source(Name)], unvisitedNeighbours(_, Options));
    if(surveyed(CurrVertex)){
        .print("Result of unvisitedNeighbours: ", Options);
        !findNextVertex(CurrVertex, Options, NextVertex);
        !goto(NextVertex);
    }
    else{
        .print(CurrVertex, " not surveyed, surveying.");
        !survey;    	
    }.

// Fallback goal.
+!exploreGraph
    <-
    .print("The goal !exploreGraph has failed.");
    fail_goal(exploreGraph).

// Return not visited vertex from the neighborhood.
 +!findNextVertex(CurrVertex, Options, NextVertex):
     .length(Options, NumOptions) & NumOptions > 1
     <-      
     .nth(math.random(NumOptions), Options, NextVertexList);
     .nth(0, NextVertexList, NextVertex);
     .nth(1, NextVertexList, NextVertexWeight);
     .print("Next vertex: ", NextVertex, " having weight: ", NextVertexWeight);
     +edge(CurrVertex, NextVertex, NextVertexWeight);
     +edge(NextVertex, CurrVertex, NextVertexWeight).
 
 // If there are no unvisited vertices in the neighborhood -> return previous position.     
 +!findNextVertex(CurrVertex, Options, NextVertex):
     dfspath(CurrPath) & .length(CurrPath) > 1
     <- 
     .nth(1, CurrPath, NextVertex);
     .print("Can't find unvisited vertices in the neighborhood - returning one step back to ", NextVertex). 
 
 // DFS is completed. 
 +!findNextVertex(CurrVertex, Options, NextVertex)
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
