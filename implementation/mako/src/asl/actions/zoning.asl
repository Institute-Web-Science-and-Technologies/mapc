// Agent zoning in project mako

/* Initial beliefs and rules */
mustCommunicateZones(true).
// This is the scheme how we could internally store known zones.
// TODO: If we could guarantee that .nth(0, UsesNodes, Node) == CenterNode
// then we could leave out CenterNode.
// knownZone(ZoneValue, UsesNodes, CenterNode).

/* Initial goals */

!start.

/* Plans */

// Zoning mode has begun and the agent is now looking for possible zones
// to build around him.
// It orders to calculate the best zone and starts the communication process.
+!doAction:
    zoneMode(true)
    & mustCommunicateZones(true)
    & position(Vertex)
    <- -+mustCommunicateZones(false);
       // ask Vertex for its zone as well as its neighbours
       // this will probably be done through one iA-call:
       .send(Vertex, askOne, bestZoneValue, CurrentZone);
       // TODO: next up, the reply must be handled. It is needed so
       // that we wait until all zones have been set.
       !findBestZone.

// Zoning broadcasts may begin if we have found ourselves the bestZone:
+bestZone(ZoneValue, UsedNodes, CenterNode)[source(self)]:
		broadcastAgentList(BroadcastList)
		& bestZone(ZoneValue, UsedNodes, CenterNode)
    <- .send(BroadcastList, tell, bestZone(ZoneValue, UsedNodes, CenterNode)).

// Got the bestZone from s.o. else and it is worse than mine, so I send back
// mine:
+bestZone(ZoneValue, UsedNodes, CenterNode)[source(Coach)]:
        Coach \== self
        <- true.
        
// Got the bestZone from s.o. else and it is better than mine, so I reply
// with my distance to CenterNode:
+bestZone(ZoneValue, UsedNodes, CenterNode)[source(Coach)]:
        Coach \== self
        <- true.

//Looks into the agent's bb and determines the best known zone
//from all received beliefs knownZone
+!findBestZone:
 		// there are zones to choose from:
 		.length(knownZone(_)) > 0
 		// get all beliefs as a list. TODO: there has to be an easier way!
 		& (.findall([ZoneValue, UsedNodes, CenterNode], knownZone(ZoneValue, UsedNodes, CenterNode), KnownZones)
    	  & .max(KnownZones, NewBestZone)
    	  // compare our current maximum with the previously set (if any).
    	  // N.b.: bestZone should only be already set if some other agent
    	  // told this agent about a better one before:
          & (bestZone(OldBestZone) & OldBestZone < NewBestZone)
          // also trigger this goal if there wasn't any bestZone yet:
    	  | not bestZone(_))
 	<- .nth(0, NewBestZone, BestZoneValue);
 	   .nth(1, NewBestZone, BestZoneUsedNodes);
 	   .nth(2, NewBestZone, BestZoneCenterNode);
 	   -+bestZone(BestZoneValue, BestZoneUsedNodes, BestZoneCenterNode)[source(self)]. // TODO: test updates all bestZones??

// If there was no more knownZone left remove the bestZone or
// if there was a better bestZone set (from another agent),
// do nothing (doesn't remove bestZone(_)[source(_)]).
//
// TODO: switch this agent's behaviour to someone who is interested
// in helping others building or extending zones.
+!findBestZone
 	<- -bestZone(_)[source(self)].
