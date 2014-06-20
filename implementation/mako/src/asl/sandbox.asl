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