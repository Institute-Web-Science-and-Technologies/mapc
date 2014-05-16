package mas.agents0815.doIt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import mas.agents0815.Const;
import mas.agents0815.HelpFunctions;
import mas.agents0815.SubsumptionAgent;
import massim.javaagents.agents.MarsUtil;
import apltk.interpreter.data.LogicBelief;
import apltk.interpreter.data.LogicGoal;
import eis.iilang.Action;

public class DoJoinTwoPartyZone {
	Collection<LogicBelief> beliefs;
	Collection<LogicGoal> goals;

	private final HelpFunctions help = new HelpFunctions();
	
	public DoJoinTwoPartyZone(Collection<LogicBelief> beliefs,
			Collection<LogicGoal> goals) {
		super();
		this.beliefs = beliefs;
		this.goals = goals;
	}

	/*********************************************************
	 * @brief Find an unexplored edge, go there and survey
	 * @param current
	 *            beliefs and goals
	 * @return goto Action to the node from which best to perform survey
	 * @see processAction 
	 ****************************************/
	public Action doIt(SubsumptionAgent agent, String destination) {
		LinkedList<String> destinationList = new LinkedList<String>();
		destinationList.add(destination);
		LinkedList<LogicBelief> edges = new LinkedList<LogicBelief>();
		LinkedList<String> vertices = new LinkedList<String>();
		
		for(LogicBelief l : beliefs){
			// put all known edges in one LinkedList
			if (l.getPredicate().equals(Const.UNEXPLOREDEDGE))
				edges.add(l);

			// put all unknown edges in one LinkedList
			if (l.getPredicate().equals(Const.EXPLOREDEDGE))
				edges.add(l);

			// put all Vertices into a list
			if (l.getPredicate().equals(Const.PROBEDVERTEX)){
				vertices.add(l.getParameters().get(0));
			}

			// put all Vertices into a list
			if (l.getPredicate().equals(Const.UNPROBEDVERTEX)) {
				vertices.add(l.getParameters().get(0));
			}
		}
		
		if(destinationList.size()==0){
			System.out.print("***********Holger: ALTER WIE KANN MAN DIE POS EINES BEKANNTEN NICHT KENNEN???");
			return null;
		}
		ArrayList<LogicGoal> twoPartyRoute = help.findRoute(agent.getMyPos(), destinationList, vertices, edges, false,200);
//		if(twoPartyRoute.size()%2==0){
//			int myMeetingPoint = twoPartyRoute.size()/2;
//			int alliedMeetingPoint = twoPartyRoute.size()/2+2;
//		}
//		else{
//			int myMeetingPoint = twoPartyRoute.size()/2-1;
//			int alliedMeetingPoint = twoPartyRoute.size()/2+1;
//		}
	
		// fill goals
		String dest= "";
		for (int i = 0; i < twoPartyRoute.size() - 1; i++) {
			dest = twoPartyRoute.get(i).getParameters().firstElement()
					.toString();
			agent.addGoal(new LogicGoal(Const.GOTO, dest));
		}

		// return first step of the route
		dest = twoPartyRoute.get(twoPartyRoute.size() - 1).getParameters()
				.firstElement().toString();
		
		agent.addBelief(new LogicBelief(Const.TWOPARTYZONE));
		
		return MarsUtil.gotoAction(dest);
	}

}