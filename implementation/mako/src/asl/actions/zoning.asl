/* Initial beliefs and rules */
isCoach(false).
isMinion(false).
// @all: If you are looking for something to do, search this file as well as
//       zoning.* for the keyword "TODO".
// TODO [prio:low]: test that the internal actions work properly when JavaMap is merged onto master.
// TODO [prio:high]: !foundNewZone achievement goal needs to be written. It expresses that an agent should either ask for expendable zones or start zoning anew. Its stub is located at the bottom of this fail.
// TODO [prio:medium]: we must make a cut at some distance. Else all agents will want to go to one super-duper-awesome zone which is far away. @see onlyAddOneBetterZoneAtATime
// TODO [prio:low]: is zoneMode really only set once? Else, we will have to lock the zoneMode(true) belief trigger with a mustCommunicateZones(true) belief.
// TODO [prio:high]: if we receive a broadcast from a new idleZoner which we haven't talked to yet, reply him our bestZone if we aren't building yet. Currently, we only process our own bestZone and send negativeZoneReplies to bestZones we abandon.
// TODO [prio:low]: search our 1HNH only for zones which need at max .count(idleZoner(_),X)+1 many agents. This could be problematic as idleZoner is extremely dynamic.
// TODO [prio:medium]: implement alphabetical comparison in @see onlyAddOneBetterZoneAtATime.
// TODO [prio:medium]: in zoning.coach.asl we tell Minions we don't need to search a different zone. Instead, we could have them extend our zone (if possible). The corresponding belief triggered event is marked with a TODO.

/* Plans */

// Zoning mode has begun and it will trigger the achievement goal builtZone.
+zoneMode(true)
    <- !builtZone.
    
// The agent is now looking for possible zones to build around him. It will
// retrieve the best in his 1HNH (short for: one-hop-neighbourhood) and start
// broadcasting it. It will also register as an idleZoner.
+!builtZone:
    position(PositionVertex)
    & broadcastAgentList(BroadcastList)
    & .my_name(MyName)
    <- // tell all agents that I am ready to build zones:
       .send(BroadcastList, tell, idleZoner(MyName));
       
       // start over new: clear all previous beliefs:
       -bestZone(_)[source(_)];
       -positiveZoneReply(_)[source(_)];
       -negativeZoneReply[source(_)];
       -zoneNode(_)[source(_)];
       -foreignBestZone(_, _, _)[source(_)];
       
       // ask Vertex for its zone as well as its neighbours:
       ia.getBestZone(PositionVertex, 1, Value, CentreNode, UsedNodes);
       // trigger broadcasting:
       +bestZone(Value, CentreNode, UsedNodes)[source(self)];
       .send(BroadcastList, tell, foreignBestZone(Value, CentreNode, UsedNodes)).

// We have received a zone better than the one we know (or
// didn't know any), so we throw our zone away
// We also calculate the distance to it and cache that value
// as a belief. We discard better zones that are too far away.
//
// We also tell our former coach that we are no longer interested in his zone!
@onlyAddOneBetterZoneAtATime[atomic]
+foreignBestZone(Value, CentreNode, UsedNodes)[source(Coach)]:
    isCoach(false) & isMinion(false)
    & position(PositionVertex)
    & ia.getDistance(PositionVertex, CentreNode, Distance)
    & Distance < 20
    & bestZone(FormerZoneValue, FormerZoneCentreNode, FormerZoneUsedNodes)[source(FormerCoach)]
    // my zone is worse:
    & FormerZoneValue < Value
    // or the zones are identical but my name is alphabetically bigger:
    | (Value == FormerZoneValue
        & .my_name(MyName)
        & Coach < MyName// TODO: that will probably not work
    )
    <- .send(FormerCoach, tell, negativeZoneReply(FormerZoneCentreNode));
       -bestZone(FormerZoneValue, FormerZoneCentreNode, FormerZoneUsedNodes)[source(FormerCoach)]; // or use .abolish(bestZone(_,_,_))
       +bestZone(Value, CentreNode, UsedNodes)[source(Coach)];
       !receivedAllReplies.

// We were informed about a worse zone. Or, we aren't even interested in zoning.
// In both ways, tell the sender and test whether we have received all replies.
+foreignBestZone(_, CentreNode, _)[source(Sender)]:
    isCoach(false) & isMinion(false)
    <- .send(Sender, tell, negativeZoneReply(CentreNode));
       !receivedAllReplies.

// Don't count the negative reply AND the broadcast of the same sender in the
// !receivedAllReplies[2] achievement goal.
+negativeZoneReply(_)[source(Sender)]:
    isCoach(false) & isMinion(false)
    & foreignBestZone(_, _, _)[source(Sender)]
    <- -foreignBestZone(_, _, _)[source(Sender)];
       !receivedAllReplies.

// We only got a negative zone reply. We will test if we now got replies from
// every agent.
+negativeZoneReply(_)[source(_)]:
    isCoach(false) & isMinion(false)
    <- !receivedAllReplies.

// If all idleZoners have replied, we will have the best zone stored.
// We will then choose our role for this zoning part (Coach vs. Minion).
+!receivedAllReplies:
    .count(foreignBestZone(_, _, _), BroadcastRepliesAmount)
    & .count(idleZoner(_), AvailableZonersAmount)
    & BroadcastRepliesAmount == AvailableZonersAmount
    <- !choseZoningRole.

// Or if all agents have replied with either a broadcast or a refusal. This
// achievement goal is intended for zoners who appear after the first broadcast
// storm.
+!receivedAllReplies:
    .count(foreignBestZone(_, _, _), BroadcastRepliesAmount)
    & .count(negativeZoneReply(_), RefusalAmount)
    & .count(broadcastAgentList(BroadcastList), AgentsAmount) // or use 28 as a static measure :Þ
    & AgentsAmount ==  BroadcastRepliesAmount + RefusalAmount
    <- !choseZoningRole.

// We still have to wait and fail this achievement goal silently as true.
+!receivedAllReplies
    <- true.

// If we get told (or decide ourselves) to cancel our current process and start
// a different approach.
// The current behaviour is just a placeholder.
+!foundNewZone
    <- !cancelledZoneBuilding.