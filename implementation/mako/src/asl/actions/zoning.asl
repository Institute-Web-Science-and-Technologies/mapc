/* Initial beliefs and rules */
mustCommunicateZones(true).
// @all: If you are looking for something to do,
//       search this file for the keyword "TODO".

// TODO: replace the placeholder names for internal actions with their actual names.
// TODO: split this file into several files (e.g. zone_negotiation and zone_forming).

/* Initial goals */

!start.

/* Plans */

// Zoning mode has begun and the agent is now looking for possible zones
// to build around him. It will retrieve the best in his
// 1HNH (short for: one-hop-neighbourhood) and start communicating it.
+!doAction:
    zoneMode(true)
    & mustCommunicateZones(true)
    & position(PositionVertex)
    <- -+mustCommunicateZones(false);
       // start over new: clear all previous beliefs:
       -bestZone(_);
       -availableMinions(_);
       // ask Vertex for its zone as well as its neighbours
       // this will be done through one iA-call whose name
       // I don't know by heart:
       .getBestZonePLACEHOLDERiA(PositionVertex, 1, BestZone);
       .nth(0, BestZone, Value);
       .nth(1, BestZone, CentreNode);
       .nth(2, BestZone, UsedNodes);
       // trigger broadcasting:
       +bestZone(Value, CentreNode, UsedNodes)[source(self)].

// Our zone is better so we reply to the Coach with ours:
+foreignBestZone(Value, CentreNode, UsedNodes)[source(Coach)]:
    bestZone(BestZoneValue, BestZoneCentreNode, BestZoneUsedNodes)
    // my zone is better:
    & Value < BestZoneValue
    // or my the zones are identical but my name is alphabetically smaller:
    | (Value == BestZoneValue
        & .my_name(MyName)
        & MyName < Coach // TODO: that will probably not work
    )
    <- -foreignBestZone(Value, CentreNode, UsedNodes)[source(Coach)];
        // TODO: can we manipulate the sender of this message? Else we will need an additional parameter:
       .send(Coach, tell, foreignBestZone(BestZoneValue, BestZoneCentreNode, BestZoneUsedNodes)).

// We have received a zone better than the one we know (or
// didn't know any), so we throw our zone away and reply
// with our distance to the CentreNode.
//
// Also, we have to inform our minions about this.
//
// TODO: we must make a cut at some distance. Else all agents will want to go to one super-duper-awesome zone which is far away.
@onlyAddOneBetterZoneAtATime[atomic]
+foreignBestZone(Value, CentreNode, UsedNodes)[source(Coach)]:
    position(PositionVertex)
    // call the internal action to calculate my distance:
    & .getDistancePLACEHOLDERiA(PositionVertex, CentreNode, Distance)
    <- -foreignBestZone(Value, CentreNode, UsedNodes)[source(Coach)];
    
       // tell our minions to look for another zone:
       .findall(Minion, availableMinion(Minion), Minions);
       .send(Minions, tell, foreignBestZone(BestZoneValue, BestZoneCentreNode, BestZoneUsedNodes));
       -availableMinions(_);
       
       .send(Coach, tell, positiveZoneReply(CentreNode, Distance));
       // TODO: does that properly clear all previous best zones?:
       -+bestZone(Value, CentreNode, UsedNodes)[source(Coach)].

// If s.o. agreed to build a zone with us, we memorise his
// availability if he is replying to the zone which we still try
// to build right now.
+positiveZoneReply(CentreNode, Distance)[source(Minion)]:
    bestZone(BestZoneValue, BestZoneCentreNode, BestZoneUsedNodes)
    // TODO: is the CentreNode as an identifier sufficient? I'm thinking of overlapping 1HNHs whose zone value got updated in the mean time:
    & BestZoneCentreNode == CentreNode
    <- +availableMinion(Minion).
    
// If s.o. agreed to build a zone with us but we changed our
// mind already, we tell him.
//
// TODO: can we manipulate the sender of this message? Else we will need an additional parameter, because the change probably came from a different agent informing us:
+positiveZoneReply(CentreNode, Distance)[source(Minion)]:
    bestZone(BestZoneValue, BestZoneCentreNode, BestZoneUsedNodes)
    <- -positiveZoneReply(CentreNode, Distance)[source(WannabeCoach)];
       .send(Minion, tell, foreignBestZone(BestZoneValue, BestZoneCentreNode, BestZoneUsedNodes)).

// Zoning broadcasts may begin if we have found ourselves the bestZone:
//
// TODO: start working on this part and revise the upper belief addition triggers.
+bestZone(ZoneValue, UsedNodes, CentreNode)[source(self)]:
		broadcastAgentList(BroadcastList)
		& bestZone(ZoneValue, UsedNodes, CentreNode)
    <- .send(BroadcastList, tell, foreignBestZone(ZoneValue, UsedNodes, CentreNode)).
    
// We will wait for our coach to give us further instructions:
+bestZone(_, _, _)[source(_)]
    <- true.
    
// TODO: wait for all agents to reply and then build a zone.