{ include("agent.asl") }
{ include("../actions/parry.asl")}

role(repairer).

// Initialize the list of agents to repair
repairQueue([]).

// If the repairer is disabled, he cannot repair others => don't process repair request.
+requestRepair(DisabledAgentPosition)[source(Agent)]:
 	 health(Health)
 	 & Health == 0
    <-
    .abolish(requestRepair(_)[source(Agent)]).

// When a repair request is received from the agent - start negotiating about the closest less busy repairer
+requestRepair(DisabledAgentPosition)[source(Agent)]:
    position(Position)
    & ia.getDistance(Position, DisabledAgentPosition, Distance)
    & Distance >= 0
    & repairerList(RepairerList)
    & repairQueue(RepairQueue)
    & .length(RepairQueue, RepairQuequeLength)
    & .my_name(Name)
    <-
    .abolish(requestRepair(_)[source(Agent)]);
    //.print("Got repair request for agent ", Agent, " on position ", Position);
    +repairBid(Agent, Name, Distance, RepairQuequeLength);
    .send(RepairerList, tell, repairBid(Agent, Name, Distance, RepairQuequeLength));
    .wait(400);
    !decideWhoWillRepair(Agent).

// If the path to the agent is unknown - don't negotiate.
+requestRepair(DisabledAgentPosition)[source(Agent)]
    <- .abolish(requestRepair(_)[source(Agent)]).
    
// The less busy or (if equal) the closest or (if equal) with less name repairer will respond to the repair request.
+!decideWhoWillRepair(Agent):
    .my_name(MyName)
    & repairQueue(RepairQueue)
    <-
    .findall([RepairQuequeLength, Distance, Name], repairBid(Agent, Name, Distance, RepairQuequeLength), Bids);
    .min(Bids, WinBid);
    // If won in bidding and don't have this agent in the RepairQueue yet
    if(.nth(2, WinBid, MyName) & not .member(Agent, RepairQueue)){
    	.send(Agent, tell, closestRepairer(MyName));
    	.concat(RepairQueue, [Agent], NewRepairQueue);
    	-+repairQueue(NewRepairQueue);
    	.print("I will repair ", Agent, " when he will be nearby.");
    };
    // Clear the bids
    .abolish(repairBid(Agent, _, _, _)).

// If energy is not enough - recharge 
//Todo: repairer can also repair the agent who is undisabled,and spend 2 energy    
+!doRepair(Vehicle, VehiclePosition):
	energy(Energy)
	& position(MyPosition)
	& ia.getDistance(MyPosition, VehiclePosition, Distance)
	& Energy < (3 + Distance)
	<-
    .print("I have ", Energy, " energy, but I need ", 3 + Distance, " to repair ", Vehicle, ". Will recharge.");
    recharge.
      
// If energy is enough - repair    
+!doRepair(Vehicle, _)
	<-
    .print("Repairing ", Vehicle);
    repair(Vehicle).

// When the agent is repaired - update repairQueue belief.  
+successfullyRepaired[source(Agent)]:
    repairQueue(RepairQueue)
    <-
    .abolish(successfullyRepaired);
    .delete(Agent, RepairQueue, NewRepairQueue);
    -+repairQueue(NewRepairQueue).
    