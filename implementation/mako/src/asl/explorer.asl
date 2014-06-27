{ include("base.agent.asl") }

/* Initial beliefs and rules */

/* Initial goals */

/* Plans */

+step(5)[source(self)] <-
	edge(A, B, W);
	.send(cartographer, tell, findPath(position(Vertex), B, Path));	
	.print("Found Path: ", Path). 