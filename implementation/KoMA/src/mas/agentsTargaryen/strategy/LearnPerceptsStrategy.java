package mas.agentsTargaryen.strategy;

import mas.agentsTargaryen.TargaryenAgent;
import apltk.interpreter.data.LogicBelief;
import eis.iilang.Action;
import eis.iilang.Percept;

/**
 * Percepts verarbeiten
 */
public class LearnPerceptsStrategy implements StrategyIf {

	@Override
	public Action execute(TargaryenAgent agent) {
		// Alles vergessen was letzte Runde sichtbar war
		agent.removeBeliefs("visibleEntity");

		// Percepts einarbeiten
		for (Percept p : agent.retrieveAllPercepts()) {
			if (p.getName().equals("position")) {
				agent.removeBeliefs("position", "", agent.getName());
				LogicBelief newBelief = new LogicBelief("position", p.getParameters().get(0).toString(), agent.getName(), agent.getRole());
				agent.addBelief(newBelief);
				agent.broadcastBelief(newBelief);
				if (agent.getAllBeliefs("vertex", p.getParameters().getFirst().toString()).isEmpty()) {
					agent.addBelief(new LogicBelief("vertex", p.getParameters().getFirst().toString(), "-1"));
				}
			} else if (p.getName().equals("visibleEdge")) {
				if (agent.getAllBeliefs("edge", p.getParameters().getFirst().toString(), p.getParameters().getLast().toString()).isEmpty()
						&& agent.getAllBeliefs("edge", p.getParameters().getLast().toString(), p.getParameters().getFirst().toString())
								.isEmpty()) {
					LogicBelief newBelief = new LogicBelief("edge", p.getParameters().get(0).toString(), p.getParameters().get(1)
							.toString(), "11");
					agent.addBelief(newBelief);
					// broadcastBelief(newBelief);
				}
			} else if (p.getName().equals("money")) {
				agent.removeBeliefs("money");
				agent.addBelief(new LogicBelief("money", p.getParameters().get(0).toString()));
			} else if (p.getName().equals("steps")) {
				agent.removeBeliefs("totalSteps");
				agent.addBelief(new LogicBelief("totalSteps", p.getParameters().get(0).toString()));
			} else if (p.getName().equals("edges")) {
				agent.removeBeliefs("totalEdges");
				agent.addBelief(new LogicBelief("totalEdges", p.getParameters().get(0).toString()));
			} else if (p.getName().equals("vertices")) {
				agent.removeBeliefs("totalVertices");
				agent.addBelief(new LogicBelief("totalVertices", p.getParameters().get(0).toString()));
			} else if (p.getName().equals("strength")) {
				agent.removeBeliefs("strength");
				agent.addBelief(new LogicBelief("strength", p.getParameters().get(0).toString()));
			} else if (p.getName().equals("health")) {
				agent.removeBeliefs("health");
				agent.addBelief(new LogicBelief("health", p.getParameters().get(0).toString()));
			} else if (p.getName().equals("maxHealth")) {
				agent.removeBeliefs("maxHealth");
				agent.addBelief(new LogicBelief("maxHealth", p.getParameters().get(0).toString()));
			} else if (p.getName().equals("energy")) {
				agent.removeBeliefs("energy");
				agent.addBelief(new LogicBelief("energy", p.getParameters().get(0).toString()));
			} else if (p.getName().equals("maxEnergy")) {
				agent.removeBeliefs("maxEnergy");
				agent.addBelief(new LogicBelief("maxEnergy", p.getParameters().get(0).toString()));
			} else if (p.getName().equals("role")) {
				agent.addBelief(new LogicBelief("role", p.getParameters().get(0).toString()));
			} else if (p.getName().equals("visRange")) {
				agent.removeBeliefs("visRange");
				agent.addBelief(new LogicBelief("visRange", p.getParameters().get(0).toString()));
			} else if (p.getName().equals("step")) {
				agent.removeBeliefs("step");
				agent.addBelief(new LogicBelief("step", p.getParameters().get(0).toString()));
			} else if (p.getName().equals("score")) {
				agent.removeBeliefs("score");
				agent.addBelief(new LogicBelief("score", p.getParameters().get(0).toString()));
			} else if (p.getName().equals("zoneScore")) {
				agent.removeBeliefs("zoneScore");
				agent.addBelief(new LogicBelief("zoneScore", p.getParameters().get(0).toString()));
			} else if (p.getName().equals("zonesScore")) {
				agent.removeBeliefs("zonesScore");
				agent.addBelief(new LogicBelief("zonesScore", p.getParameters().get(0).toString()));
			} else if (p.getName().equals("lastStepScore")) {
				agent.removeBeliefs("lastStepScore");
				agent.addBelief(new LogicBelief("lastStepScore", p.getParameters().get(0).toString()));
			} else if (p.getName().equals("probedVertex")) {
				if (!agent.getAllBeliefs("vertex", p.getParameters().get(0).toString()).isEmpty()) {
					agent.updateBelief(agent.getAllBeliefs("vertex", p.getParameters().get(0).toString()).getFirst(), new LogicBelief(
							"vertex", p.getParameters().get(0).toString(), p.getParameters().get(1).toString()));
				}
			} else if (p.getName().equals("visibleEntity")) {
				if (agent.getAllBeliefs("enemyTeam").isEmpty() && !p.getParameters().get(3).equals(agent.getTeam())) {
					LogicBelief belief = new LogicBelief("enemyTeam", p.getParameters().get(3).toString());
					agent.addBelief(belief);
					agent.broadcastBelief(belief);
				}
				agent.addBelief(new LogicBelief("visibleEntity", agent.getName(), p.getParameters().get(0).toString(), p.getParameters()
						.get(1).toString(), p.getParameters().get(2).toString(), p.getParameters().get(3).toString()));
			} else if (p.getName().equals("surveyedEdge")) {
				if (!agent.getAllBeliefs("edge", p.getParameters().get(0).toString(), p.getParameters().get(1).toString()).isEmpty()) {
					agent.updateBelief(agent
							.getAllBeliefs("edge", p.getParameters().get(0).toString(), p.getParameters().get(1).toString()).getFirst(),
							new LogicBelief("edge", p.getParameters().get(0).toString(), p.getParameters().get(1).toString(), p
									.getParameters().get(2).toString()));
				} else {
					agent.addBelief(new LogicBelief("edge", p.getParameters().get(0).toString(), p.getParameters().get(1).toString(), p
							.getParameters().get(2).toString()));
				}
			} else if (p.getName().equals("inspectedEntity")) {
				if (!p.getParameters().get(1).equals(agent.getTeam())) {
					LogicBelief newBelief = new LogicBelief("inspectedEntity", p.getParameters().get(0).toString(), p.getParameters()
							.get(1).toString(), p.getParameters().get(2).toString(), p.getParameters().get(3).toString(), p.getParameters()
							.get(4).toString(), p.getParameters().get(5).toString(), p.getParameters().get(6).toString(), p.getParameters()
							.get(7).toString(), p.getParameters().get(8).toString(), p.getParameters().get(9).toString());
					agent.addBelief(newBelief);
					agent.broadcastBelief(newBelief);
				}
			}
		}
		for (LogicBelief b : agent.getAllBeliefs("edge")) {
			if (agent.getAllBeliefs("vertex", b.getParameters().get(0)).isEmpty()) {
				LogicBelief newBelief = new LogicBelief("vertex", b.getParameters().get(0).toString(), "-1");
				agent.addBelief(newBelief);
				// broadcastBelief(newBelief);
			}
			if (agent.getAllBeliefs("vertex", b.getParameters().get(1)).isEmpty()) {
				LogicBelief newBelief = new LogicBelief("vertex", b.getParameters().get(1).toString(), "-1");
				agent.addBelief(newBelief);
				// broadcastBelief(newBelief);
			}
		}
		return null;
	}
}