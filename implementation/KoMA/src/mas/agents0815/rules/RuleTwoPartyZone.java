package mas.agents0815.rules;

import java.util.Collection;

import mas.agents0815.Const;
import mas.agents0815.InternalAction;
import mas.agents0815.SubsumptionAgent;
import apltk.interpreter.data.LogicBelief;
import apltk.interpreter.data.LogicGoal;
import eis.iilang.Percept;

public class RuleTwoPartyZone extends Rule {

	@Override
	public boolean fire(Collection<Percept> percepts,
			Collection<LogicBelief> beliefs, Collection<LogicGoal> goals,
			SubsumptionAgent agent) {

		// check if i am part of an attackTeam
		for (LogicBelief b : agent.getAllBeliefs(Const.ATTACKTEAM)) {
			if (b.getParameters().get(1).toString().equals(agent.getMyName())
					|| b.getParameters().get(1).toString().equals(
							agent.getMyName())) {
				setAction(new InternalAction(Const.JOINTWOPARTYZONE));
				return true;
			}// if
		}// for
		return false;
	}
}
