package mas.agents0815.rules;

import java.util.Collection;

import mas.agents0815.Const;
import mas.agents0815.InternalAction;
import mas.agents0815.SubsumptionAgent;
import apltk.interpreter.data.LogicBelief;
import apltk.interpreter.data.LogicGoal;
import eis.iilang.Percept;

public class RuleInitAttackTeam extends Rule {

	@Override
	public boolean fire(Collection<Percept> percepts,
			Collection<LogicBelief> beliefs, Collection<LogicGoal> goals,
			SubsumptionAgent agent) {

		// Check which index for our Attackteam should be used
		boolean exists = agent.getAllBeliefs(Const.ATTACKTEAM).size() != 0;
		Integer i = 0;
		if (exists)
			i = 1;
		// Check if i have seen an Inspector
		for (LogicBelief seen : agent.getAllBeliefs(Const.SEEN)) {
			if (agent.getMyRole().equals(Const.ROLESABOTEUR)) {
				for (LogicBelief role : agent.getAllBeliefs(Const.ALLIEDROLE)) {
					if (role.getParameters().get(0)
							.equals(seen.getParameters().get(0))) {
						if (role.getParameters().get(1).equals(Const.ROLEINSPECTOR)) {
							String first = "", second = "";
							for (String s : agent
									.getAllBeliefs(Const.INITIALISATION)
									.getFirst().getParameters()) {
								if (s.equals(agent.getMyName())
										|| s.equals(seen.getParameters().get(0))) {
									if (first.equals("")) {
										first = s;
										continue;
									}
									second = s;
									break;
								}
							}
							String partner;
							if(first.equals(agent.getMyName()))
								partner = second;
							else
								partner = first;
							agent.addBelief(new LogicBelief(Const.MYATTACKTEAM,
									i.toString(), partner));
							agent.addBelief(new LogicBelief(Const.ATTACKTEAM, i
									.toString(), first, second));
							agent.broadcastBelief(new LogicBelief(
									Const.ATTACKTEAM, i.toString(), first,
									second));
							setAction(new InternalAction(Const.INITTWOPARTYZONE));
							return true;
						}
						break;
					}

				}//for
			}//if
			else{
				for (LogicBelief role : agent.getAllBeliefs(Const.ALLIEDROLE)) {
					if (role.getParameters().get(0)
							.equals(seen.getParameters().get(0))) {
						if (role.getParameters().get(1).equals(Const.ROLESABOTEUR)) {
							String first = "", second = "";
							for (String s : agent
									.getAllBeliefs(Const.INITIALISATION)
									.getFirst().getParameters()) {
								if (s.equals(agent.getMyName())
										|| s.equals(seen.getParameters().get(0))) {
									if (first.equals("")) {
										first = s;
										continue;
									}
									second = s;
									break;
								}
							}
							String partner;
							if(first.equals(agent.getMyName()))
								partner = second;
							else
								partner = first;
							agent.addBelief(new LogicBelief(Const.MYATTACKTEAM,
									i.toString(),partner));
							agent.addBelief(new LogicBelief(Const.ATTACKTEAM, i
									.toString(), first, second));
							agent.broadcastBelief(new LogicBelief(
									Const.ATTACKTEAM, i.toString(), first,
									second));
							setAction(new InternalAction(Const.INITTWOPARTYZONE));
							return true;
						}
						break;
					}

				}//for
			}
		}//for
		return false;
	}
}
