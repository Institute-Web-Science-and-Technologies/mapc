// Agent zoning in project mako

/* Initial beliefs and rules */
waitingForZones(true).
// knownZone(ZoneValue, UsesNodes)[source(CenterNode)].

/* Initial goals */

!start.

/* Plans */

// Zoning mode has begun and the agent is now looking for possible zones
// to build around him:
+!doAction:
    zoneMode(true)
    & waitingForZones(true)
    & position(Vertex)
    <- // ask Vertex for its zone as well as its neighbours
    // save it as knownZones([ZoneValue, [NodeAgentX, NodeAgentY]])
       .send(Vertex, askOne, bestZoneValue, currentZone);
       -+waitingForZones(false);
       !findBestZone;
       !communicatedZone.

// Zoning broadcasts may begin as soon as all nodes in the one-hop-
// neighbourhood have replied with their most valuable zone.
+!communicatedZone:
		broadcastAgentList(BroadcastList) 
		& bestZone(BestZone)[source(self)]
    <- 
    	.send(BroadcastList, tell, BestZone).
 //Looks into the agent's bb and determines the best known zone
 //from all received beliefs knownZone
 +!findBestZone:
 		bestZone(OldBestZone)
 		& .length(knownZone(_))>0
 		& .findall(ZoneValue, knownZone(ZoneValue), KnownZones)
    	& .max(KnownZones, NewBestZone)
    	& OldBestZone < NewBestZone
 	<- 
    	-+bestZone(NewBestZone)[source(self)]. //test updates all bestZones??
    	
 +!findBestZone
 	<- -bestZone(_)[source(self)].

+!start : true <- .print("hello world.").
