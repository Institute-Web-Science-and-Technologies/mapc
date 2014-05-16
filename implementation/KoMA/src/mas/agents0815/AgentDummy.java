package mas.agents0815;

import mas.agents0815.rules.RuleCheckGoals;
import mas.agents0815.rules.RuleGotoNearestUnprobedVertex;
import mas.agents0815.rules.RuleInitializeZone;
import mas.agents0815.rules.RuleJoinZone;
import mas.agents0815.rules.RuleProbe;
import mas.agents0815.rules.RuleRandomWalk;
import mas.agents0815.rules.RuleRecharge;
import mas.agents0815.rules.RuleSkip;
import mas.agents0815.rules.RuleSurvey;


/**
 * @author Florian
 * as in Subsumption Agent
 *
 */
public class AgentDummy extends SubsumptionAgent{

	public AgentDummy(String name, String team) {
		super(name, team);

		getRelation().add(new RuleSkip());
	}

	@Override
	public String getMyRole() {
		return Const.ROLEEXPLORER;
	}

}
