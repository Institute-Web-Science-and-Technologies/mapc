// Plans for leaving zone mode that will ensure that existing zones are
// properly destroyed.

/* Plans */

// Unsetting zoneMode should unregister zoning.
-zoneMode(_)
    <- ia.unregisterFromZoning.

// No more preparations are needed for agents out of zone mode wanting to get
// repaired.
+!quitZoneMode:
    zoneMode(false).

// Unsetting zoneMode ensures that cancelledZoneBuilding cleans up the beliefs
// but does not launch another round of zoning.
// Unsetting the locked status is necessary so that cancelling the zone as a
// coach is possible.
+!quitZoneMode:
    isCoach(true)
    <- -+zoneMode(false);
       -+isLocked(false);
       !cancelledZoneBuilding;
       .print("[zoning][coach] I am quitting zoning mode and hence destroying my zone.").

// Unsetting zoneMode ensures that cancelledZoneBuilding cleans up the beliefs
// but does not launch another round of zoning.
+!quitZoneMode:
    isMinion(true)
    & bestZone(_, _, _)[source(Coach)]
    <- -+zoneMode(false);
       .send(Coach, tell, cancelledZoneBuilding);
       !cancelledZoneBuilding;
       .print("[zoning][minion] I am quitting zoning mode and hence informing my ", Coach, " to break up his zone.").

// Unsetting zoneMode ensures that cancelledZoneBuilding cleans up the beliefs
// but does not launch another round of zoning.
// Unsetting the zoneGoalVertex cancels the 1-agent-zone automagically.
+!quitZoneMode:
    zoneGoalVertex(_)
    <- -+zoneMode(false);
       -zoneGoalVertex(_);
       .print("[zoning] I am quitting zoning mode and destroying my 1-agent-zone.").

// If still in zone building mode (neither minion, coach, nor single zoner),
// simply unregister as a zoner and start getting repaired.
+!quitZoneMode
    <- -+zoneMode(false);
       .print("[zoning] I am quitting zoning mode.").