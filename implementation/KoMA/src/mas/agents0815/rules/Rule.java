// *******************************************************
// Rule.java
//
// Contains the abstract data type Rule to implement specific rules
// -------------------------------------------------------
// AUTHOR: Florian
// changed: 23.05.2010
// *******************************************************

package mas.agents0815.rules;

import java.util.Collection;

import mas.agents0815.SubsumptionAgent;

import apltk.interpreter.data.LogicBelief;
import apltk.interpreter.data.LogicGoal;

import eis.iilang.Action;
import eis.iilang.Percept;

public abstract class Rule {
	
	private Action action;
	
/*********************************************************
@brief the respective Rule fires, if necessary percepts, beliefs and goals can be found within in the parameters
@param current percepts, beliefs and goals with which to check if the rule fires
@return yes, if rule fires, no, if rule fires not
*********************************************************/
	public abstract boolean fire(Collection<Percept> percepts, Collection<LogicBelief> beliefs,Collection<LogicGoal> goals, SubsumptionAgent agent);
	
	/*********************************************************
	@brief set- Function
	@param Action which should be assigned
	@return -
	*********************************************************/
	protected void setAction(Action action){
		this.action = action;
	}

	/*********************************************************
	@brief get Function
	@param -
	@return Action parameter of the class
	*********************************************************/
	public Action getAction() {
		return action;
	}
	
	
	

}
