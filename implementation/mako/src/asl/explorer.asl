{ include("base.agent.asl") }

/* Initial beliefs and rules */

/* Initial goals */
!recharge.

/* Plans */
+!recharge : true <- recharge.
