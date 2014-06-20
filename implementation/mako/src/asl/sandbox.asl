// sandbox for writing down our ideas

// shortest path map
edge(v123, v234).
edge(v123, v235).
edge(v234, v236).
edge(v234, v235).

getPath(v123,X).

+!getPath(X,Y) 
	<- edge(X1, Y1, V1);
	   edge(X2, Y2, V2);
				  V1 < V2.
+!getPath(X,Y) 
	<- !getPath(X, Z)
	   !getPath(Z, Y).

				  
// wait for next step
nextStep(S).

+step(S) 
	<- !nextStep(S+1).

+!nextStep(S)
	<- .wait("step(", S,")").
	
+!getNextVertex: position(Vertex)[source(percept)] & surveyedEdge(X, Y, Z) & X==Vertex
	<- .findall(Cost,surveyedEdge(Vertex, Y, Cost),List);
	   //.findall(Cost2,surveyedEdge(Y2, Vertex, Cost2), List2);
	   //.concat(List,List2,ConcatedList);
		.min(List, MinValue);
		.print("Minimum Cost: ",MinValue);
		?surveyedEdge(Vertex, NextNode, MinValue);
		//?surveyedEdge(NextNode, Vertex, MinValue);
		.print("Going to ",NextNode," which has cost of ",MinValue);
		goto(NextNode);
		.print("went to node ", NextNode).