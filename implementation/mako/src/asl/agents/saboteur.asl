{ include("agent.asl") }
{ include("../actions/parry.asl")}

role(saboteur).

// Saboteur current strategy: attack_chase or zoneDefence
strategy(attack_chase).

// If there is a new zone defence request, but we already defending a zone - skip processing, busy
+requestZoneDefence(ZoneCentre): strategy(zoneDefence).

// If there is a new zone defence request - do bidding for this zone defence
+requestZoneDefence(ZoneCentre):
    position(Position) & saboteurList(SaboteurList) & .my_name(Name)
    & ia.getDistance(Position, ZoneCentre, Distance)
    & Distance >= 0
    <-
    +defendZoneBid(ZoneCentre, Distance, Name);
    .send(SaboteurList, tell, defendZoneBid(ZoneCentre, Distance, Name));
    .wait(400);
    !evaluateBiddingOutcome(ZoneCentre).

// If Distance to ZoneCentre is equal to -1 - return zone unreachable message, stop processing.
+requestZoneDefence(ZoneCentre)
    <-
  	.print("The target zone with centre in ", ZoneCentre," is unreachable - ignoring zone defence request").
 
// Check bids, assign defence strategy if won. 
@evaluateBidding[atomic] 
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

// If there were no bids as they were abolished earlier and this method called
// twice, do nothing because it was already reasoned on the bids.
+!evaluateBiddingOutcome(ZoneCentre).

// If the zone is no longer require defence - return to regular strategy.
+cancelZoneDefence(ZoneCentre)   
    <-
    .abolish(requestZoneDefence(ZoneCentre));
    if(strategy(zoneDefence) & defendingZone(ZoneCentre)){
    	-+strategy(attack_chase);
        -defendingZone(ZoneCentre);
    };
    .abolish(cancelZoneDefence(ZoneCentre)). 

// In defending zone mode saboteur should attack the disturbing enemy once he sees it    
+!defendZone:
	defendingZone(ZoneCentre)  
	& ia.getClosestEnemy(ZoneCentre, EnemyPosition, _)
	& position(Position)
	& (visibleEdge(Position, EnemyPosition) | visibleEdge(EnemyPosition, Position) | EnemyPosition == Position)
	& myTeam(MyTeam)
	& visibleEntity(Vehicle, EnemyPosition, Team, _)
	& MyTeam \== Team
 	<- .print("Attacking ", Vehicle, " disturbing zone on ", EnemyPosition);
 	   !doAttack(Vehicle, EnemyPosition).

// Saboteurs in defending zone mode should go directly to the disturbing enemy
+!defendZone:
	defendingZone(ZoneCentre)  
	& ia.getClosestEnemy(ZoneCentre, EnemyPosition, _)
 	<- .print("Going to the disturbing enemy on vertex ", EnemyPosition);
 	   !goto(EnemyPosition).

// Saboteur can't see the enemy - returning to attack/chase strategy    
+!defendZone:
    defendingZone(ZoneCentre) 
    & saboteurList(SaboteurList)
    <- .print("I can't see the enemy near the protected zone, cancelling zone defence.");
    +cancelZoneDefence(ZoneCentre);
    .send(SaboteurList, tell, cancelZoneDefence(ZoneCentre));
    !!doAction.
    
// Fallback plan - if for some reason saboteur does not know which zone he should defend - return to standard strategy.   
+!defendZone 
    <- -+strategy(attack_chase);
    !!doAction.  
    
+!doAttack(Vehicle, Vertex):
    lastActionResult(failed_in_range)
    & lastAction(attack)
    & not position(Vertex)
    <- .print("I failed to attack ", Vehicle, ". But I will follow it.");
       !goto(Vertex).

// Recharge if we want to attack but don't have the required energy.
// Energy costs increase with distance for ranged actions, so we have to take that
// into account.
+!doAttack(Enemy, EnemyPosition):
    energy(MyEnergy)
    & position(MyPosition)
    & ia.getDistance(MyPosition, EnemyPosition, Distance)
	& MyEnergy < (2 + Distance)
    <- .print("I have ", MyEnergy, " energy, but I need ", 2 + Distance, " energy to attack ", Enemy, ". I will recharge.");
       recharge.

// If an enemy agent is outside of our attacking range, perform a goto instead
// of an attack.
+!doAttack(Enemy, EnemyPosition):
	position(MyPosition)
	& ia.getDistance(MyPosition, EnemyPosition, Distance)
	& visRange(MyRange)
	& Distance > (MyRange / 2)
	<-
	.print("I want to attack ", Enemy, ", but it is ", Distance, " steps away, and my visibility range is only ", MyRange);
	!goto(EnemyPosition).
	
// It is possible that we don't know a path from our position to the
// enemy agent. In this case, ia.getdistance returns the distance 0.
+!doAttack(Vehicle, Vertex):
	position(Position)
	& Position \== Vertex
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
