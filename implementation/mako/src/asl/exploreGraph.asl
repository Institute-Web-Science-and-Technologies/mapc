// Implementation of Depth First Search for graph exploration.

/* Initial goals */

!start.

/* Plans */

// Initialization
+!start
    <-
    +dfspath([])[source(self)]. // Initial path for DFS

// If Vertex is not visited -> broadcast visited.
+!setVisited(Vertex):
    not visited(Vertex)
    <- 
    +visited(Vertex);
    .broadcast(tell, visited(Vertex)).

//Otherwise do nothing.    
+!setVisited(Vertex).

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
    position(CurrVertex) & surveyed(CurrVertex)
    <-
    !findNextVertex(CurrVertex, NextVertex); 
    .print("Next vertex: ", NextVertex);
    !goto(NextVertex).

// (DFS) If the vertex is not surveyed -> survey.
+!exploreGraph:
    position(CurrVertex) & not surveyed(CurrVertex)
    <-
    .print(CurrVertex, " not surveyed, surveying.");
    !survey.    

// Return not visited vertex from the neighborhood.
+!findNextVertex(CurrVertex, NextVertex):
    edge(CurrVertex, NextVertex, _) & not visited(NextVertex)
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
    .print("I'm done with exploring!");
    .fail_goal(exploreGraph).

// Want to goto, but don't have enough energy -> recharge.
+!goto(NextVertex):
    position(CurrVertex) & energy (CurrEnergy) & edge(CurrVertex, NextVertex, Weight) & CurrEnergy < Weight
    <- 
    .print("I have ", CurrEnergy, " energy, but need ", Weight, " to go, going to recharge first.");
    recharge.

// Otherwise just goto.
+!goto(NextVertex)
    <-
    goto(NextVertex).

// Want to survey, but don't have energy -> recharge.
+!survey:
    energy(CurrEnergy) & CurrEnergy < 1
    <-
    .print("I have ", CurrEnergy, " energy, but need 1 to survey going to recharge first.");
    recharge.
 
 // Otherwise just survey.
 +!survey   
    <-
    survey.
    
// If already surveyed or if there is unsurveyed adjacent edge - do nothing.
+!isVertexSurveyed(Vertex):
    surveyed(Vertex) | (edge(Vertex, _, Weight) & Weight == 1000).

// At least one edge with weight less than 1000 exists - mark as surveyed    
+!isVertexSurveyed(Vertex):
    edge(Vertex, _, Weight) & Weight < 1000
    <- 
    +surveyed(Vertex);
    .broadcast(tell, surveyed(Vertex)).

// Have a zero condition goal to prevent errors:
+!isVertexSurveyed(Vertex).

