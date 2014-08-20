/* Initial beliefs and rules */
mustCommunicateZones(true).

// @all: If you are looking for something to do,
//       search this file for the keyword "TODO".
// TODO: replace the placeholder names for internal actions with their actual names.
// TODO: a later plan/achievement should deal with searching the 1HNH for only .count(idleZoner) amount of agents
// TODO: foundNewZone achievement goal needs to be written

/* Plans */

// Zoning mode has begun and the agent is now looking for possible zones
// to build around him. It will retrieve the best in his
// 1HNH (short for: one-hop-neighbourhood) and start broadcasting it.
// It will also register as an idleZoner.
+zoneMode(true):
    mustCommunicateZones(true)
    & position(PositionVertex)
    & broadcastAgentList(BroadcastList)
    & .my_name(MyName)
    <- -+mustCommunicateZones(false);
       // tell all agents that I am ready to build zones:
       .send(BroadcastList, tell, idleZoner(MyName));
       // start over new: clear all previous beliefs:
       -bestZone(_)[source(_)];
       -positiveZoneReply(_)[source(_)];
       -negativeZoneReply[source(_)];
       -zoneNode(_)[source(_)]
       // ask Vertex for its zone as well as its neighbours
       // this will be done through one iA-call whose name
       // I don't know by heart:
       .getBestZonePLACEHOLDERiA(PositionVertex, 1, BestZone);
       .nth(0, BestZone, Value);
       .nth(1, BestZone, CentreNode);
       .nth(2, BestZone, UsedNodes);
       // trigger broadcasting:
       +bestZone(Value, CentreNode, UsedNodes)[source(self)];
       .send(BroadcastList, tell, foreignBestZone(Value, CentreNode, UsedNodes)).

// We have received a zone better than the one we know (or
// didn't know any), so we throw our zone away
// We also calculate the distance to it and cache that value
// as a belief. We discard better zones that are too far away.
//
// TODO: we must make a cut at some distance. Else all agents will want to go to one super-duper-awesome zone which is far away.
@onlyAddOneBetterZoneAtATime[atomic]
+foreignBestZone(Value, CentreNode, UsedNodes)[source(Coach)]:
    position(PositionVertex)
    & .getDistancePLACEHOLDERiA(PositionVertex, CentreNode, Distance)
    & Distance < 20 // TODO: do something reasonable
    & bestZone(BestZoneValue, BestZoneCentreNode, BestZoneUsedNodes)[source(FormerCoach)]
    // my zone is worse:
    & BestZoneValue < Value
    // or the zones are identical but my name is lexicographically bigger:
    | (Value == BestZoneValue
        & .my_name(MyName)
        & Coach < MyName// TODO: that will probably not work
    )
    <- -bestZone(BestZoneValue, BestZoneCentreNode, BestZoneUsedNodes)[source(FormerCoach)]; // TODO: or use .abolish(bestZone(_,_,_))
       +bestZone(Value, CentreNode, UsedNodes)[source(Coach)];
       -+distanceToBestZone(Distance);
       !receivedAllReplies.

// We were informed about a worse zone. Do nothing.
+foreignBestZone(_, _, _)
    <- true.

// If all idleZoners have replied, we will have the best zone stored.
// We will then choose our role for this zoning part (Coach vs. Minion).
+!receivedAllReplies:
    .count(foreignBestZone(_, _, _), broadcastRepliesAmount)
    & .count(idleZoner(_), availableZonersAmount)
    & broadcastRepliesAmount == availableZonersAmount
    <- !choseZoningRole.

// We still have to wait.
// TODO: should we fail silently with true or is false valid?
+!receivedAllReplies
    <- false.