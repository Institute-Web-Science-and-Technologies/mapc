{ include("../actions/zoning.coach.defence.asl") }
/* Initial beliefs and rules */

/* Plans */

// Use an internal action that determines where to place the agents the best.
// The agents include all the coach's minions as well as himself.
// Then tell the minions that they should try (achievement goal) to go to their
// designated vertex. 
//
//  After that all, remaining idle agents are told to start a new round of zoning.
+!assignededAgentsTheirPosition:
    isCoach(true)
    & bestZone(ZoneValue, CentreNode, ClosestAgents)
    & .print("[zoning][coach] Trying to find out how to place ", ClosestAgents)
    & broadcastAgentList(BroadcastList)
    & ia.placeAgentsOnZone(CentreNode, ClosestAgents, AgentPositionMapping)
    <- .difference(BroadcastList, ClosestAgents, NonZoners);
       .send(NonZoners, achieve, preparedNewZoningRound);
       
       .length(AgentPositionMapping, MappingLength);
       .print("[zoning][coach] I'm going to place the agents as follows: ", AgentPositionMapping, " with the CentreNode being ", CentreNode);
       for (.range(ControlVariable, 0, MappingLength - 1)) {
           .nth(ControlVariable, AgentPositionMapping, [Agent, PositionVertex]);
           .send(Agent, tell, zoneGoalVertexProposal(ZoneValue, CentreNode, ClosestAgents, PositionVertex));
       };
       -+isLocked(false).

// The achievement goal failed for some reason. Tell all agents to restart
// zoning. Simply calling !cancelledZoneBuilding isn't possible because the zone
// was never registered as a zone in JavaMap. In general, this goal shouldn't be
// called anymore. But it's always good to have a backup plan.
+!assignededAgentsTheirPosition:
    isCoach(true)
    & .my_name(Coach)
    & bestZone(_, CentreNode, ClosestAgents)
    & broadcastAgentList(BroadcastList)
    <- .print("[zoning][coach][bug] Assigning agents a position failed.");
       .difference(BroadcastList, ClosestAgents, NonZoners);
       .send(NonZoners, achieve, preparedNewZoningRound);
       
       .difference(ClosestAgents, [Coach], Minions);
       .send(Minions, achieve, cancelledZoneBuilding);
       
       !resetZoningBeliefs;
       !preparedNewZoningRound;
       
       -+isLocked(false).

// If s.o. or ourselves cancelled the zone, we have to inform all our minions
// about it and go back to start zoning from scratch.
// The lock should make sure that this goal is not processed before
// !assignededAgentsTheirPosition was processed.
// Also resets the currentRange.
@cancelZoneAndInformMinions[atomic]
+!cancelledZoneBuilding[source(Sender)]:
    isCoach(true)
    & .my_name(Coach)
    & bestZone(_, CentreNode, ClosestAgents)
    & (Sender == self
        | .member(Sender, ClosestAgents)
    )
    & .length(ClosestAgents, ZoneSize)
    <- ia.destroyZone(CentreNode, ZoneSize);
       .difference(ClosestAgents, [Coach, Sender], UnawareMinions);
       .send(UnawareMinions, achieve, cancelledZoneBuilding);
       .print("[zoning][coach] ", Sender, " told me to destroy my zone. I had to inform ", UnawareMinions);
       
       // Cancel ordered saboteurs if any. Can't be "!!" because it needs
       // bestZone(_,CentreNode,_):
       !informedSaboteursAboutZoneBreakup;
       
       !resetZoningBeliefs;
       !preparedNewZoningRound.

// Plan for handling !cancelledZoneBuilding achivement goals which were sent
// from an agent (minion) that is not in our zone. The coach will ignore it.
+!cancelledZoneBuilding[source(Sender)]:
    isCoach(true)
    & bestZone(_, CentreNode, ClosestAgents)
    <- .print("[zoning][coach] ", Sender, " wanted me to destroy my zone but he is not my minion. Ignoring.").

// Plan that fixes/works with the bug that sometimes agents have forgotten its bestZone belief.
// In that case the agent restarts the zoning process.
+!cancelledZoneBuilding[source(_)]:
    isCoach(true)
    <- !informedSaboteursAboutZoneBreakup;
       !resetZoningBeliefs;
       !preparedNewZoningRound;
       .print("[zoning][coach][bug] I forgot about my bestZone belief. I have no idea how this can happen. Restarting zoning.").