// Agent baseAgent in project mako

/* Initial beliefs and rules */

/* Initial goals */
!start.
!recharge.
+myPosition(unknown)[source(self)].
+myName(MyName)[source(percept)]: true <- .print("My Name is ", MyName).
+health(MyH)[source(percept)]: MyH > 0 <- .print("My Health is ", MyH).

+position(Vertex)[source(percept)]:
   .my_name(MyName)
    <-
    -+myPosition(CurrentVertex);
    internalActions.updateTeamAgentPosition(MyName, Vertex).

+visibleVertex(Vertex, Team)[source(percept)]:
    true
    <-
    internalActions.addVertex(Vertex, Team).
    
+probedVertex(Vertex, Value)[source(percept)]:
    true
    <-
    internalActions.addVertex(Vertex, Value).
    
/* Plans */
+!start <- .my_name(AgentName).
+!recharge : true <- bla(hallo).