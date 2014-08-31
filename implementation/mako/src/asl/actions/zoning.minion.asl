/* Initial beliefs and rules */

/* Plans */

// If our coach cancelled the zone, we go back to start zoning from scratch.
// We also cancel directly leave zoning mode if we triggered this ourselves,
// because we cannot wait for a reply – it might be a matter of life and death.
+!cancelledZoneBuilding[source(Sender)]:
    isMinion(true)
    & (Sender == self
        | bestZone(_, _, _)[source(Sender)]
    )
    <- -+isMinion(false);
       !preparedNewZoningRound.