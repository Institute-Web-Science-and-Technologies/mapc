// To avoid an enemy agent, ask the MapAgent for best position.
// TODO: Currently, an agent will cycle between two adjacent vertices when
// avoiding an enemy that does not move.
+!avoidEnemy:
    position(Position) 
    & ia.getVertexToAvoidEnemy(Position, Destination)
    <- 
    !goto(Destination).