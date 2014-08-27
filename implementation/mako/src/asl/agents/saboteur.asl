{ include("agent.asl") }
{ include("../actions/parry.asl")}

role(saboteur).

// Saboteur current strategy: attack_chase or zoneDefence
strategy(attack_chase).

// If there is a new zone defense request, but we already defending a zone - skip processing, busy
+requestZoneDefence(ZoneCentre): strategy(zoneDefence).

// If there is a new zone defense request - do bidding for this zone defense
+requestZoneDefence(ZoneCentre):
    position(Position) & saboteurList(SaboteurList) & .my_name(Name)
    <-
    ia.getDistance(Position, ZoneCentre, Distance);
    +defendZoneBid(ZoneCentre, Distance, Name);
    .send(SaboteurList, tell, defendZoneBid(ZoneCentre, Distance, Name));
    .wait(400);
    !evaluateBiddingOutcome(ZoneCentre).
 
// Check bids, assign defense strategy if won.  
+!evaluateBiddingOutcome(ZoneCentre):
    .my_name(MyName)
    <-
    .findall([Distance, Name], defendZoneBid(ZoneCentre, Distance, Name), Bids);
    .min(Bids, WinBid);
    // If won in bidding
    if(.nth(1, WinBid, MyName)){
    	-+strategy(zoneDefence);
    	-+defendingZone(ZoneCentre);
    }
    // Clear the bids
    .abolish(defendZoneBid(ZoneCentre, _, _)).

// If the zone is no longer require defense - return to regular strategy.
+cancelZoneDefense(ZoneCentre)   
    <-
    .abolish(requestZoneDefence(ZoneCentre));
    if(strategy(zoneDefence) & defendingZone(ZoneCentre)){
    	-+strategy(attack_chase);
        -defendingZone(ZoneCentre);
    }. 
    
+!doAttack(Vehicle,Vertex):
    lastActionResult(failed_in_range)
    & lastAction(attack)
    <- .print("I failed to attack ", Vehicle, ". But I will follow it.");
       !goto(Vertex).

// If energy is not enough - recharge
+!doAttack(Vehicle, Vertex):
    energy(Energy) & Energy < 2
    <- .print("I have ", Energy, " energy, but I need 2 energy to attack. Going to recharge first.");
       recharge.

// If energy is enough - attack
+!doAttack(Vehicle, Vertex)
    <- .print("Attacking ", Vehicle);
       attack(Vehicle).
