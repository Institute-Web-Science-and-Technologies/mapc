// Agent cartographershortestPath in project mako


+!searchPath(InitialVertex, GoalVertex, Path): InitialVertex == GoalVertex <-
	Path = [Value|_];
	-+bestPathValue(Value).

// [14,[v123,v124,v125]]

+!findPath(InitialVertex, GoalVertex, Path) <-
	.print("Entering findPath plan! InitialVertex is: ", InitialVertex, ". GoalVertex is: ", GoalVertex);
	-+bestPathValue(10000);
//	.print("Set bestPathValue to 10000");
	Path = [0, []];
	.print("Path is: ", Path);
	!searchPath(InitialVertex, GoalVertex, Path).

+!searchPath(InitialVertex, GoalVertex, Path): InitialVertex \== GoalVertex <-	
	?breadthSearch(InitialVertex, Path, ListOfPaths);
//	.print("?breadthSearch(InitialVertex, Path, ListOfPaths); ", ListOfPaths);
	if (.list(ListOfPaths)) {	
	for (.member([PathValue|PathVertices], ListOfPaths)) {
		if (bestPathValue(V) & V > PathValue) {
		.length(PathVertices, PathLength);
		.nth(PathLength-1, PathVertices, NextVertex);	
		!findPath(NextVertex, GoalVertex, PathVertices);
		}
	}
	}
.
+?breadthSearch(CurrentVertex, PathWithValue, ListOfPaths) <-
	PathWithValue = [Value|Path];
//	ListOfPaths = [];
	.findall([AdjacentVertexA, WeightA], edge(CurrentVertex, AdjacentVertexA, WeightA), VerticesA);	
//	.print("VerticesA: ", VerticesA);
	.findall([AdjacentVertexB, WeightB], edge(AdjacentVertexB, CurrentVertex, WeightB), VerticesB);
//	.print("VerticesB: ", VerticesB);
	.concat(VerticesA, VerticesB, Vertices);
//	.print("ListOfVertices: ", Vertices);
	for (.member([Vertex, Weight], Vertices)) {
		if (not .member(Vertex, Path)) {
			NewListOfPaths = ListOfPaths;
			.print("NewListOfPaths = ListOfPaths;", NewListOfPaths);
			if (.member([], Path)) {
				NewPath = [Vertex];
				.print("Result of NewPath = [Vertex];: ", NewPath)
			}
			else {
				.concat([Vertex], Path, NewPath);
				.print("Result of .concat([Vertex], Path, NewPath): ", NewPath);
			}
			NewValue = Value + Weight;
//			.print("Result of NewValue = Value + Weight: ", NewValue);
			.concat (NewPath, [NewValue], NewPathWithValue);
			.print("Result of .concat (NewPath, [NewValue], NewPathWithValue); ", NewPathWithValue);
			if (.list(NewListOfPaths)) {
				.concat (NewPathWithValue, NewListOfPaths, ListOfPaths);
				.print(".concat (NewPathWithValue, ListOfPaths, ListOfPaths);", ListOfPaths)				
			}
			else {
				ListOfPaths = [NewPathWithValue];
				.print("ListOfPaths = [NewPathWithValue]", ListOfPaths);
			}
		}
		
		}.

// Agent cartographershortestPath in project mako


+!searchPath(InitialVertex, GoalVertex, Path): InitialVertex == GoalVertex <-
	Path = [Value|_];
	-+bestPathValue(Value).

// [14,[v123,v124,v125]]

+!findPath(InitialVertex, GoalVertex, Path) <-
	-+bestPathValue(10000);
	Path = [0, []];
	!searchPath(InitialVertex, GoalVertex, Path).

+!searchPath(InitialVertex, GoalVertex, Path): InitialVertex \== GoalVertex <-	
	?breadthSearch(InitialVertex, Path, ListOfPaths);
	for (.member([PathValue|PathVertices], ListOfPaths)) {
		if (bestPathValue(V) & V > PathValue) {
		.length(PathVertices, PathLength);
		.nth(PathLength-1, PathVertices, NextVertex);	
		!findPath(NextVertex, GoalVertex, PathVertices)
		}
	}.

+?breadthSearch(CurrentVertex, PathWithValue, ListOfPaths) <-
	PathWithValue = [Value|Path];
	.findall([AdjacentVertexA, WeightA], edge(CurrentVertex, AdjacentVertexA, WeightA), VerticesA);	
	.print("VerticesA: ", VerticesA);
	.findall([AdjacentVertexB, WeightB], edge(AdjacentVertexB, CurrentVertex, WeightB), VerticesB);
	.print("VerticesB: ", VerticesB);
	.concat(VerticesA, VerticesB, Vertices);
	.print("ListOfVertices: ", Vertices);
	for (.member([Vertex, Weight], Vertices)) {		
		if (not .member(Vertex, Path)) {
			.concat(Path, Vertex, NewPath);
			NewValue = Value + Weight;
			.concat (NewValue, NewPath, NewPathWithValue);
			.concat (ListOfPaths, NewPathWithValue, ListOfPaths)
		}		
	}.
