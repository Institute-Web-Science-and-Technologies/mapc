// To avoid an enemy agent, ask the MapAgent for best position.
// TODO: Currently, an agent will cycle between two adjacent vertices when
// avoiding an enemy that does not move.
+!avoidEnemy(Enemy):
	position(MyPosition)
	& ia.getVertexToAvoidEnemy(MyPosition, Destination)
	& MyPosition == Destination
	& (role(sentinel) | role(saboteur) | role(repairer))
	& lastAction(parry)
	& lastActionResult(useless)
	<-
	.print("I can't escape from ", Enemy, "! But the last time I parried, I didn't get attacked. So I'll ignore ", Enemy, " this step.");
	+ignoreEnemy(Enemy);
	!doAction.

+!avoidEnemy(Enemy):
	position(MyPosition)
	& ia.getVertexToAvoidEnemy(MyPosition, Destination)
	& MyPosition == Destination
	& (role(sentinel) | role(saboteur) | role(repairer))
	<-
	.print("I can't escape from ", Enemy, "! Will try to parry.");
	!doParry.
	
+!avoidEnemy(Enemy):
	position(MyPosition)
	& ia.getVertexToAvoidEnemy(MyPosition, Destination)
	& MyPosition == Destination
	& (visibleEdge(MyPosition, EscapeNode) | visibleEdge(EscapeNode, MyPosition))
	<-
	.print("I can't escape from ", Enemy, "! I'm probably screwed. Will try to flee to ", EscapeNode);
	!goto(EscapeNode).
	
+!avoidEnemy(Enemy):
    position(MyPosition) 
    & ia.getVertexToAvoidEnemy(MyPosition, Destination)
    <- 
    .print("Avoiding ", Enemy, " and moving to safe node ", Destination);
    !goto(Destination).
