package mas.agents0815;

import mas.agents0815.rules.*;

/**
 * Role description:
 * does a random walk and chase enemies, whenever he sees any
 * if a zone is established he look for enemies out of the zone
 * (so he does not enter his teams zone)
 */
public class AgentSaboteur extends SubsumptionAgent {

	public AgentSaboteur(String name, String team) {
		super(name, team);
		
		this.getRelation().add(new RuleGetRepair());
		this.getRelation().add(new RulePrimitiveAttack());
		this.getRelation().add(new RuleCheckGoals());
		this.getRelation().add(new RuleRecharge());
		this.getRelation().add(new RuleBuySaboteurUpgrades());
		this.getRelation().add(new RuleAttackEnemyZone());	
		this.getRelation().add(new RuleRandomWalk());
	}

	@Override
	public String getMyRole() {
		return Const.ROLESABOTEUR;
	}

}
