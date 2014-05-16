package mas.agentsTargaryen.strategy;

import java.util.ArrayList;

import mas.agentsTargaryen.TargaryenAgent;
import massim.javaagents.agents.MarsUtil;
import apltk.interpreter.data.LogicBelief;
import eis.iilang.Action;

/**
 * Zone verteidigen
 */
public class DefendZoningStrategy implements StrategyIf {

	@Override
	public Action execute(TargaryenAgent agent) {
		if (agent.getAllGoals("zoning2").isEmpty() || !agent.getRole().equals("Saboteur")) {
			return null;
		}
		ArrayList<String> target = new ArrayList<String>();
		String team = agent.getEnemyTeam();
		String ward = "";
		if (team == null) {
			return null;
		}
		if (!agent.getAllBeliefs("wardAgent").isEmpty()) {
			ward = agent.getAllBeliefs("wardAgent").get(0).getParameters().get(0);
		}
		if (agent.getAllBeliefs("visibleEntity", "", "", team).isEmpty()) {
			return null;
		}
		for (LogicBelief b : agent.getAllBeliefs("visibleEntity", "", "", team)) {
			if (b.getParameters().get(4).equals("normal") && b.getParameters().get(0).equals(ward)) {
				target.add(b.getParameters().get(3));
			}
		}
		if (target.isEmpty()) {
			return null;
		}
		ArrayList<String> path = agent.getUtil().getDirection(agent.getPosition(), target);
		if (path.size() > 0) {
			if (path.get(0).equals(agent.getPosition())) {
				path.remove(0);
			}
		}
		if (path.size() > 0 && agent.getUtil().getNeighborVertexes(agent.getPosition()).contains(path.get(0))) {
			return MarsUtil.gotoAction(path.get(0));
		}
		return null;
	}
}