{ include("agent.asl") }

// Store list of all agents except myself for broadcasting.
!generateExplorerAgentList.
+!generateExplorerAgentList:
    .my_name(Name)
    <-
    AgentList = [
    	explorer1,
    	explorer2,
    	explorer3,
    	explorer4,
    	explorer5,
    	explorer6
    ];
    .delete(Name, AgentList, BroadcastList); 
    +explorerAgentList(BroadcastList).

// If the agent has enough energy than probe. Otherwise recharge.
+!doProbing:
	energy(Energy) & Energy > 1 & position(Position)
	<- .print("Probing vertex ", Position, ".");
		probe.
	
+!doProbing:
 energy(Energy) & Energy < 1
	<- .print("I have not enough energy to probe. I'll recharge first.");
    	recharge.

// Explorer has to flee from enemy agents.
+!dealWithEnemy(Vehicle): 
	ia.isSaboteur(Vehicle)
<- !avoidEnemy.

+!dealWithEnemy(Vehicle)
	<-
	+ignoreEnemy(Vehicle);
	!doAction.