!loadBroadcastAgentList.
!loadSaboteurList.
!loadRepairerList.

// Store list of all agents (excluding myself) for broadcasting.    
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
    
+!loadSaboteurList:
    .my_name(Name)
    <-
    AgentList = [
    	saboteur1,
    	saboteur2,
    	saboteur3,
    	saboteur4
    ];
    if(role(saboteur)){
    	.delete(Name, AgentList, SaboteurList); 
    }
    else{
    	SaboteurList = AgentList;
    };
    +saboteurList(SaboteurList).

+!loadRepairerList:
    .my_name(Name)
    <-
    AgentList = [
    	repairer1,
    	repairer2,
    	repairer3,
    	repairer4,
    	repairer5,
    	repairer6
    ];
    if(role(repairer)){
    	.delete(Name, AgentList, RepairerList); 
    }
    else{
    	RepairerList = AgentList;
    };
    +repairerList(RepairerList).
    