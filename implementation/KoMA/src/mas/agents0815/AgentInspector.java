package mas.agents0815;

import mas.agents0815.rules.*;

/**
 * Role description:
 * does random walk as long as having enemies in range
 * then he is doing a inspect action
 * as soon as there is a zone, he joins in
 */
public class AgentInspector extends SubsumptionAgent {
	

	public AgentInspector(String name, String team) {
		super(name, team);
		this.getRelation().add(new RuleGetRepair());
		this.getRelation().add(new RuleCheckGoals());	
		this.getRelation().add(new RuleJoinZone());
		this.getRelation().add(new RuleRecharge());
		this.getRelation().add(new RuleInspect());
		this.getRelation().add(new RuleRandomWalk());
		this.getRelation().add(new RuleSkip());
	}

	@Override
	public String getMyRole() {
		return Const.ROLEINSPECTOR;
	}

}
