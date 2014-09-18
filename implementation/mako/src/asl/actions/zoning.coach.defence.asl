/* Plans */

// If the enemy is inside the zone - call saboteur to help if the request was not send earlier.
+!checkZoneUnderAttack:
    isCoach(true)
    & isLocked(false) // locking for cancelling saboteurs is not needed
    & bestZone(_, CentreNode, _)
    & not zoneProtectRequestSent
    & ia.getClosestEnemy(CentreNode, EnemyPosition, _)
    & ia.getDistance(CentreNode, EnemyPosition, Distance)
    & (Distance <= 3) & (Distance >= 0)
    & saboteurList(SaboteurList)
    <- .print("[zoning][coach] Calling for saboetur help");
       .send(SaboteurList, tell, requestZoneDefence(CentreNode));
       +zoneProtectRequestSent. 

// If the enemy left the zone, but we called the saboteur to help - cancel help request.     
+!checkZoneUnderAttack:
    isCoach(true)
    & bestZone(_, CentreNode, _)
    & zoneProtectRequestSent
    & ia.getClosestEnemy(CentreNode, EnemyPosition, _)
    & ia.getDistance(CentreNode, EnemyPosition, Distance)
    & (Distance > 3)
    & saboteurList(SaboteurList)
    <- .print("[zoning][coach] Cancelling saboetur help");
       .send(SaboteurList, tell, cancelZoneDefence(CentreNode));
       .abolish(zoneProtectRequestSent).

// Fallback plan
+!checkZoneUnderAttack.

// If the coach ordered saboteurs, he must cancel it because there isn't any
// zone to defent anymore.
+!informedSaboteursAboutZoneBreakup:
    zoneProtectRequestSent
    & bestZone(_, CentreNode, _)
    & saboteurList(SaboteurList)
    <- .send(SaboteurList, tell, cancelZoneDefence(CentreNode));
       .abolish(zoneProtectRequestSent).

// If no saboteurs are on the way, do nothing.
+!informedSaboteursAboutZoneBreakup.