package mas.agents0815.rules;

import java.util.Collection;
import java.util.LinkedList;

import mas.agents0815.Const;
import mas.agents0815.InternalAction;
import mas.agents0815.SubsumptionAgent;
import massim.javaagents.agents.MarsUtil;
import apltk.interpreter.data.LogicBelief;
import apltk.interpreter.data.LogicGoal;
import eis.iilang.Identifier;
import eis.iilang.Percept;

/**
 * @author Dennis J.
 * @brief checks if there is an agent which needs repair
 * @param current beliefs
 * @return method call for DoInitRepair, returns also the name of the damaged agent (as String)
 */
public class RuleInitRepair extends Rule {

	public boolean fire(Collection<Percept> percepts, Collection<LogicBelief> beliefs,Collection<LogicGoal> goals, SubsumptionAgent agent){

		
		//LinkedList<String> alliesAtThisPosition = new LinkedList<String>();
		LinkedList<LogicBelief> agentHealth = agent.getAllBeliefs(Const.ALLIEDHEALTH);
		LinkedList<LogicBelief> alliesPosition = agent.getAllBeliefs(Const.ALLIEDPOSITION);
		String agentName = null;
		
		/*
		for (LogicBelief check : beliefs){
			
			// Get all agents with the same position
			if (check.getPredicate().equals(Const.ALLIEDPOSITION)){
				if (check.getParameters().get(1).equals(agent.getMyPos())){
					agentName = check.getParameters().get(0);
					alliesAtThisPosition.add(agentName);
				}
			}
			
			// Get all known disabled agents
			if (check.getPredicate().equals(Const.ALLIEDHEALTH)){
				if (check.getParameters().get(1).equals(0)){
					
					System.out.println("###DEBUG_DENNIS: Found disabled agent; Name = "+check.getParameters().get(0).toString()+"###");
					agentName = check.getParameters().get(0);
					disabledAgents.add(agentName);
				}
			}
		}*/
		LinkedList<LogicBelief> iHandle = agent.getAllBeliefs(Const.YOUHANDLE);
		if (iHandle.size()>0){
			setAction(new InternalAction(Const.INITREPAIR, new Identifier(iHandle.get(0).getParameters().get(0))));
			return true;
		}
		
		for (LogicBelief b : agentHealth){
			
			// LowLevel behavior: If there is a disabled agent at my position, repair him without further calculations
			// (adding repair order in other methods becomes obsolete)
			if (b.getParameters().get(1).equals("0")){
				//found an disabled agent
				//check if he is at my position
				agentName = b.getParameters().get(0);
				if (alliesPosition.contains(new LogicBelief(Const.ALLIEDPOSITION, agentName, agent.getMyPos()))){
					System.out.println("###DEBUG_DENNIS: Will perform lowLevel repair###");
					setAction(MarsUtil.repairAction(agentName.substring(5).toLowerCase()));
					System.out.println("####DEBUG: I repair "+ agentName);
					return true;
				}//if
				else{
					if (!(beliefs.contains(new LogicBelief(Const.BLOCK, Const.INITREPAIR, agentName)))){
						//Any calculation is useless, if I have not seen the damagedAgent
							setAction(new InternalAction(Const.INITREPAIR,new Identifier(agentName)));
							return true;						
					}
				}//else

			}//if
		}//for
		return false;
	}
}