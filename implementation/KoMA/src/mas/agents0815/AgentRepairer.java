package mas.agents0815;

import mas.agents0815.rules.*;

/**
 * The repairer surveys edges, moves randomly or joins a zone as standard behavior.
 * If an agent gets disabled, the first contacted repairer starts to calculate a
 * repair strategy (which repairer should take care, which agent moves/waits)
 * 
 * The repair order is processed in the two methods InitRepair and HandleRepair
 * InitRepair is only for repairers, HandleRepair is for all agents
 * 
 * InitRepair calculates a repair strategy (near zone, distance, decide responsibility).
 * Also decide which agent (damaged or repairer) should move.
 * Near Zone = repairer moves, otherwise damaged agent moves to repairer.
 * 
 * HandleRepair is used, if it's not the calculating repairer which has to move. It's
 * possible to use InitRepair for this if repairer moves, but not if an other agent moves.
 * So everything for a different agent is outsourced to HandleRepair.
 * 
 * TODO Dennis
 */
public class AgentRepairer extends SubsumptionAgent {

	
	public AgentRepairer(String name, String team) {
		super(name, team);
		this.getRelation().add(new RuleRepair());
		this.getRelation().add(new RuleGetRepair());
     	//needed because of low level repair (RuleInitRepair)
		this.getRelation().add(new RuleParry());
		getRelation().add(new RuleCheckGoals());
		getRelation().add(new RuleRecharge());
		//getRelation().add(new RuleInitRepair());
		//getRelation().add(new RuleHandleRepair());
		getRelation().add(new RuleSurvey());
		//relation.add(new RuleInitializeZone());
		getRelation().add(new RuleJoinZone());
		getRelation().add(new RuleRandomWalk());
		getRelation().add(new RuleSkip());
	}

	@Override
	public String getMyRole() {
		return Const.ROLEREPAIRER;
	}
	
	/*
	 * Relevant code:
	 * 
	 * - AgentRepairer: whole class
	 * - ConstOutput: tagged with TAG_REPAIRER
	 * - BeliefAndGoalsRevision:
	 * tagged with REPAIRER_UNBLOCK
	 * tagged with DAMAGED_RETURN
	 * 
	 * 
	 * - SubsumptionAgent:
	 * processAction(): tagged with REPAIRER_PROCESS_INITREPAIR
	 * processAction(): tagged with REPAIRER_PROCESS_HANDLEREPAIR
	 * 
	 * - RuleInitRepair: whole class
	 * - DoInitRepair: whole class
	 * 
	 * - RuleHandleRepair: whole class
	 * - DoHandleRepair: whole class
	 * 
	 * - RuleCheckGoals: tagged with BEHAVIOR_DISABLED
	 * 
	 * 
	 * Enhance:
	 * - BeliefAndGoalsRevision: tagged with ENHANCE_REPAIRER_MESSAGE 
	 * 
	 * 
	 * 
	 * 
	 * ###############################################################################################
	 * ###############################################################################################
	 * 
	 * Encountered problems:
	 * agentA5
	 * Exception in thread "main" java.lang.IndexOutOfBoundsException: Index: 0, Size: 0
	 * 	at java.util.LinkedList.entry(Unknown Source)
	 * 	at java.util.LinkedList.get(Unknown Source)
	 * 	at mas.agents0815.rules.RuleJoinTwoPartyZone.fire(RuleJoinTwoPartyZone.java:20)
	 * 
	 * ###
	 * 
	 * error with alliedRole. section from InitRepair():
	 * AlliedRoles: alliedRole(agentA9,inspector).
	 * AlliedRoles: alliedRole(agentA8,agentA8).
	 * AlliedRoles: alliedRole(agentA2,explorer).
	 * AlliedRoles: alliedRole(agentA1,agentA1).
	 * AlliedRoles: alliedRole(agentA10,inspector).
	 * AlliedRoles: alliedRole(agentA4,agentA4).
	 * AlliedRoles: alliedRole(agentA6,agentA6).
	 * AlliedRoles: alliedRole(agentA7,agentA7).
	 * AlliedRoles: alliedRole(agentA5,agentA5).
	 * AlliedRoles: alliedRole(agentA6,saboteur).
	 * AlliedRoles: alliedRole(agentA10,agentA10).
	 * AlliedRoles: alliedRole(agentA9,agentA9).
	 * AlliedRoles: alliedRole(agentA7,sentinel).
	 * AlliedRoles: alliedRole(agentA8,sentinel).
	 * AlliedRoles: alliedRole(agentA2,agentA2).
	 * 
	 * Reason (SubsumptionAgent):
	 * 	if (l.getPredicate().equals(Const.ALLIEDROLE))
	 * 		addBelief(new LogicBelief(Const.ALLIEDROLE, m.sender, l.getParameters().get(0)));
	 * has to be get(1)
	 * 
	 * ###
	 * 
	 * repairer tries to repair with depleted energy
	 * correction: RuleRecharge comes as top priority (recharge order from checkGoals only fires if goalBase not empty)
	 * 
	 * ###
	 * 
	 * ###############################################################################################
	 * ###############################################################################################
	 * 
	 * 
	 * Strategy changes:
	 * 1) At first there were extra beliefs for all seen agents for the repairers, so one could calculate
	 * a route for the other even if he hasn't seen him yet. This was changed. If the repairers didn't
	 * contacted each other yet, they will only plan for themselves.
	 * 
	 * 
	 * 
	 * 
	 * beliefs:
	 * 
	 * RETURNFORREPAIR, agentName (agentName is the target for findRoute)
	 * 
	 * 
	 * 
	 * 
	 * ###############################################################################################
	 * ###############################################################################################
	 * 
	 *
	 * 
	 * 
	 * TODO
	 * 
	 * IDEA: insert method so that the goalBase is deleted, if the last (3?) actions failed
	 * IDEA: change return for findRoute if there was no route found (so this can be detected)
	 * 
	 * - handle case that last action was failed
	 * 
	 * - add belief in DoInitRepair for handling the problem "the other repairer is unknown"
	 * -- check each step if the other repairer is now known and submit active blocks
	 * 
	 * 
	 * 
	 * 
	 * General affairs:
	 * 
	 * - Eine uebersichtliche Liste mit allen neuen Beliefs erstellen (zur besseren Uebersicht & zum debuggen)
	 * 
	 */
}