package mas.agents0815.rules;

import java.util.Collection;
import java.util.LinkedList;

import mas.agents0815.Const;
import mas.agents0815.InternalAction;
import mas.agents0815.SubsumptionAgent;
import massim.javaagents.agents.MarsUtil;
import apltk.interpreter.data.LogicBelief;
import apltk.interpreter.data.LogicGoal;
import eis.iilang.Percept;

public class RuleProbeInZone extends Rule{

	public boolean fire(Collection<Percept> percepts, Collection<LogicBelief> beliefs,Collection<LogicGoal> goals, SubsumptionAgent agent){
		
		LinkedList<LogicBelief> zone = agent.getAllBeliefs(Const.ZONE);
		LinkedList<LogicBelief> innerZone = agent.getAllBeliefs(Const.INNERZONE);
		LinkedList<String> protectedVertices = new LinkedList<String>();

		//these vertices needs to be probed
		if(!zone.isEmpty() && !innerZone.isEmpty()){
			for (int i=0; i<zone.get(0).getParameters().size();i++)
				protectedVertices.add(zone.get(0).getParameters().get(i));
			for (int i=0; i<innerZone.get(0).getParameters().size();i++)
				protectedVertices.add(innerZone.get(0).getParameters().get(i));
		}//for
		
		if(!zone.isEmpty()){
			for (String b : protectedVertices){
				if (agent.containsBelief(new LogicBelief(Const.UNPROBEDVERTEX,b))){
					if(!agent.containsBelief(new LogicBelief(Const.BLOCK,Const.PROBE, b))){
						setAction(new InternalAction("probeInZone"));
						return true;
					}
				}
			}
		}//for

		return false;
			
	
		}
}
