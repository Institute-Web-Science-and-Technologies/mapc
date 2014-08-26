{ include("agent.asl") }
{ include("../actions/parry.asl")}
+!doAttack(Vehicle,Vertex):
    lastActionResult(failed_in_range)
    & lastAction(attack)
    <- .print("I failed to attack ", Vehicle, ". But I will follow it.");
       !goto(Vertex).

// If energy is not enough - recharge
+!doAttack(Vehicle, Vertex):
    energy(Energy) & Energy < 2

// If energy is not enough - recharge
+!doAttack(Vehicle, Vertex):
    energy(Energy) & Energy < 2
    <- .print("I have ", Energy, " energy, but I need 2 energy to attack. Going to recharge first.");
       recharge.

// If energy is enough - attack
+!doAttack(Vehicle, Vertex)
    <- .print("Attacking (", Vehicle, ").");
       attack(Vehicle).
