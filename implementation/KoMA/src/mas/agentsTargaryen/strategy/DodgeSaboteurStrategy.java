package mas.agentsTargaryen.strategy;

import mas.agentsTargaryen.TargaryenAgent;
import massim.javaagents.agents.MarsUtil;
import apltk.interpreter.data.LogicBelief;
import eis.iilang.Action;

/**
 * Weglaufen, wenn feindlicher Saboteur auf selbem Feld ist
 */
public class DodgeSaboteurStrategy implements StrategyIf {

	@Override
	public Action execute(TargaryenAgent agent) {
		if (agent.isDisabled()) {
			return null;
		}
		for (LogicBelief b : agent.getAllBeliefs("visibleEntity")) {
			// Nur Gegner beachten
			if (b.getParameters().get(3).equals(agent.getTeam())) {
				continue;
			}
			// Gegner auf gleichem Feld?
			if (b.getParameters().get(2).equals(agent.getPosition())) {
				// Kennen wir den Gegner?
				if (!agent.getAllBeliefs("inspectedEntity", b.getParameters().get(1)).isEmpty()) {
					// Ist Gegner Saboteur?
					if (agent.getAllBeliefs("inspectedEntity", b.getParameters().get(1)).getFirst().getParameters().get(2)
							.equals("Saboteur")) {
						// Ist Gegner heile?
						if (b.getParameters().get(4).equals("normal")) {
							// Sind wir selber Saboteur? Volles Pfund aufs Maul!
							if (agent.getRole().equals("Saboteur") && agent.getEnergy() >= 3) {
								return MarsUtil.attackAction(b.getParameters().get(1));
							} else if (agent.getEnergy() >= 10) {
								// Lauf!
								return new RandomWalkStrategy().execute(agent);
							} else if ((agent.getEnergy() >= 2)
									&& (agent.getRole().equals("Repairer") || agent.getRole().equals("Saboteur") || agent.getRole().equals(
											"Sentinel"))) {
								// Parry!
								return MarsUtil.parryAction();
							}
						}
					}
				}
			}
		}
		return null;
	}
}