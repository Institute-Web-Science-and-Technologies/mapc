package mas.agents0815.rules;

import java.util.Collection;

import mas.agents0815.Const;
import mas.agents0815.InternalAction;
import mas.agents0815.SubsumptionAgent;
import apltk.interpreter.data.LogicBelief;
import apltk.interpreter.data.LogicGoal;
import eis.iilang.Identifier;
import eis.iilang.Percept;

public class RuleJoinTwoPartyZone extends Rule{

	@Override
	public boolean fire(Collection<Percept> percepts,
			Collection<LogicBelief> beliefs, Collection<LogicGoal> goals,
			SubsumptionAgent agent) {
		String positionToGo="";
		if(agent.getAllBeliefs(Const.MEETINGPOINT).get(0)==null){
		positionToGo = agent.getAllBeliefs(Const.MEETINGPOINT).get(0).toString();
		}
		if(!positionToGo.equals("")){
			setAction(new InternalAction(Const.JOINTWOPARTYZONE, new Identifier(positionToGo)));
			return true;
		}
		// TODO Auto-generated method stub
		return false;
	}

}
