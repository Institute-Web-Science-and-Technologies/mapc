package mas.agents0815.doIt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import eis.iilang.Action;
import eis.iilang.Identifier;
import mas.agents0815.Const;
import mas.agents0815.HelpFunctions;
import mas.agents0815.InternalAction;
import mas.agents0815.SubsumptionAgent;
import massim.javaagents.agents.MarsUtil;
import apltk.interpreter.data.LogicBelief;
import apltk.interpreter.data.LogicGoal;

/**
 * @author Dennis
 * 
 * limitations:
 * - implemented for only _one_ zone
 *
 */
public class DoInitRepair {
	public Collection<LogicBelief> beliefs;
	public Collection<LogicGoal> goals;
	public String damagedAgent;
	public SubsumptionAgent agent;
	private final HelpFunctions help = new HelpFunctions();
	
	public DoInitRepair(Collection<LogicBelief> beliefs, Collection<LogicGoal> goals, SubsumptionAgent agent, String agentName) {
		super();
		this.damagedAgent = agentName;
		this.beliefs = beliefs;
		this.goals = goals;
		this.agent = agent;
	}

	
	public Action doIt() {

		//ENHANCE_DENNIS: Configure what distance is defined as "near the zone"
		final int MAXDISTANCEFROMZONE = 2;

		//Getting name of the other repairer
		String otherRepairer = null;
		for(LogicBelief l : agent.getAllBeliefs(Const.ALLIEDROLE)){
			if(l.getParameters().get(1).equals(Const.ROLEREPAIRER) && !(l.getParameters().get(0).equals(agent.getMyName()))){
				otherRepairer = l.getParameters().get(0);
			}//if
		}//for
		
		// Get topology informations for the findRoute method
		LinkedList<LogicBelief> edges = new LinkedList<LogicBelief>();
		LinkedList<String> vertices = new LinkedList<String>();
		
		//Positions of agents and the zone
		String positionOtherRepairer =null;
		LinkedList<String> positionDamagedAgent = new LinkedList<String>();
		LinkedList<String> zoneBorder = new LinkedList<String>();
		
		//The routes
		ArrayList<LogicGoal> routeThisRepairer = new ArrayList<LogicGoal>();
		ArrayList<LogicGoal> routeOtherRepairer = new ArrayList<LogicGoal>();
		ArrayList<LogicGoal> routeDamaged = new ArrayList<LogicGoal>();
		
		boolean iRepair = true;
		
		// #############################################################################
		// Get the different beliefs and prepare them
		for (LogicBelief b : beliefs) {

			// If the name is correct (para0), then use their position (para1)
			if (b.getPredicate().equals(Const.ALLIEDPOSITION)) {
				
				if (b.getParameters().get(0).equals(otherRepairer))
					positionOtherRepairer = b.getParameters().get(1).toString();
				if (b.getParameters().get(0).equals(damagedAgent))
					positionDamagedAgent.add(b.getParameters().get(1).toString());
			}//if
			
			// all Edges in a LinkedList
			if (b.getPredicate().equals(Const.EXPLOREDEDGE))
					edges.add(b);
			if (b.getPredicate().equals(Const.UNEXPLOREDEDGE))
					edges.add(b);

			// all Vertices in a LinkedList
			if (b.getPredicate().equals(Const.PROBEDVERTEX))
					vertices.add(b.getParameters().get(0));
			if (b.getPredicate().equals(Const.UNPROBEDVERTEX))
					vertices.add(b.getParameters().get(0));
			
			//Get the outer vertices of our zone (for only one zone)
			if (b.getPredicate().equals(Const.ZONE)) {
					for(int i=1; i<b.getParameters().size();i++)
						zoneBorder.add(b.getParameters().get(i));
				}//if
			}//for

		// Get the different beliefs and prepare them
		// #############################################################################
		
		//DEBUG_DENNIS: If the position of the damaged agent isn't found
		//Should not happen, because if rule fires repairer knows of health (and all the other informations)
		if (positionDamagedAgent.isEmpty()){
			System.out.println("###DEBUG_DENNIS: System didn't found the locations of the damaged agent.");
			return null;
		}//if
				
		
		// #############################################################################
		//SELECT_REPAIRER
		
		routeThisRepairer = help.findRoute(agent.getMyPos(), positionDamagedAgent, vertices,edges, false,200);

		//TODO What if the other repairer has no contact to the damaged agent?
		//TODO The route will then be calculated as the nearest? (size==0)
		
		//Check if the other repairer is available and closer to the damagedAgent
		//If there is a BLOCK that means the other repairer is on a mission
		//precondition: I know his position
		if(!positionOtherRepairer.isEmpty() && !damagedAgent.equals(otherRepairer)){
			if (!agent.containsBelief(new LogicBelief(Const.BLOCK, Const.INITREPAIR))){
				routeOtherRepairer = help.findRoute(positionOtherRepairer, positionDamagedAgent, vertices,edges, false,200);
				
				if ((routeOtherRepairer.size()>0) || samePosition(positionOtherRepairer, positionDamagedAgent)){

					//compare distances
					if ( routeOtherRepairer.size() < routeThisRepairer.size() )
						iRepair=false;
				}//if
			}//if
		}//if
		
		//SELECT_REPAIRER
		// #############################################################################
		
		
		
		// #############################################################################
		// REPAIR_STRATEGY
		
		if (!zoneBorder.isEmpty()){

			// Is the distance too large the damaged agent has to move
			// BEWARE: Only distance damagedAgent-zone is considered, not the actual position of the repairer
			routeDamaged = help.findRoute(positionDamagedAgent.get(0).toString(), zoneBorder, vertices, edges, false,200);


			if (routeDamaged.size() > MAXDISTANCEFROMZONE) {
				// ## SubCaseA: damagedAgent has to move ## //
				
				/*
				Plan: Der kaputte Agent laeuft zur Position des ausgewaehlten Repairers.
				Waehrenddessen ignorieren die beiden Repairer ihn.
				Ist er angekommen, wird er entweder repariert (repairer noch da) oder eine neue
				route wird berechnet
				*/				

				LogicBelief ignoreForNow = new LogicBelief(Const.BLOCK, Const.INITREPAIR, damagedAgent);
				agent.sendMessage(ignoreForNow, otherRepairer);
				agent.addBelief(ignoreForNow);
				
				if (iRepair){
					agent.sendMessage(new LogicBelief(Const.RETURNFORREPAIR, agent.getMyName()), damagedAgent);
				} else {
					agent.sendMessage(new LogicBelief(Const.RETURNFORREPAIR, otherRepairer), damagedAgent);					
				}
				return new InternalAction(Const.RULE_SELECTION_AGAIN);
			}
		}
		
		// REPAIR_STRATEGY		
		// #############################################################################
		
		

		
		// #############################################################################
		// PROCESS_RESULTS
		/*
		 * Cases if a repairer has to move.
		 * If the other repairer is known, a BLOCK is sent.
		 * A handleRepair is saved in the beliefBase (needed if the positions has changed)
		 * 
		 */
		

		// Case1: This repairer moves
		if (iRepair){
			LogicGoal singleGoal=null;
			
			System.out.println(Const.THIS_REPAIRER_MOVES);
			
			//Send a BLOCK if the other repairer is known (else the receiver name is unknown)
			if (!otherRepairer.isEmpty()){
				System.out.println(Const.OTHER_REPAIRER_KNOWN);
				agent.sendMessage(new LogicBelief(Const.BLOCK, Const.INITREPAIR, damagedAgent), otherRepairer);	
			}//if
			else
				System.out.println(Const.OTHER_REPAIRER_UNKNOWN);

			agent.addBelief(new LogicBelief(Const.HANDLEREPAIR, damagedAgent));
			
			for (int i = 0; i < routeThisRepairer.size()-1; i++){
				singleGoal = routeThisRepairer.get(i);
				agent.addGoal(singleGoal);
			}//for

			// Processing the first action (last entry in the list)
			// catch case if no route is needed (already on right position)
			if (routeThisRepairer.size() == 0){
				agent.addBelief(new LogicBelief(Const.IWANTTOREPAIR, damagedAgent));
				return MarsUtil.repairAction(damagedAgent.substring(5).toLowerCase());
			}
			singleGoal = routeThisRepairer.get(routeThisRepairer.size()-1);
			return new Action(singleGoal.getPredicate(), new Identifier(singleGoal.getParameters().get(0)));
		}//if
		
		// Case2: The other repairer should do it
		else{
			
			System.out.println(Const.OTHER_REPAIRER_HANDLES);
			agent.sendMessage(new LogicBelief(Const.HANDLEREPAIR, damagedAgent), otherRepairer);
			agent.addBelief(new LogicBelief(Const.BLOCK, Const.INITREPAIR, damagedAgent));
			return new InternalAction(Const.RULE_SELECTION_AGAIN);
			}
			
		}//else
		// PROCESS_RESULTS
		// #############################################################################
	
	
	//Catch problem that findRoute returns null if there is no route
	public boolean samePosition(String posRep, LinkedList<String> posDamaged){
		
		if (posRep.equals(posDamaged.get(0)))
			return true;		
		return false;
	}


}//class