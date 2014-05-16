package mas.agentsTargaryen.strategy;

import java.util.ArrayList;

import mas.agentsTargaryen.TargaryenAgent;
import massim.javaagents.agents.MarsUtil;
import apltk.interpreter.data.LogicBelief;
import eis.iilang.Action;

/**
 * Wenn Agent kaputt, dann braucht er Repair-Hilfe
 */
public class NeedRepairStrategy implements StrategyIf {

	@Override
	public Action execute(TargaryenAgent agent) {
		if (!agent.isDisabled()) {
			agent.removeBeliefs("repairComing");
			return null;
		}
		// Hilfe rufen
		agent.broadcastBelief(new LogicBelief("needRepair", agent.getName(), agent.getPosition()));
		if (agent.getAllBeliefs("repairComing").isEmpty()) {
			return null;
		}
		if (agent.getAllBeliefs("position", "", agent.getAllBeliefs("repairComing").getFirst().getParameters().get(0)).isEmpty()) {
			return null;
		}
		// Wenn Repairer auf Nachbarknoten oder eigener Position ist: skippen
		if (agent
				.getUtil()
				.getNeighborVertexes(agent.getPosition())
				.contains(
						agent.getAllBeliefs("position", "", agent.getAllBeliefs("repairComing").getFirst().getParameters().get(0))
								.getFirst().getParameters().get(0))
				|| agent.getPosition().equals(
						agent.getAllBeliefs("position", "", agent.getAllBeliefs("repairComing").getFirst().getParameters().get(0))
								.getFirst().getParameters().get(0))) {
			return new SkipStrategy().execute(agent);
		} else {
			// sonst entgegen laufen (nur falls Weg bekannt)
			ArrayList<String> ziel = new ArrayList<String>();
			ziel.add(agent.getAllBeliefs("position", "", agent.getAllBeliefs("repairComing").getFirst().getParameters().get(0)).getFirst()
					.getParameters().get(0));
			ArrayList<String> path = agent.getUtil().getDirection(agent.getPosition(), ziel);
			if (path.size() > 0 && path.get(0).equals(agent.getPosition())) {
				path.remove(0);
			}
			if (path.size() > 0 && agent.getUtil().getNeighborVertexes(agent.getPosition()).contains(path.get(0))) {
				return MarsUtil.gotoAction(path.get(0));
			}
		}
		return null;
	}
}