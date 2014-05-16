package mas.agentsTargaryen.strategy;

import java.util.ArrayList;
import java.util.Collections;

import mas.agentsTargaryen.TargaryenAgent;
import massim.javaagents.agents.MarsUtil;
import eis.iilang.Action;

/**
 * RandomWalk-Strategy
 */
public class RandomWalkStrategy implements StrategyIf {

	@Override
	public Action execute(TargaryenAgent agent) {
		ArrayList<String> neighbors = agent.getUtil().getNeighborVertexes(agent.getPosition());
		Collections.shuffle(neighbors);
		return MarsUtil.gotoAction(neighbors.get(0));
	}
}