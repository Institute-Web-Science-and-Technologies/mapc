package mas.agentsTargaryen.strategy;

import java.util.ArrayList;

import mas.agentsTargaryen.TargaryenAgent;
import massim.javaagents.agents.MarsUtil;
import apltk.interpreter.data.LogicBelief;
import apltk.interpreter.data.LogicGoal;
import eis.iilang.Action;

/**
 * Repairer: Repair-Strategy
 */
public class RepairerRepairStrategy implements StrategyIf {

	@Override
	public Action execute(TargaryenAgent agent) {
		if (!agent.getRole().equals("Repairer")) {
			return null;
		}
		if (!agent.getAllGoals("repairAlly").isEmpty()) {
			LogicGoal goal = agent.getAllGoals("repairAlly").getFirst();
			// Wenn Position identisch zu kaputtem Agent: Reparieren
			if (agent.getPosition().equals(goal.getParameters().get(1))) {
				agent.deleteGoals("repairAlly");
				return MarsUtil.repairAction(goal.getParameters().get(0));
			} else {
				// sonst hingehen
				ArrayList<String> ziel = new ArrayList<String>();
				ziel.add(goal.getParameters().get(1));
				ArrayList<String> path = agent.getUtil().getDirection(agent.getPosition(), ziel);
				if (path.size() > 0 && path.get(0).equals(agent.getPosition())) {
					path.remove(0);
				}
				if (path.size() > 0 && !path.get(0).equals("test")
						&& agent.getUtil().getNeighborVertexes(agent.getPosition()).contains(path.get(0))) {
					return MarsUtil.gotoAction(path.get(0));
				} else {
					// wenn Weg unbekannt: Anderen Repairer rufen
					agent.deleteGoals("repairAlly");
					agent.broadcastBelief(new LogicBelief("needRepair", goal.getParameters().get(0), goal.getParameters().get(1)));
				}
			}
		}
		return null;
	}
}