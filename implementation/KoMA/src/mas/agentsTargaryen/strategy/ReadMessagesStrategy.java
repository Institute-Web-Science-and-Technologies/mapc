package mas.agentsTargaryen.strategy;

import mas.agentsTargaryen.TargaryenAgent;
import apltk.interpreter.data.LogicBelief;
import apltk.interpreter.data.LogicGoal;
import apltk.interpreter.data.Message;
import eis.iilang.Action;

/**
 * Nachrichten abrufen und einarbeiten
 */
public class ReadMessagesStrategy implements StrategyIf {

	@Override
	public Action execute(TargaryenAgent agent) {
		boolean sendRepairComing = false;
		LogicBelief repairAllyBelief = null;
		for (Message m : agent.retrieveMessages()) {
			LogicBelief b = (LogicBelief) m.value;

			if (b.getPredicate().equals("vertex")) {
				if (agent.getAllBeliefs("vertex", b.getParameters().get(0)).isEmpty()) {
					agent.addBelief(new LogicBelief("vertex", b.getParameters().get(0).toString(), "-1"));
				}
				if (agent.getAllBeliefs("vertex", b.getParameters().get(1)).isEmpty()) {
					agent.addBelief(new LogicBelief("vertex", b.getParameters().get(1).toString(), "-1"));
				}
			} else if (b.getPredicate().equals("edge")) {
				if (agent.getAllBeliefs("edge", b.getParameters().get(0).toString(), b.getParameters().get(1).toString()).isEmpty()
						&& agent.getAllBeliefs("edge", b.getParameters().get(1).toString(), b.getParameters().get(0).toString()).isEmpty()) {
					agent.addBelief(new LogicBelief("edge", b.getParameters().get(0).toString(), b.getParameters().get(1).toString(), "11"));
				}
			} else if (b.getPredicate().equals("goto")) {
				if (agent.getAllBeliefs("goto", "", b.getParameters().get(1).toString()).isEmpty()) {
					agent.removeBeliefs("goto", "", b.getParameters().get(1).toString());
				}

				agent.addBelief(new LogicBelief("goto", b.getParameters().get(0).toString(), b.getParameters().get(1).toString(), b
						.getParameters().get(2).toString()));

			} else if (b.getPredicate().equals("inspectedEntity")) {
				agent.addBelief(b);
			} else if (b.getPredicate().equals("zonePoint")) {
				agent.removeBeliefs("zonePoint");
				agent.addBelief(b);
			} else if (b.getPredicate().equals("wardAgent")) {
				agent.removeBeliefs("wardAgent");
				agent.addBelief(b);
			} else if (b.getPredicate().equals("wardDone")) {
				agent.removeBeliefs("wardDone");
				agent.addBelief(b);
			} else if (b.getPredicate().equals("needRepair")) {
				if (agent.getRole().equals("Repairer")) {
					if (agent.getAllGoals("repairAlly").isEmpty()) {
						agent.broadcastBelief(new LogicBelief("gotoRepair", b.getParameters().get(0), agent.getName()));
						sendRepairComing = true;
						repairAllyBelief = b;
					}
				}
			} else if (b.getPredicate().equals("gotoRepair")) {
				sendRepairComing = false;
				agent.deleteGoals("repairAlly");
			} else if (b.getPredicate().equals("repairComing")) {
				agent.removeBeliefs("repairComing");
				agent.addBelief(b);
			} else if (b.getPredicate().equals("position")) {
				agent.removeBeliefs("position", "", b.getParameters().get(1));
				agent.addBelief(b);
			}
		}
		if (sendRepairComing) {
			agent.addGoal(new LogicGoal("repairAlly", repairAllyBelief.getParameters().get(0), repairAllyBelief.getParameters().get(1)));
			agent.sendMessage(new LogicBelief("repairComing", agent.getName()), repairAllyBelief.getParameters().get(0));
		} else {
			agent.deleteGoals("repairAlly");
		}
		return null;
	}
}