package mas.agentsTargaryen.strategy;

import mas.agentsTargaryen.TargaryenAgent;
import massim.javaagents.agents.MarsUtil;
import apltk.interpreter.data.LogicBelief;
import eis.iilang.Action;

/**
 * Inspector: Wenn unbekannter Gegner in Reichweite: Inspecten
 */
public class InspectorInspectStrategy implements StrategyIf {

	@Override
	public Action execute(TargaryenAgent agent) {
		if (!agent.getRole().equals("Inspector")) {
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
						&& agent.getAllBeliefs("inspectedEntity", b.getParameters().get(1)).isEmpty()) {
					return MarsUtil.inspectAction();
				}
			}
		}
		return null;
	}
}