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
	.delete([MyNextVertexWeight, MyNextVertex], Options, NewOptions);
	!findNextVertex(CurrVertex, NewOptions, RevisedNextVertex, _).

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

// Return not visited vertex from the neighborhood.
 +!findNextVertex(CurrVertex, Options, NextVertex, NextVertexWeight):
     not .length(Options, 0)
     <-
     .min(Options, CheapestOption);
     .nth(0, CheapestOption, NextVertexWeight);
     .nth(1, CheapestOption, NextVertex);
     .print("Next vertex: ", NextVertex, " having weight: ", NextVertexWeight);
     +edge(CurrVertex, NextVertex, NextVertexWeight);
     +edge(NextVertex, CurrVertex, NextVertexWeight).
 
 // If there are no unvisited vertices in the neighborhood -> return previous position.     
 +!findNextVertex(CurrVertex, _, NextVertex, NextVertexWeight):
     dfspath(CurrPath) & .length(CurrPath) > 1
     <- 
     .nth(1, CurrPath, NextVertex);
     ?edge(CurrVertex, NextVertex, NextVertexWeight);
     .print("Can't find unvisited vertices in the neighborhood - returning one step back to ", NextVertex). 
 
 // DFS is completed. 
 +!findNextVertex(CurrVertex, _, NextVertex, NextVertexWeight)
     <-
     .print("I'm done with DFS, heading to some unvisited vertex.");
     !findNearestUnvisitedVertex(CurrVertex, NextVertex);
     -+dfspath([])[source(self)];
     .print("I chose to go to ", NextVertex, " as my next hop.").
     
+!findNearestUnvisitedVertex(CurrVertex, NextVertex)
    <-
    .send(cartographer, askOne, unvisitedVertices(_), unvisitedVertices(UnvisitedVertexList));
    //.print("Unvisited vertices(", .length(UnvisitedVertexList),"): ", UnvisitedVertexList);
    if(not .length(UnvisitedVertexList, 0)){
    	//.print("I want to send message to ", CurrVertex);
    	.send(CurrVertex, askOne, getClosestVetexFromList(UnvisitedVertexList, _), getClosestVetexFromList(_, NextVertex));
    	.send(cartographer, askOne, edge(CurrVertex, NextVertex, _), edge(_, _, Weight));
    	-+edge(CurrVertex, NextVertex, Weight);  	
    }
    else{
    	.print("There are no unvisited vertices in the graph. Stopping.");
    	.succeed_goal(exploreGraph);
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
    <- survey.
       // TODO check if it was successful
