/* Initial beliefs and rules */

/* Plans */

// We lock ourselves to fully obey our coach. Hence, we unregister us from the
// idleZoner list.
// Next, we tell our Coach how far it is for us to move to his CentreNode.
@becomeAMinion
+!choseZoningRole:
    bestZone(_, CentreNode, _)[source(Coach)]
    & Coach \== self
    & broadcastAgentList(BroadcastList)
    & .my_name(MyName)
    <- .send(BroadcastList, untell, idleZoner(MyName));
       ?distanceToBestZone(Distance);
       .send(Coach, tell, positiveZoneReply(CentreNode)).

// If we had been a coach in our previous life or if the sender is just
// confused, tell him to find a new zone on his own and leave us alone.
+positiveZoneReply( _)[source(Sender)]
    <- .send(Sender, achieve, foundNewZone).

// If we got a zoneNode, which is a node we should move to to build a zone, we
// will move there.
// TODO: this action will probably be either executed too early or too late. Place it at the correct place in agent.asl.
+!doAction:
    zoneNode(GoalVertex)
    <- .getNextHopForGotoPLACEHOLDERiA(GoalVertex, NextHop);
       goto(NextHop).