// To avoid an enemy agent, ask the MapAgent for best position.
// TODO: Currently, an agent will cycle between two adjacent vertices when
// avoiding an enemy that does not move.
+!avoidEnemy:
	position(MyPosition)
	& ia.getVertexToAvoidEnemy(MyPosition, Destination)
	& MyPosition == Destination
	& (role(sentinel) | role(saboteur) | role(repairer))
	<-
	.print("I can't escape from the enemy! Will try to parry.");
	!doParry.
	
+!avoidEnemy:
	position(MyPosition)
	& ia.getVertexToAvoidEnemy(MyPosition, Destination)
	& MyPosition == Destination
	& (visibleEdge(MyPosition, EscapeNode) | visibleEdge(EscapeNode, MyPosition))
	<-
	.print("I can't escape from the enemy! I'm probably screwed. Will try to flee to ", EscapeNode);
	!goto(EscapeNode).
	
+!avoidEnemy:
    position(MyPosition) 
    & ia.getVertexToAvoidEnemy(MyPosition, Destination)
    <- 
    .print("Avoiding enemy and moving to safe node ", Destination);
    !goto(Destination).