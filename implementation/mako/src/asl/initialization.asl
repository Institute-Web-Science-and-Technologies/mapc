// Agent initialization in project mako

/* Initial goals */

!start.

/* Plans */

+!start
    <- 
    !initExploreGraph;
    !loadBroadcastAgentList.
    
// Store list of all agents except myself for broadcasting.    
+!loadBroadcastAgentList:
    .my_name(Name)
    <-
    AgentList = [
    	explorer1,
    	explorer2,
    	explorer3,
    	explorer4,
    	explorer5,
    	explorer6,
    	repairer1,
    	repairer2,
    	repairer3,
    	repairer4,
    	repairer5,
    	repairer6,
    	sentinel1,
    	sentinel2,
    	sentinel3,
    	sentinel4,
    	sentinel5,
    	sentinel6,
    	inspector1,
    	inspector2,
    	inspector3,
    	inspector4,
    	inspector5,
    	inspector6,
    	saboteur1,
    	saboteur2,
    	saboteur3,
    	saboteur4
    ];
    .delete(Name, AgentList, BroadcastList); 
    +broadcastAgentList(BroadcastList).
