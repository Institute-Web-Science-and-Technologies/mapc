{ include("agent.asl") }

+!doProbing:
	position(Position)
	<- .print("Try to probe Vertex (", Position,")");
		.send(cartographer, askOne, probedVertex(Position, _), Reply);
		if (Reply == false) {
       		!executeProbing
       	}.
    
// If not probed - probe
+ !executeProbing:
 energy(Energy) & Energy > 1
 	<- .print("(", Vertex, ") is not probed. Execute probing");
    	probe.
    
//if energy is not enough - recharge
+ !executeProbing:
 energy(Energy) & Energy < 1
	<- .print("I have ", Energy, " energy, but I need 1 energy to probe. Going to recharge first.");
    	recharge;
    	-+isProbing(false).

+!dealWithEnemy <- !avoidEnemy.
