package mas.agents0815;

import mas.agents0815.rules.RuleCheckGoals;
import mas.agents0815.rules.RuleGetRepair;
import mas.agents0815.rules.RuleInitializeZone;
import mas.agents0815.rules.RuleJoinZone;
import mas.agents0815.rules.RulePlanSurvey;
import mas.agents0815.rules.RuleRandomWalk;
import mas.agents0815.rules.RuleRecharge;
import mas.agents0815.rules.RuleSkip;

/**
 * Role description:
 * identify "good" positions to do a survey, go there
 * and survey
 * as soon as a zone is established, he joins in
 */
public class AgentSentinel extends SubsumptionAgent{

	public AgentSentinel(String name, String team) {
		super(name, team);
		this.getRelation().add(new RuleGetRepair());
		getRelation().add(new RuleCheckGoals());
		getRelation().add(new RuleRecharge());
		getRelation().add(new RulePlanSurvey());
		getRelation().add(new RuleInitializeZone());
		getRelation().add(new RuleJoinZone());
		getRelation().add(new RuleRandomWalk());
		getRelation().add(new RuleSkip());
	}

	@Override
	public String getMyRole() {
		return Const.ROLESENTINEL;
	}

}
