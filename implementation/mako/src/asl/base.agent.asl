// Agent baseAgent in project mako

/* Initial beliefs and rules */

/* Initial goals */

!start.
!recharge.

/* Plans */

+!start : true <- .print("hello world.").
+!recharge : true <- recharge.
