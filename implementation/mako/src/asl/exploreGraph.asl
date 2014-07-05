// Implementation of Depth First Search for graph exploration.

/* Initial beliefs and rules */

/* Initial goals */

/* Plans */

// Initialization
+!initExploreGraph
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
        .length(Options, NumOptions);
        !findNextVertex(CurrVertex, Options, NextVertex, NextVertexWeight);
        ?broadcastAgentList(BroadcastList);
        .send(BroadcastList, tell, iWantToGoTo(NextVertex, NextVertexWeight, E, NumOptions)); // Broadcast a bid for NextVertex.
        .wait(400); // Wait for bids from other agents.
//        .findall(SourceAgent, iWantToGoTo(_, _, _ ,_)[source(SourceAgent)], ReceivedBids);
//        .print("-------------------------------------------------Received number of bids: ", .length(ReceivedBids));
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
    
// If my chosen NextVertex is in conflict with another agent's NextVertex, and 
// - I have more options;
// - or equal number of options, but the cost of going to NextVertex is more expensive for me;
// - or equal edge cost, but I have less energy;
// - or equal energy, but my name string is lower than his (in terms of string comparison),
// then choose another NextVertex.
+!reconsiderChoice(MyNextVertex, MyNextVertexWeight, Options, RevisedNextVertex):
	iWantToGoTo(MyNextVertex, NextVertexWeight, E, NumOptions)[source(AgentName)] 
	& position(CurrVertex) & energy(MyE) & .my_name(MyName) & .length(Options, MyNumOptions)
	& ( 
		(MyNumOptions > NumOptions) // Have bigger number of options
		| (MyNumOptions == NumOptions & (MyNextVertexWeight > NextVertexWeight // Have bigger edge cost
		| (MyNextVertexWeight == NextVertexWeight & (MyE < E // Have lower energy
		| MyE == E & MyName < AgentName) // Have lower name string
	))))	
	<-
	.print("I am in a conflict in intension to go to ", MyNextVertex, " with the agent ", AgentName, ". Need to recalculate next vertex.");
//	.print("My parameters for comparison were: ", MyNumOptions, " ",MyNextVertexWeight, " ", MyE, " ", MyName);
//	.print("Opponent's' parameters for comparison were: ", NumOptions, " ",NextVertexWeight, " ", E, " ", AgentName);
	!recalculateNextVertex(CurrVertex, Options, MyNextVertex, RevisedNextVertex).

// Debugging bidding outcomes
//+!reconsiderChoice(MyNextVertex, MyNextVertexWeight, Options, RevisedNextVertex):
//	iWantToGoTo(MyNextVertex, NextVertexWeight, E, NumOptions)[source(AgentName)] 
//	& position(CurrVertex) & energy(MyE) & .my_name(MyName) & .length(Options, MyNumOptions)
//	<-
//	.print("I had a conflict in intension to go to ", MyNextVertex, " with the agent ", AgentName, ", but I won the bidding.");	
//	.print("My parameters for comparison were: ", MyNumOptions, " ",MyNextVertexWeight, " ", MyE, " ", MyName);
//	.print("Opponent's' parameters for comparison were: ", NumOptions, " ",NextVertexWeight, " ", E, " ", AgentName);
//	RevisedNextVertex = MyNextVertex.
		
// Don't have conflicts or won in bidding.
+!reconsiderChoice(MyNextVertex, _, _, RevisedNextVertex)
    <-
    RevisedNextVertex = MyNextVertex.
    
// Try to find another unvisited vertex
+!recalculateNextVertex(CurrVertex, Options, NextVertex, RevisedNextVertex):
    .length(Options) > 1 & edge(CurrVertex, NextVertex, NextVertexWeight) 
    <-
    .delete([NextVertex, NextVertexWeight], Options, NewOptions);
    .length(NewOptions, NumOptions);
    .nth(math.random(NumOptions), NewOptions, NextVertexList);
    .nth(0, NextVertexList, RevisedNextVertex);
    .nth(1, NextVertexList, RevisedNextVertexWeight);
    .print("New options were: ", NewOptions, ", I chose ", RevisedNextVertex);
    +edge(CurrVertex, RevisedNextVertex, RevisedNextVertexWeight);
    +edge(RevisedNextVertex, CurrVertex, RevisedNextVertexWeight).

// If we cannot find unvisited vertices not selected by another agent -> return previous position.     
+!recalculateNextVertex(CurrVertex, _, _, RevisedNextVertex):
    dfspath(CurrPath) & .length(CurrPath) > 1
    <- 
    .nth(1, CurrPath, RevisedNextVertex);
    .print("Can't find unvisited vertices in the neighborhood - returning one step back to ", RevisedNextVertex). 

// DFS is completed. 
+!recalculateNextVertex(_, _, _, _)
    <-
    .print("I'm done with exploring!");
    .succeed_goal(exploreGraph).

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
     ?edge(CurrVertex, NextVertex, NextVertexWeight);
     .print("Can't find unvisited vertices in the neighborhood - returning one step back to ", NextVertex). 
 
 // DFS is completed. 
 +!findNextVertex(CurrVertex, Options, NextVertex, NextVertexWeight	)
     <-
     .print("I'm done with exploring!");
     .succeed_goal(exploreGraph).

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
