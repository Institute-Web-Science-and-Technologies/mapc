// Agent zoning in project mako

/* Initial beliefs and rules */
lookingForZones(true).

/* Initial goals */

!start.

/* Plans */

// Zoning mode has begun and the agent is now looking for possible zones
// to build around him:
+!doAction:
    zoneMode(true)
    & lookingForZones(true)
    & position(Vertex)
    <- // ask Vertex for its zone as well as its neighbours
    // save it as knownZones([ZoneValue, [NodeAgentX, NodeAgentY]])
       .send(Vertex, askOne, bestZoneValue, currentZone)
       true.

// Zoning broadcasts may begin as soon as all nodes in the one-hop-
// neighbourhood have replied with their most valuable zone.
+!doAction:
    zoneMode(true)
    & lookingForZones(true)
    & .length(knownZones) == oneHopNeighbourhoodSize
    <- -+lookingForZones(false);
       true.

+!start : true <- .print("hello world.").
