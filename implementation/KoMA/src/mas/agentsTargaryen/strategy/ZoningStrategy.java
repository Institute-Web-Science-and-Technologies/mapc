package mas.agentsTargaryen.strategy;

import java.util.ArrayList;
import java.util.Collections;

import mas.agentsTargaryen.TargaryenAgent;
import massim.javaagents.agents.MarsUtil;
import apltk.interpreter.data.LogicBelief;
import apltk.interpreter.data.LogicGoal;
import eis.iilang.Action;

/**
 * Zoning-Strategy
 */
public class ZoningStrategy implements StrategyIf {

	@Override
	public Action execute(TargaryenAgent agent) {
		// Zur Zone laufen
		if (!agent.getAllGoals("zoning").isEmpty()) {
			if (agent.getAllBeliefs("zonePoint").isEmpty()) {
				String zonePoint = agent.getPosition();
				for (LogicBelief b : agent.getAllBeliefs("vertex")) {
					if (Integer.valueOf(b.getParameters().get(1)) >= 9) {
						zonePoint = b.getParameters().get(0);
						break;
					}
				}
				LogicBelief b = new LogicBelief("zonePoint", zonePoint);
				agent.addBelief(b);
				agent.broadcastBelief(b);
			}
			ArrayList<String> ziel = new ArrayList<String>();
			ziel.add(agent.getAllBeliefs("zonePoint").get(0).getParameters().get(0));
			ArrayList<String> path = new ArrayList<String>();
			path = agent.getUtil().getDirection(agent.getPosition(), ziel);
			if (path.size() > 0 && path.get(0).equals(agent.getPosition())) {
				path.remove(0);
				agent.deleteGoals("zoning");
				agent.addGoal(new LogicGoal("zoning2"));
			}
			if (path.size() > 0 && agent.getUtil().getNeighborVertexes(agent.getPosition()).contains(path.get(0))) {
				return MarsUtil.gotoAction(path.get(0));
			} else {
				agent.addGoal(new LogicGoal("testEdge"));
			}
		}

		// Zone halten/erweitern
		if (!agent.getAllGoals("zoning2").isEmpty()) {
			if ((!agent.getAllBeliefs("zoneScore").isEmpty())
					&& (Integer.parseInt(agent.getAllBeliefs("zoneScore").get(0).getParameters().get(0)) >= 100)) {
				return new SkipStrategy().execute(agent);
			} else {
				// wenn letzter zug doof war, geh zurueck!
				if ((!agent.getAllBeliefs("oldZoneScore").isEmpty())
						&& (agent.getZoneScore() < Integer.parseInt(agent.getAllBeliefs("oldZoneScore").get(0).getParameters().get(0)))) {
					if (agent.getPosition().equals(agent.getAllBeliefs("oldPosition").get(0).getParameters().get(0))) {
						agent.removeBeliefs("oldPosition");
						agent.removeBeliefs("oldZoneScore");
						agent.addBelief(new LogicBelief("oldPosition", agent.getPosition()));
						agent.addBelief(new LogicBelief("oldZoneScore", String.valueOf(agent.getZoneScore())));
						ArrayList<String> neighbors = agent.getUtil().getNeighborVertexes(agent.getPosition());
						Collections.shuffle(neighbors);
						for (int n = 0; n < neighbors.size() - 1; n++) {
							// Altes zone-Belief loeschen
							if (!agent.getAllBeliefs("zone", agent.getPosition(), agent.getName()).isEmpty()
									|| agent.getAllBeliefs("zone", "", agent.getName()).isEmpty()) {
								if (!agent.getAllBeliefs("zone", agent.getPosition(), agent.getName()).isEmpty()) {
									agent.removeBeliefs("zone", agent.getPosition(), agent.getName());
								}
							}
							if (!agent.getAllBeliefs("zone", neighbors.get(n)).isEmpty()) {
								continue;
							}
							agent.addBelief(new LogicBelief("zone", neighbors.get(n), agent.getName()));
							return MarsUtil.gotoAction(neighbors.get(n));
						}
					} else {
						System.out.println("oldposi");
						return MarsUtil.gotoAction(agent.getAllBeliefs("oldPosition").get(0).getParameters().get(0));
					}
				} else {
					// randomwalk. mal sehn ob sich die zonescore verbessert
					agent.removeBeliefs("oldPosition");
					agent.removeBeliefs("oldZoneScore");
					agent.addBelief(new LogicBelief("oldPosition", agent.getPosition()));
					agent.addBelief(new LogicBelief("oldZoneScore", String.valueOf(agent.getZoneScore())));
					ArrayList<String> neighbors = agent.getUtil().getNeighborVertexes(agent.getPosition());
					Collections.shuffle(neighbors);
					for (int n = 0; n < neighbors.size() - 1; n++) {
						// Altes zone-Belief loeschen
						if (!agent.getAllBeliefs("zone", agent.getPosition(), agent.getName()).isEmpty()
								|| agent.getAllBeliefs("zone", "", agent.getName()).isEmpty()) {
							if (!agent.getAllBeliefs("zone", agent.getPosition(), agent.getName()).isEmpty()) {
								agent.removeBeliefs("zone", agent.getPosition(), agent.getName());
							}
						}
						if (!agent.getAllBeliefs("zone", neighbors.get(n)).isEmpty()) {
							continue;
						}
						agent.addBelief(new LogicBelief("zone", neighbors.get(n), agent.getName()));
						return MarsUtil.gotoAction(neighbors.get(n));
					}
					return MarsUtil.gotoAction(neighbors.get(0));
				}
			}
		}
		return null;
	}
}