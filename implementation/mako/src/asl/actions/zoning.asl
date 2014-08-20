/* Initial beliefs and rules */
mustCommunicateZones(true).
// If this belief is true, the agent is either forming
// a zone or keeping one up and may deny any zone building
// requests:
isBusyZoning(false).

// @all: If you are looking for something to do,
//       search this file for the keyword "TODO".
// TODO: replace the placeholder names for internal actions with their actual names.
// TODO: split this file into several files (e.g. zone_negotiation and zone_forming).
// TODO: activate and deactivate isBusyZoning.

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

// Deny any zone building offers when we are busy building
// or keeping up a zone already:
+foreignBestZone(Value, CentreNode, UsedNodes)[source(Coach)]:
    isBusyZoning(true)
    <- -foreignBestZone(Value, CentreNode, UsedNodes)[source(Coach)];
        // TODO: is CentreNode sufficient as an identifier?
        .send(Coach, tell, negativeZoneReply(CentreNode)).

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
       .send(Minions, tell, foreignBestZone(Value, CentreNode, UsedNodes, Coach));
       -availableMinions(_);
       
       // if we were a minion ourselves, we should tell our master:
       ?bestZone(_, _, _)[source(PreviousCoach)];
       // TODO: would send to self fail or can we do it like this?:
       .send(PreviousCoach, tell, foreignBestZone(Value, CentreNode, UsedNodes, Coach));
       
       
       .send(Coach, tell, positiveZoneReply(CentreNode, Distance));
       // TODO: does that properly clear all previous best zones?:
       -+bestZone(Value, CentreNode, UsedNodes)[source(Coach)].

// Convert four parameter replies to three parameter replies. This
// is a workaround as we cannot fake the sender of a reply.
// This belief is set when an agent replies with knowledge about
// a better zone where he is not the coach.
+foreignBestZone(Value, CentreNode, UsedNodes, RemoteCoach)
    <- -foreignBestZone(Value, CentreNode, UsedNodes, RemoteCoach);
       +foreignBestZone(Value, CentreNode, UsedNodes)[source(RemoteCoach)].

// If s.o. agreed to build a zone with us, we memorise his
// availability if he is replying to the zone which we still try
// to build right now.
+positiveZoneReply(CentreNode, Distance)[source(Minion)]:
    bestZone(BestZoneValue, BestZoneCentreNode, BestZoneUsedNodes)
    // TODO: is the CentreNode as an identifier sufficient? I'm thinking of overlapping 1HNHs whose zone value got updated in the mean time:
    & BestZoneCentreNode == CentreNode
    <- -positiveZoneReply(CentreNode, Distance)[source(Minion)];
       +availableMinion(Minion);
       
       // TODO: this must be put somewhere else now as we wait for all replies and choose only the best:
       !choseClosestMinions;
       !toldMinionsTheirPosition.
       
// Use an internal action that determines where to place the minions the best
// and tell them:
+!toldMinionsTheirPosition:
    .findall(Minion, zoneMinion(Minion), Minions)
    & .bestZone(_, _, UsedNodes)
    & .getPositionsForMinionsPLACEHOLDERiA(UsedNodes, Minions, PositionMinionMapping)
    & .length(PositionMinionMapping, MappingLength)
	<- for (.range(ControlVariable, 0, MappingLength - 1)) {
	       .nth(ControlVariable, PositionMinionMapping, [Position, Minion]);
	       .send(Minion, tell, Position);
	   }.
	
//choose closest Minions in case that more agents committed themselves
//to the zone than actually needed and tell the agents that are not needed
//to look for another zone
+!choseClosestMinions:
    .bestZone(_, _, UsedNodes)
    & .findall(Minion, availableMinion(Minion), Minions)
    & .length(UsedNodes) < Minions
	& .findall([Distance, Minion], positiveZoneReply(_ , Distance)[source(Minion)], DistanceMinionList) // TODO: this is only possible if we keep the replies
	<- .sort(DistanceMinionList, SortedMinions);
	   for (.range(_, 0, .length(UsedNodes))) { // TODO: this is quite an imperative style, does .length get evaluated in this comparison?
	       .nth(0, SortedMinions, [_, ClosestMinion]); // TODO: does [_, X] work in .nth?
	       +zoneMinion(ClosestMinion);
	       .delete(0, SortedMinions, _);
	   };
	   // Inform minions which are not needed anymore:
	   // TODO: do we want to tell him already? We might need him if s.o. else drops off.
	   for (.range(ControlVariable, 0, .length(SortedMinions))) {
	       .nth(ControlVariable, SortedMinions, [_, UnnecessaryMinion]);
	       .send(UnnecessaryMinion, tell, lookForOtherZone(true))
	   }.

// We have as many minions as we need:
+!choseClosestMinions:
    .bestZone(_, _, UsedNodes)
    & .findall(Minion, availableMinion(Minion), Minions)
    & .length(UsedNodes) < Minions
    <- true.

// We don't have enough minions:
+!choseClosestMinions
    <- false.
		
// If s.o. agreed to build a zone with us but we changed our
// mind already, we tell him.
+positiveZoneReply(CentreNode, Distance)[source(Minion)]:
    bestZone(BestZoneValue, BestZoneCentreNode, BestZoneUsedNodes)[source(Coach)]
    <- -positiveZoneReply(CentreNode, Distance)[source(Minion)];
       .send(Minion, tell, foreignBestZone(BestZoneValue, BestZoneCentreNode, BestZoneUsedNodes, Coach)).

// If we get a negative zone reply, we remove the sender from
// our minions list – if he was on it.
+negativeZoneReply(CentreNode)[source(PossibleMinion)]:
    bestZone(BestZoneValue, BestZoneCentreNode, BestZoneUsedNodes)
    // TODO: is the CentreNode as an identifier sufficient?:
    & BestZoneCentreNode == CentreNode
    <- -negativeZoneReply(CentreNode)[source(PossibleMinion)];
       -availableMinion(PossibleMinion).

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
