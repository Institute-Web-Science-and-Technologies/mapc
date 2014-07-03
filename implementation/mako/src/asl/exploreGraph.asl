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
    position(CurrVertex) & .my_name(Name) & energy(E)
    <-
    .send(cartographer, askOne, unvisitedNeighbours(CurrVertex, _)[source(Name)], unvisitedNeighbours(_, Options));
    if(surveyed(CurrVertex)){
        .print("Result of unvisitedNeighbours: ", Options);
        //-+options(Options);
        !findNextVertex(CurrVertex, Options, NextVertex, NextVertexWeight);
        .broadcast(tell, iWantToGoTo(NextVertex, NextVertexWeight, E));
        .wait(300); // Wait for bids from other agents.
        !reconsiderChoice(NextVertex, NextVertexWeight, Options, RevisedNextVertex);
        !goto(RevisedNextVertex);
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
    
//if my chosen NextVertex is in conflict: another agent wants to go there and the cost of going there
//is cheaper for him, then choose another NextVertex
+!reconsiderChoice(MyNextVertex, MyNextVertexWeight, Options, RevisedNextVertex):
	iWantToGoTo(MyNextVertex, NextVertexWeight, _) & MyNextVertexWeight > NextVertexWeight & position(CurrVertex)
	<-
	.print("I am in a conflict and the other agent has lower costs. Need to recalculate next vertex.");
	!recalculateNextVertex(CurrVertex, Options, RevisedNextVertex).

// Conflict. My energy is lower -> recalculate next vertex.	
+!reconsiderChoice(MyNextVertex, _, Options, RevisedNextVertex):
	iWantToGoTo(MyNextVertex, _, E) & energy(MyE) & MyE < E  & position(CurrVertex)
	<-
	.print("I am in a conflict and the other agent has more energy. Need to recalculate next vertex.");
	!recalculateNextVertex(CurrVertex, Options, RevisedNextVertex).
	
// Conflict. My name string is lower -> recalculate next vertex.	
+!reconsiderChoice(MyNextVertex, _, Options, RevisedNextVertex):
	iWantToGoTo(MyNextVertex, _, _)[source(AgentName)] & .my_name(MyName) & MyName < AgentName  & position(CurrVertex)
	<-
	.print("I am in a conflict and the other agent has greater name string. Need to recalculate next vertex.");
	!recalculateNextVertex(CurrVertex, Options, RevisedNextVertex).
	
// Don't have conflicts.
+!reconsiderChoice(MyNextVertex, _, _, RevisedNextVertex)
    <-
    RevisedNextVertex = MyNextVertex.
    
// Try to find unvisited vertex, which is not selected by another agent
+!recalculateNextVertex(CurrVertex, Options, NextVertex):
    .length(Options) > 0
    & .nth(OptionNumber, Options, NextVertexList)
    & .nth(0, NextVertexList, NextVertex)
    & .nth(1, NextVertexList, NextVertexWeight)
    & not iWantToGoTo(NextVertex, _, _)
    <-
    +edge(CurrVertex, NextVertex, NextVertexWeight);
    +edge(NextVertex, CurrVertex, NextVertexWeight).

// If we cannot find unvisited vertices not selected by another agent -> return previous position.     
+!recalculateNextVertex(CurrVertex, Options, NextVertex):
    dfspath(CurrPath) & .length(CurrPath) > 1
    <- 
    .nth(1, CurrPath, NextVertex);
    .print("Can't find unvisited vertices in the neighborhood - returning one step back to ", NextVertex). 

// DFS is completed. 
+!recalculateNextVertex(CurrVertex, Options, NextVertex)
    <-
    .print("I'm done with exploring!").

// Return not visited vertex from the neighborhood.
 +!findNextVertex(CurrVertex, Options, NextVertex, NextVertexWeight):
     .length(Options, NumOptions) & NumOptions > 0
     <-      
     .nth(math.random(NumOptions), Options, NextVertexList);
     .nth(0, NextVertexList, NextVertex);
     .nth(1, NextVertexList, NextVertexWeight);
     .print("Next vertex: ", NextVertex, " having weight: ", NextVertexWeight);
     +edge(CurrVertex, NextVertex, NextVertexWeight);
     +edge(NextVertex, CurrVertex, NextVertexWeight).
 
 // If there are no unvisited vertices in the neighborhood -> return previous position.     
 +!findNextVertex(CurrVertex, Options, NextVertex, NextVertexWeight):
     dfspath(CurrPath) & .length(CurrPath) > 1
     <- 
     .nth(1, CurrPath, NextVertex);
     .print("Can't find unvisited vertices in the neighborhood - returning one step back to ", NextVertex). 
 
 // DFS is completed. 
 +!findNextVertex(CurrVertex, Options, NextVertex, NextVertexWeight	)
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
