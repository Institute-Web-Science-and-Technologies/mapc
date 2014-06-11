// Agent baseAgent in project mako

/* Initial beliefs and rules */

/* Initial goals */
!start.
!recharge.

+myName(MyName)[source(percept)]: true <- .print("My Name is ", MyName).
+health(MyH)[source(percept)]: MyH > 0 <- .print("My Health is ", MyH).

/* Plans */
+!start <- .my_name(AgentName).
+!recharge : true <- bla(hallo).