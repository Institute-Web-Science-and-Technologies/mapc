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
 * @author Dennis
 * @brief Fires only with active RepairStrategy which involves this agent
 * 
 * @return if successful the internal action handleRepair and the name of the designated target (if this agent has to move)
 */
public class RuleHandleRepair extends Rule {

	public boolean fire(Collection<Percept> percepts, Collection<LogicBelief> beliefs,Collection<LogicGoal> goals, SubsumptionAgent agent){

		/* 
		 * TODO ENHANCE_DENNIS: Wenn sich der wartende Agent bewegt, sendet er dem anderen sein Ziel,
		 * damit dieser seine findRoute anpassen kann
		 */
		
		
		for (LogicBelief b1: beliefs){
			
			if (b1.getPredicate().equals(Const.HANDLEREPAIR)) {
				
				// Case 1: This agent has to move (agentName is in para0)
				// Need to know which agent has to be reached
				// >> RuleHandleRepair will be fired once or never (if this step is done in initRepair). The work is then taken over by RuleCheckGoals <<
				if (b1.getParameters().get(0).equals(agent.getMyName())){
					
					System.out.println("###DEBUG_DENNIS: Found HandleRepair. I move###");
					setAction(new InternalAction(Const.HANDLEREPAIR, new Identifier(b1.getParameters().get(1))));
					return true;	
				}

				// Case 2: This agent has to wait (agentName is in para1)
				// Do not move, just do recharges for now. If the other agent reached this location, repair him
				// >> RuleHandleRepair will be fired every round until the other agent reaches his position and is repaired. <<
				if (b1.getParameters().get(1).equals(agent.getMyName())){
					
					System.out.println("###DEBUG_DENNIS: Found HandleRepair. I wait###");
					agent.clearGoals(); //TODO Necessary??
					setAction(MarsUtil.rechargeAction());
					return true;					
				}			
			}
		}	
		return false;
	}
}
