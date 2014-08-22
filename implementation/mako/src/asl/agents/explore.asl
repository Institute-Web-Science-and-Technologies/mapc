//author: sewell
//author: daudrich
+!doExploring:
	position(Position)
	& role(explorer) 
	& ia.getNextUnprobedVertex(Position, Destination)
	<-
	.print("I am currently on vertex ", Position, ". My next unprobed vertex is: ", Destination);
	!goto(Destination).

+!doExploring:
	position(Position)
	& ia.getNextUnsurveyedVertex(Position, Destination)
	<-
	.print("I am currently on vertex ", Position, ". My next unsurveyed vertex is: ", Destination);
	!goto(Destination).