package mas.agents0815;

import mas.agents0815.rules.*;

/**
 * Role description:
 * as soon as a zone is established, he identifies enemies coming in the zon
 * and attacks them
 * 
 */
public class AgentDefender extends SubsumptionAgent {

	public AgentDefender(String name, String team) {
		super(name, team);
		this.getRelation().add(new RuleGetRepair());
		this.getRelation().add(new RulePrimitiveAttack());
		this.getRelation().add(new RuleCheckGoals());	
		this.getRelation().add(new RuleBuyDefenderUpgrades());
		this.getRelation().add(new RuleDefendZone());
		this.getRelation().add(new RuleRecharge());
		this.getRelation().add(new RuleSurvey());
		this.getRelation().add(new RuleRandomWalk());
		this.getRelation().add(new RuleSkip());
	}

	@Override
	public String getMyRole() {
		return Const.ROLESABOTEUR;
	}

}
