package mas.agents0815;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import massim.javaagents.agents.MarsUtil;
import apltk.interpreter.data.LogicBelief;
import apltk.interpreter.data.LogicGoal;
import eis.iilang.Percept;

public class BeliefAndGoalsRevision {

	/*********************************************************
	 * gets the current percepts and updates the BeliefBase and GoalBase
	 * Currently just safes the topology of the map
	 * 
	 * @param current
	 *            percepts which to process
	 * @return boolean, identifying a successful call
	 *********************************************************/
	public boolean beliefAndGoalsRevision(Collection<Percept> percepts,
			SubsumptionAgent agent) {


		String vertex1 = "";
		String vertex2 = "";
		String oldPos = "";
		String curPos = "";
		String oldEnergy = "";
		String curEnergy = "";
		String curHealth = "";
		String status = "";
		String lastAction = "";
		String lastActionParam = "";


		LinkedList<LogicBelief> tmp = agent.getAllBeliefs(Const.POSITION);
		if (!tmp.isEmpty())
			oldPos = tmp.get(0).getParameters().get(0).toString();

		tmp = agent.getAllBeliefs(Const.ENERGY);
		if (!tmp.isEmpty()) {
			oldEnergy = tmp.get(0).getParameters().get(0).toString();
		}

		// 1. get current Position and Energy
		for (Percept p : percepts) {

			// update current position
			if (p.getName().equals(Const.POSITION)) {
				curPos = p.getParameters().get(0).toString();
				agent.removeBeliefs(Const.POSITION);
				agent.addBelief(new LogicBelief(Const.POSITION, curPos));
				agent.setMyPos(curPos);
				
				// TeamChat
				agent.broadcastBelief(new LogicBelief(Const.ALLIEDPOSITION,
						agent.getMyName(), curPos));
				// debugging
				System.out.println("My Position is " + curPos);

				// catch the case, that its position is not yet known to
				// the agent
				if (!agent.containsBelief(new LogicBelief(Const.UNPROBEDVERTEX,
						curPos))) {
					Collection<LogicBelief> probed = agent
							.getAllBeliefs(Const.PROBEDVERTEX);
					boolean foundPos = false;
					for (LogicBelief b : probed) {
						if (b.getParameters().get(0).equals(curPos))
							foundPos = true;
					}// for
					if (!foundPos) {
						agent.addBelief(new LogicBelief(Const.UNPROBEDVERTEX,
								curPos));
						System.out.println("Hi, Iam new here");
					}// if
				}// if
			}// else if

			// update energy level
			else if (p.getName().equals(Const.ENERGY)) {
				Integer energy = new Integer(p.getParameters().get(0)
						.toString());
				curEnergy = energy.toString();
				// System.out.println("my current energy is " + curEnergy);
				agent.removeBeliefs(Const.ENERGY);
				ArrayList<String> params = new ArrayList<String>();
				agent.setMyEnergy(energy);
				params.add(curEnergy);
				agent.addBelief(new LogicBelief(Const.ENERGY, params));
				// TeamChat
				agent.broadcastBelief(new LogicBelief(Const.ALLIEDENERGY, agent
						.getMyName(), curEnergy));
			}// else if
			
			// get the agents health
			else if (p.getName().equals(Const.HEALTH)) {
				Integer health = new Integer(p.getParameters().get(0)
						.toString());
				curHealth = health.toString();
				agent.removeBeliefs(Const.HEALTH);
				agent.setMyHealth(health);
				agent.addBelief(new LogicBelief(Const.HEALTH, curHealth));
				// TeamChat
				agent.broadcastBelief(new LogicBelief(Const.ALLIEDHEALTH, agent
						.getMyName(), curHealth));
			}// else if

			// get the agents last action
			else if (p.getName().equals(Const.LASTACTION)) {
				lastAction = p.getParameters().get(0).toString();
				if (p.getParameters().size() > 1) {
					lastActionParam = p.getParameters().get(1).toString();
				}
				agent.setMyLastAction(lastAction);
			}

			//update other relevant information..
			else if (p.getName().equals(Const.MAXENERGY)) {
				agent.setMyMaxEnergy(new Integer(p.getParameters().get(0)
						.toString()));
			}
			else if (p.getName().equals(Const.MAXENERGYDISABLED)) {
				agent.setMyMaxEnergyDisabled(new Integer(p.getParameters().get(0)
						.toString()));
			}
			else if (p.getName().equals(Const.MAXHEALTH)) {
				agent.setMyMaxHealth(new Integer(p.getParameters().get(0)
						.toString()));
			}
			else if (p.getName().equals(Const.STRENGTH)) {
				agent.setMyStrength(new Integer(p.getParameters().get(0)
						.toString()));
			}
			else if (p.getName().equals(Const.MONEY)) {
				agent.setMoney(new Integer(p.getParameters().get(0)
						.toString()));// - agent.getOtherBuys()*10);
			}

			// get the outcome of the agents last action
			else if (p.getName().equals(Const.LASTACTIONRESULT)) {
				status = p.getParameters().get(0).toString();
				agent.setMyLastActionResult(status);
			}
		}// for

		// 2. process the topology percepts and the entitity percepts

		// forget my history
		agent.removeBeliefs(Const.NEIGHBOR);
		agent.removeBeliefs(Const.VISIBLEENEMY);
		agent.removeBeliefs(Const.ENEMYINZONE);
		agent.removeBeliefs(Const.VISIBLEENTITY);

		for (Percept p : percepts) {

			// check whether unknown edges can be seen
			// TODO not necessary if agent has not moved!
			if (p.getName().equals(Const.VISIBLEEDGE)) {

				vertex1 = p.getParameters().get(0).toString();
				vertex2 = p.getParameters().get(1).toString();
				// check whether we have found a direct neighbor
				// this is saved for more convenient further processing
				if (vertex1.equals(curPos)) {
					agent.addBelief(new LogicBelief(Const.NEIGHBOR, vertex2));
				}
				if (vertex2.equals(curPos)) {
					agent.addBelief(new LogicBelief(Const.NEIGHBOR, vertex1));
				}

				if (isUnknownEdge(vertex1, vertex2, agent)) {
					// new Edge has been discovered;

					// System.out.println("I perceive an unknown edge from " +
					// vertex1 +"to " + vertex2);

					// quick method for copying percept parameters
					p.setName(Const.UNEXPLOREDEDGE);
					LogicBelief b = MarsUtil.perceptToBelief(p);
					agent.addBelief(b);
					// TeamChat
					agent.broadcastBelief(b);

					// at least one vertex must be unknown to the agent
					// find the nodes which are unknown to the agent
					// and add it to BeliefBase
					String v1 = "";
					LinkedList<LogicBelief> tmp2 = agent
							.getAllBeliefs(Const.UNPROBEDVERTEX);
					tmp2.addAll(agent.getAllBeliefs(Const.PROBEDVERTEX));
					boolean vertex1known = false;
					boolean vertex2known = false;

					for (LogicBelief b2 : tmp2) {
						v1 = b2.getParameters().get(0).toString();
						if (v1.equals(vertex1))
							vertex1known = true;
						else if (v1.equals(vertex2))
							vertex2known = true;
					}
					if (!vertex1known)
						agent.addBelief(new LogicBelief(Const.UNPROBEDVERTEX,
								vertex1));
					// TeamChat
					agent.broadcastBelief(new LogicBelief(Const.UNPROBEDVERTEX,
							vertex1));
					if (!vertex2known)
						agent.addBelief(new LogicBelief(Const.UNPROBEDVERTEX,
								vertex2));
					// TeamChat
					agent.broadcastBelief(new LogicBelief(Const.UNPROBEDVERTEX,
							vertex2));

					// System.out.println(newVertex + " is new to me");

				}// if
			}// if

			// update surveyedEdge
			else if (p.getName().equals("surveyedEdge")) {
				p.setName(Const.EXPLOREDEDGE);
				LogicBelief b = MarsUtil.perceptToBelief(p);
				if (agent.containsBelief(b) == false) {

					System.out
							.println("I perceive the weight of an edge that I have not known before");
					agent.addBelief(b);
					// TeamChat
					agent.broadcastBelief(b);

					// Delete the corresponding unknownEdge
					vertex1 = b.getParameters().get(0).toString();
					vertex2 = b.getParameters().get(1).toString();

					if (agent.containsBelief(new LogicBelief(
							Const.UNEXPLOREDEDGE, vertex1, vertex2))) {
						agent.removeBelief(new LogicBelief(
								Const.UNEXPLOREDEDGE, vertex1, vertex2));
					} else if (agent.containsBelief(new LogicBelief(
							Const.UNEXPLOREDEDGE, vertex2, vertex1))) {
						agent.removeBelief(new LogicBelief(
								Const.UNEXPLOREDEDGE, vertex2, vertex1));
					}
				}
			}// else if

			// update probed vertices
			else if (p.getName().equals(Const.PROBEDVERTEX)) {
				LogicBelief b = MarsUtil.perceptToBelief(p);

				// did I probe successfully?
				if (status.equals(Const.SUCCESSFUL)
						&& lastAction.equals(Const.PROBE)) {
					System.out.println(p.getParameters().get(0).toString());
					System.out.println(p.getParameters().get(1).toString());
					
					// delete unprobedVertex
					if (agent.containsBelief(new LogicBelief(
							Const.UNPROBEDVERTEX, curPos))) {
						// TODO debugging
						if (!b.getParameters().get(0).equals(curPos)) {
							System.out
									.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!BUG: curPos != probed");
						}
						agent.removeBelief(new LogicBelief(
								Const.UNPROBEDVERTEX, curPos));
					}// if
					// add to BeliefBase
					agent.addBelief(b);
					// TeamChat
					agent.broadcastBelief(b);
				}// if

			}// if

			// get the current step
			else if (p.getName().equals(Const.STEP))
				agent.setStep(Integer.parseInt((p.getParameters().getFirst()
						.toString())));

			// get visible enemies
			else if (p.getName().equals(Const.VISIBLEENTITY)) {
				// check if it is an enemy
				if (!p.getParameters().get(2).toString().equals(agent.getMyTeam())) {
					LogicBelief enemy = new LogicBelief(Const.VISIBLEENEMY, p
							.getParameters().get(0).toString(), p
							.getParameters().get(1).toString(), p
							.getParameters().get(3).toString());
					agent.addBelief(enemy);
					agent.broadcastBelief(enemy);
				}// if
			}// else
			

		}// for

		
		//is an enemy endangering our zone?
		LinkedList<LogicBelief> zone = agent.getAllBeliefs(Const.ZONE);
		LinkedList<LogicBelief> innerZone = agent.getAllBeliefs(Const.INNERZONE);
		LinkedList<String> protectedVertices = new LinkedList<String>();
		//these vertices needs protection
		if(!zone.isEmpty() && !innerZone.isEmpty()){
			for (int i=0; i<zone.get(0).getParameters().size();i++)
				protectedVertices.add(zone.get(0).getParameters().get(i));
			for (int i=0; i<innerZone.get(0).getParameters().size();i++)
				protectedVertices.add(innerZone.get(0).getParameters().get(i));
		
			//is an enemy standing on one of these?
			LinkedList<LogicBelief> visEnemy = agent.getAllBeliefs(Const.VISIBLEENEMY);
			if (!zone.isEmpty()){
				for(LogicBelief en: visEnemy){
					if (protectedVertices.contains(en.getParameters().get(1))){
						if (en.getParameters().get(2).equals(Const.NORMAL)){
							System.out.println("CHASE HIM DOWN");
							agent.broadcastBelief(new LogicBelief(Const.ENEMYINZONE,en.getParameters().get(1)));
							agent.addBelief(new LogicBelief(Const.ENEMYINZONE,en.getParameters().get(1)));
						}
				}
			}
		}
		}
 		
		// if the agent has moved, calculate and safe the weight of the last
		// edge
/*
		if (!curPos.equals(oldPos) && !oldPos.equals("") && !curPos.equals("")) {
			// get the used energy
			String weight = String.valueOf(Integer.valueOf(oldEnergy)
					- Integer.valueOf(curEnergy));

			// bring parameter in the correct order
			if (agent.containsBelief(new LogicBelief(Const.UNEXPLOREDEDGE,
					oldPos, curPos))) {
				// Delete the corresponding unknownEdge
				agent.removeBelief(new LogicBelief(Const.UNEXPLOREDEDGE,
						oldPos, curPos));
				if (!agent.containsBelief(new LogicBelief(Const.EXPLOREDEDGE, curPos,
						oldPos, weight)))
					agent.addBelief(new LogicBelief(Const.EXPLOREDEDGE, oldPos,
							curPos, weight));
				// TeamChat
				agent.broadcastBelief(new LogicBelief(Const.EXPLOREDEDGE,
						oldPos, curPos, weight));
			}// if
			else {
				// Delete the corresponding unknownEdge
				agent.removeBelief(new LogicBelief(Const.UNEXPLOREDEDGE,
						curPos, oldPos));
				if (!agent.containsBelief(new LogicBelief(Const.EXPLOREDEDGE, curPos,
						oldPos, weight)))
						agent.addBelief(new LogicBelief(Const.EXPLOREDEDGE, curPos,
								oldPos, weight));
				// TeamChat
				agent.broadcastBelief(new LogicBelief(Const.EXPLOREDEDGE,
						curPos, oldPos, weight));
			}// else 

		}// if*/

		// 3.STEP
		// Register the initOrder of all agents, if we don't already have and
		// broadcast what is our role
		if (agent.getAllBeliefs(Const.INITIALISATION).getFirst().getParameters().get(
				9).equals(Const.EMPTY)) {
			agent.broadcastBelief(new LogicBelief(Const.ALLIEDROLE, agent.getMyName(), agent
					.getMyRole()));
			Integer idx = 0;
			for (String s : agent.getAllBeliefs(Const.INITIALISATION).getFirst()
					.getParameters()) {
				if (s.equals(Const.EMPTY)) {
					agent.getAllBeliefs(Const.INITIALISATION).getFirst().getParameters().set(idx, agent.getMyName());				
					agent.broadcastBelief(new LogicBelief(Const.INITAGENT, idx
							.toString()));
					break;
				}
				idx++;
			}
		}
		
		// 4. STEP
		// Check whether the agent can unblock actions
		if (lastAction.equals(Const.PROBE))
			if (status.equals(Const.SUCCESSFUL))
				agent.broadcastBelief(new LogicBelief("UNBLOCK", Const.PROBE,
						lastActionParam));
		if (lastAction.equals(Const.SURVEY))
			if (status.equals(Const.SUCCESSFUL))
				agent.broadcastBelief(new LogicBelief("UNBLOCK", Const.SURVEY,
						lastActionParam));
		
		//5. Step
		//Can we delete a goal?
		if (status.equals(Const.SUCCESSFUL)&& !lastAction.equals(Const.RECHARGE)&& !lastAction.equals("skip")){
			LinkedList<LogicGoal> goals = (LinkedList<LogicGoal>)agent.getGoalBase();
			if (!goals.isEmpty()){
				agent.removeLastGoal();
			}
		}
		//don not do silly things
		if (status.equals("wrongParameter"))
			agent.clearGoals();
	
		// debugging
		Collection<LogicBelief> probed = agent
				.getAllBeliefs(Const.PROBEDVERTEX);
		System.out.println("I have probed " + probed.size() + " vertices");

		// TODO when can this function return false?
		return true;
	}// beliefAndGoalsRevision

