package mas.agents0815;

import mas.agents0815.rules.RuleAnnoyEnemyZone;
import mas.agents0815.rules.RuleBuySentinelUpgrades;
import mas.agents0815.rules.RuleCheckGoals;
import mas.agents0815.rules.RuleEvadeSaboteur;
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
public class AgentAggressiveSentinel extends SubsumptionAgent{

	public AgentAggressiveSentinel(String name, String team) {
		super(name, team);
		this.getRelation().add(new RuleGetRepair());
		getRelation().add(new RuleCheckGoals());
		getRelation().add(new RuleEvadeSaboteur());
		getRelation().add(new RuleRecharge());
		getRelation().add(new RuleBuySentinelUpgrades());
		getRelation().add(new RuleAnnoyEnemyZone());
		getRelation().add(new RulePlanSurvey());
		getRelation().add(new RuleRandomWalk());
		getRelation().add(new RuleSkip());
	}

	@Override
	public String getMyRole() {
		return Const.ROLESENTINEL;
	}

}
