//author: sewell
//author: daudrich
+!doExploring:
	position(Position)
	& role(explorer) 
	& ia.getNextUnprobedVertex(Position, Destination)
	<-
	.print("I am currently on ", Position, ". The nearest unprobed vertex is ", Destination);
	!goto(Destination).

+!doExploring:
	position(Position)
	& ia.getNextUnsurveyedVertex(Position, Destination)
	<-
	.print("I am currently on ", Position, ". The nearest unsurveyed vertex is ", Destination);
	!goto(Destination).