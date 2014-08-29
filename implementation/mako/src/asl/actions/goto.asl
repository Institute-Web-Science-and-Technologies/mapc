// In the case where we for some reason get told to move to the node we're already on,
// we perform a recharge action instead.
+!goto(Destination):
    position(Position)
    & Destination == Position
    <-
    .print("Warning! I was told to move to the node I am already on (", Position, "). Will recharge instead.");
    recharge.
    
// Want to goto, but don't have enough energy? Recharge.
+!goto(Destination):
    position(Position)
    & ia.getBestHopToVertex(Position, Destination, NextHop)
    & ia.getEdgeCost(Position, NextHop, Costs)
    & energy(Energy)
    & Costs > Energy
    <-
    .print("I have ", Energy, " energy, but need ", Costs, " to move to ", Destination, " by way of ", NextHop, ", going to recharge first.");
    recharge.

// This is the default goto action if we want to move to one of our neighbour nodes.
+!goto(Destination):
    position(Position)
    & ia.getBestHopToVertex(Position, Destination, NextHop)
    & ia.getEdgeCost(Position, Destination, Costs)
    <-
    .print("I will use ", Costs, " energy to move to ", Destination, " by way of ", NextHop);
    goto(NextHop).