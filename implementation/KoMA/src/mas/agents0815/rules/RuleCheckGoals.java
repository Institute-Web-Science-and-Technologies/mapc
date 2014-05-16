package mas.agents0815.rules;

import java.util.Collection;
import java.util.LinkedList;

import eis.iilang.Percept;

import mas.agents0815.Const;
import mas.agents0815.SubsumptionAgent;
import massim.javaagents.agents.MarsUtil;
import apltk.interpreter.data.LogicBelief;
import apltk.interpreter.data.LogicGoal;

public class RuleCheckGoals extends Rule{
	
	public boolean fire(Collection<Percept> percepts, Collection<LogicBelief> beliefs,Collection<LogicGoal> goals, SubsumptionAgent agent){

	//check if the agent is broken
		if (agent.getMyHealth()==0){
			setAction(MarsUtil.rechargeAction());
			return true;
		}
	//check if the agent has any goals
	if (!goals.isEmpty()){

		//do I need a recharge?
		if (agent.getMyEnergy() < 2) {
			setAction(MarsUtil.rechargeAction());
			return true;
		}
		else{			
		//do the last element of the goal list
		LogicGoal g = agent.getLastGoal();
		if (g.getPredicate().equals(Const.GOTO)){
			//do I have enough energy for the step?
			//do I have enough energy for the step?
			String dest =g.getParameters().get(1);
			String from="";
			String to="";
			int cost=9;
			
			for (LogicBelief l : beliefs) {

				if (l.getPredicate().equals(Const.EXPLOREDEDGE)){
					from=l.getParameters().get(0).toString();
					to=l.getParameters().get(1).toString();
					if(from.equals(agent.getMyPos()) && to.equals(dest)){
						cost = Integer.valueOf(l.getParameters().get(2));
						break;
					}
					if(to.equals(agent.getMyPos()) && from.equals(dest)){
						cost = Integer.valueOf(l.getParameters().get(2));
						break;
					}
				}//if
			}//for
			
			if (agent.getMyEnergy()<cost){
				setAction(MarsUtil.rechargeAction());
				return true;
			}
			else{
				setAction(MarsUtil.gotoAction(dest));
				return true;
			}//else		
		 }//if
		else if (g.getPredicate().equals(Const.SURVEY)){
			setAction(MarsUtil.surveyAction());
			return true;
		 }//if
		else if (g.getPredicate().equals(Const.PROBE)){
			setAction(MarsUtil.probeAction());
			return true;
		 }//if
		else if (g.getPredicate().equals(Const.REPAIR)){
			setAction(MarsUtil.repairAction(g.getParameters().get(1).substring(5).toLowerCase()));
			return true;
		 }//if
		}//else
	}//if
	return false;
	}//fire

}//class
