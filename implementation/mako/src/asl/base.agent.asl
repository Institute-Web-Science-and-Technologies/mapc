// Agent baseAgent in project mako

/* Initial beliefs and rules */

/* Initial goals */
!start.
!recharge.
+myPosition(unknown)[source(self)].
+myName(MyName)[source(percept)]: true <- .print("My Name is ", MyName).
+health(MyH)[source(percept)]: MyH > 0 <- .print("My Health is ", MyH).

+position(CurrentVertex)[source(percept)]:
   .my_name(MyName2)
    <-
    -+myPosition(CurrentVertex);
    internalActions.updateTeamAgentPosition(MyName2, CurrentVertex).

/* Plans */
+!start <- .my_name(AgentName).
+!recharge : true <- bla(hallo).