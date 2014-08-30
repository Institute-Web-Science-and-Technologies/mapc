{ include("agent.asl") }
{ include("../actions/parry.asl")}

role(saboteur).

// Saboteur current strategy: attack_chase or zoneDefence
strategy(attack_chase).

!start.

// Saboteurs will be busy attacking and harassing enemies and are hence never
// interest in zoning.
+!start <- -+isInterestedInZoning(false).

// If there is a new zone defence request, but we already defending a zone - skip processing, busy
+requestZoneDefence(ZoneCentre): strategy(zoneDefence).

// If there is a new zone defence request - do bidding for this zone defence
+requestZoneDefence(ZoneCentre):
    position(Position) & saboteurList(SaboteurList) & .my_name(Name)
    <-
    ia.getDistance(Position, ZoneCentre, Distance);
    +defendZoneBid(ZoneCentre, Distance, Name);
    .send(SaboteurList, tell, defendZoneBid(ZoneCentre, Distance, Name));
    .wait(400);
    !evaluateBiddingOutcome(ZoneCentre).
 
// Check bids, assign defence strategy if won.  
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

// If the zone is no longer require defence - return to regular strategy.
+cancelZoneDefence(ZoneCentre)   
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

// If an enemy agent is outside of our attacking range, perform a goto instead
// of an attack.
// TODO: In the case where we spend points to buy upgrades for saboteurs,
// their visibility range and thus their attacking range will increase, which
// we will then have to take into account.
+!doAttack(Vehicle, Vertex):
	position(Position)
	& ia.getDistance(Position, Vertex, Distance)
	& Distance > 1
	<-
	.print("I want to attack ", Vehicle, ", but it is ", Distance, " steps away.");
	!goto(Vertex).
	
// It is possible that we don't know a path from our position to the
// enemy agent. In this case, ia.getdistance returns the distance 0.
+!doAttack(Vehicle, Vertex):
	position(Position)
	& ia.getDistance(Position, Vertex, Distance)
	& Distance == -1
	<-
	.print("I wanted to attack ", Vehicle, ", but there is no known path to ", Vertex, " from ", Position, ".");
	+ignoreEnemy(Vehicle);
	!doAction.
	
	
// If energy is enough - attack
+!doAttack(Vehicle, Vertex)
    <- .print("Attacking ", Vehicle);
       attack(Vehicle).
