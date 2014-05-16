package mas.agentsTargaryen.strategy;

import mas.agentsTargaryen.TargaryenAgent;
import massim.javaagents.agents.MarsUtil;
import apltk.interpreter.data.LogicBelief;
import eis.iilang.Action;

/**
 * Saboteur: Wenn Gegner in Reichweite: Angriff
 */
public class SaboteurAttackStrategy implements StrategyIf {

	@Override
	public Action execute(TargaryenAgent agent) {
		if (!agent.getRole().equals("Saboteur")) {
			return null;
		}
		if (agent.isDisabled()) {
			return null;
		}
		if (!agent.getAllBeliefs("visibleEntity", agent.getName()).isEmpty()) {
			for (LogicBelief b : agent.getAllBeliefs("visibleEntity", agent.getName())) {
				if (!b.getParameters().get(3).equals(agent.getTeam())
						&& b.getParameters().get(2)
								.equals(agent.getAllBeliefs("position", "", agent.getName()).getFirst().getParameters().get(0))
						&& b.getParameters().get(4).equals("normal")) {
					return MarsUtil.attackAction(b.getParameters().get(1));
				}
			}
		}
		return null;
	}
}