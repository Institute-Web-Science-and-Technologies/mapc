package mas.agentsTargaryen.strategy;

import java.util.ArrayList;

import mas.agentsTargaryen.TargaryenAgent;
import massim.javaagents.agents.MarsUtil;
import apltk.interpreter.data.LogicBelief;
import eis.iilang.Action;

/**
 * Survey-Strategy
 */
public class SurveyStrategy implements StrategyIf {

	@Override
	public Action execute(TargaryenAgent agent) {
		if (agent.isDisabled()) {
			return null;
		}
		if (!agent.getAllGoals("testEdge").isEmpty()) {
			if ((!agent.getAllBeliefs("edge", agent.getPosition(), "", "11").isEmpty())
					|| (!agent.getAllBeliefs("edge", "", agent.getPosition(), "11").isEmpty())) {
				return MarsUtil.surveyAction();
			} else {
				ArrayList<String> path = new ArrayList<String>();
				if (!agent.getAllBeliefs("goto", agent.getPosition(), agent.getName()).isEmpty()
						|| agent.getAllBeliefs("goto", "", agent.getName()).isEmpty()) {
					if (!agent.getAllBeliefs("goto", agent.getPosition(), agent.getName()).isEmpty()) {
						agent.removeBeliefs("goto", agent.getPosition(), agent.getName());
					}
					ArrayList<String> ziel = agent.getUtil().getUnknownEdge(agent.getPosition());
					if (!ziel.isEmpty()) {
						if (!ziel.get(0).equals("small")) {

							path = agent.getUtil().getDirection(agent.getPosition(), ziel);
						}
						if (path.get(0).equals(agent.getPosition())) {
							path.remove(0);
						}
						agent.addBelief(new LogicBelief("goto", path.get(path.size() - 1), agent.getName()));
						if (path.size() > 0 && agent.getUtil().getNeighborVertexes(agent.getPosition()).contains(path.get(0))) {
							return MarsUtil.gotoAction(path.get(0));
						}
					} else {
						agent.removeBeliefs("goto", "", agent.getName());
					}
				} else if (!agent.getAllBeliefs("goto", "", agent.getName()).isEmpty()) {
					if (path.size() > 0) {
						if (path.get(0).equals(agent.getPosition())) {
							path.remove(0);
						}
					}
					if (path.size() > 0 && !path.get(0).equals("test")
							&& agent.getUtil().getNeighborVertexes(agent.getPosition()).contains(path.get(0))) {
						return MarsUtil.gotoAction(path.get(0));
					} else {
						return new RandomWalkStrategy().execute(agent);
					}
				}
			}
		}
		return null;
	}
}