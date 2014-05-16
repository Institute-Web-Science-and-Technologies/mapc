package mas.agents0815;

import mas.agents0815.rules.RuleCheckGoals;
import mas.agents0815.rules.RuleGetRepair;
import mas.agents0815.rules.RuleGotoNearestUnprobedVertex;
import mas.agents0815.rules.RuleInitializeZone;
import mas.agents0815.rules.RuleJoinZone;
import mas.agents0815.rules.RuleProbe;
import mas.agents0815.rules.RuleProbeInZone;
import mas.agents0815.rules.RuleRandomWalk;
import mas.agents0815.rules.RuleRecharge;
import mas.agents0815.rules.RuleSkip;
import mas.agents0815.rules.RuleSurvey;


/**
 * Role description:
 * moves around the map and surveys the map
 * when step X has passed, he tries to join the established zone
 * 
 */
public class AgentExplorer extends SubsumptionAgent{

	public AgentExplorer(String name, String team) {
		super(name, team);
		this.getRelation().add(new RuleGetRepair());
		getRelation().add(new RuleCheckGoals());
		getRelation().add(new RuleRecharge());
		getRelation().add(new RuleProbeInZone());
		getRelation().add(new RuleProbe());
		getRelation().add(new RuleGotoNearestUnprobedVertex());// TODO when does this
		// fire?
		getRelation().add(new RuleInitializeZone());
		getRelation().add(new RuleJoinZone());
		getRelation().add(new RuleSurvey());
		getRelation().add(new RuleRandomWalk());
		getRelation().add(new RuleSkip());
	}

	@Override
	public String getMyRole() {
		return Const.ROLEEXPLORER;
	}

}
