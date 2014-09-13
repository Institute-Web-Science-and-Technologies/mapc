//author: sewell
//author: daudrich

// Check if the Destination is a neighbouring node. 
is_neighbour(Destination) :- position(Position) 
    & (visibleEdge(Position, Destination) | visibleEdge(Destination, Position)).

// Check if Destination is a neighbouring dead end.
not_neigh_dead_end(Destination) :- not is_neighbour(Destination) |
    (position(Position)
    & (visibleEdge(Position, Destination) | visibleEdge(Destination, Position))
	& (visibleEdge(AnotherVertex, Destination) | visibleEdge(Destination, AnotherVertex))
	& (AnotherVertex \== Position)).

+!doExploring:
	position(Position)
	& role(explorer) 
	& ia.getNextUnprobedVertex(Position, Destination)
	<-
	if(not_neigh_dead_end(Destination))
	{
	    .print("I am currently on ", Position, ". The nearest unprobed vertex is ", Destination);
	    !goto(Destination);		
	}else{
		.print("My next vertex to probe is a neighbouring dead end vertex ", Destination, ", will probe it remotely.");
		!doRangedProbing(Destination);
	}.

+!doExploring:
	position(Position)
	& ia.getNextUnsurveyedVertex(Position, Destination)
	<-
	.print("I am currently on ", Position, ". The nearest unsurveyed vertex is ", Destination);
	!goto(Destination).
	
// If there are no unsurveyed vertices left, we're done with exploring (this step).
+!doExploring <-
	.print("Nothing to explore!");
	+nothingToExplore;
	!doAction.