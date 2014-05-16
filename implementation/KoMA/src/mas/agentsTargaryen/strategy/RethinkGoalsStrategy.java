package mas.agentsTargaryen.strategy;

import mas.agentsTargaryen.TargaryenAgent;
import apltk.interpreter.data.LogicGoal;
import eis.iilang.Action;

/**
 * Langfristige Ziele ueberdenken
 */
public class RethinkGoalsStrategy implements StrategyIf {

	@Override
	public Action execute(TargaryenAgent agent) {
		// Wenn earlygame: Welt erkunden, sonst: Zone bauen
		if (!agent.getAllGoals("zoning2").isEmpty()) {
			return null;
		}
		if (agent.getRole().equals("Explorer")) {
			if (agent.getStep() < 100) {
				agent.addGoal(new LogicGoal("scanVertex"));
			} else {
				agent.addGoal(new LogicGoal("zoning"));
			}
		} else {
			if (agent.getStep() < 50) {
				agent.addGoal(new LogicGoal("testEdge"));
			} else {
				agent.addGoal(new LogicGoal("zoning"));
			}
		}
		return null;
	}
}