	/*********************************************************
	 * @brief checks wether the agent already know the edge
	 * @param 2 vertices of an Edge
	 * @return true = Edge is new to the agent, false = Edge is already in
	 *         BeliefBase
	 * @see beliefAndGoalsRevision
	 *********************************************************/
	public boolean isUnknownEdge(String vertex1, String vertex2,
			SubsumptionAgent agent) {
		
		LinkedList<LogicBelief> surveyedEgdes = agent
				.getAllBeliefs(Const.EXPLOREDEDGE);
		LinkedList<LogicBelief> unexploredEdges = agent
				.getAllBeliefs(Const.UNEXPLOREDEDGE);
		String v1 = "";
		String v2 = "";

		// check, if edge can be found in surveyedEdges
		for (LogicBelief b : surveyedEgdes) {
			v1 = b.getParameters().get(0).toString();
			v2 = b.getParameters().get(1).toString();
			if ((v1.equals(vertex1) && v2.equals(vertex2))
					|| (v1.equals(vertex2) && v2.equals(vertex1))) {
				return false;
			}
		}
		// check if edge can be found in unexploredEdges
		for (LogicBelief b : unexploredEdges) {
			v1 = b.getParameters().get(0).toString();
			v2 = b.getParameters().get(1).toString();
			if ((v1.equals(vertex1) && v2.equals(vertex2))
					|| (v1.equals(vertex2) && v2.equals(vertex1))) {
				// edge is already in BeliefBase
				return false;
			}
		}

		return true;
	}
}
