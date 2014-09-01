{ include("agent.asl") }
{ include("../actions/parry.asl")}

role(repairer).

// Initialize the list of agents to repair
+repairQueue([]).

+requestRepair(DisabledAgentPosition)[source(Agent)]:
    position(Position)
    & ia.getDistance(Position, DisabledAgentPosition, Distance)
    & Distance >= 0
    & repairerList(RepairerList)
    & repairQueue(RepairQueue)
    & .length(RepairQueue, RepairQuequeLength)
    & .my_name(Name)
    <-
    .print("Got repair request for agent ", Agent, " on position ", Position);
    +repairBid(Agent, Name, Distance, RepairQuequeLength);
    .send(RepairerList, tell, repairBid(Agent, Name, Distance, RepairQuequeLength));
    .wait(400);
    !decideWhoWillRepair(Agent).

+!decideWhoWillRepair(Agent):
    .my_name(MyName)
    & repairQueue(RepairQueue)
    <-
    .findall([RepairQuequeLength, Distance, Name], repairBid(Agent, Name, Distance, RepairQuequeLength), Bids);
    .min(Bids, WinBid);
    // If won in bidding and don't have this agent in the RepairQueue yet
    if(.nth(2, WinBid, MyName) & not .sublist([Agent], RepairQueue)){
    	.send(Agent, tell, closestRepairer(MyName));
    	.concat(RepairQueue, [Agent], NewRepairQueue);
    	-+repairQueue(NewRepairQueue);
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