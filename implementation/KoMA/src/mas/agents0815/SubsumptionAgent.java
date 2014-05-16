// *******************************************************
// mas.agents0815.java
//
// contains the Subsumption agent, main class of the project
// -------------------------------------------------------
// AUTHOR: Florian
// changed: 04.06.2010
// *******************************************************

package mas.agents0815;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import apltk.interpreter.data.LogicBelief;
import apltk.interpreter.data.LogicGoal;
import apltk.interpreter.data.Message;

import eis.iilang.Action;
import eis.iilang.Percept;
import mas.agents0815.doIt.*;
import mas.agents0815.rules.*;
import massim.javaagents.Agent;
import massim.javaagents.agents.MarsUtil;

public abstract class SubsumptionAgent extends Agent {

	private Vector<Rule> relation;
	// TODO can this be replaced?
	final static int ANZANGENTS = 10;
	final static int ZONESTART = 60;

	private String myName;
	private int myID;
	private int step;
	private String myTeam;
	private int myEnergy;
	private int myMaxEnergy;
	private int myMaxEnergyDisabled;
	private int myHealth;
	private int myMaxHealth;
	private int myStrength;
	private String myPos;
	private String myLastActionResult;
	private String myLastAction;
	private HelpFunctions helpFunctions = new HelpFunctions();
	private BeliefAndGoalsRevision beliefAndGoalsRevision = new BeliefAndGoalsRevision();
	private int unseenAgentsInt;
	private int money;
	
	private int russianCounter = 0;

	/**
	 * A Value of -2 indicates no connectivity. A Value of -1 indicates an
	 * unknown edge weight. A Value >= 0 indicates the true edge weight.
	 */
	private Map<FromTo, Integer> directEdges;

	// private Map<FromTo, Integer> directEdges;

	public SubsumptionAgent(String name, String team) {
		super(name, team);

		relation = new Vector<Rule>();

		this.myName = name; //.substring(5).toLowerCase();
		this.myTeam = team;
		this.myID = helpFunctions.getAgentID(myName);
		this.unseenAgentsInt = 9;

	}

	public abstract String getMyRole();


	public Vector<Rule> getRelation() {
		return this.relation;
	}


	@Override
	/*********************************************************
	@brief is currently replaced by beliefAndGoalsRevision
	@param current percepts which to process
	@return -
	 *********************************************************/
	public void handlePercept(Percept p) {
		// TODO Auto-generated method stub
	}

	@Override
	/*********************************************************
	@brief first function called by the server, returns the agents action to the server
	@param -
	@return Action item to send to the server
	 *********************************************************/
	public Action step() {
		this.russianCounter++;
		return selectAction();

	}// step

	/*********************************************************
	 * main control function; updates belief and goal base, find the possible
	 * action with the highest priority and process it further if it is an
	 * internalAction
	 * 
	 * @param -
	 * @return ExternalAction item to send to the server
	 * @see step()
	 *********************************************************/
	public Action selectAction() {

		Collection<Percept> percepts = getAllPercepts();
		Collection<Message> messages = getMessages();
		int energy = 0;
		int maxenergy = 0;

		System.out.println("*******************************"+this.myName + "***************************");
		// print out the last action and its status for debugging
		for (Percept p : percepts) {
			if (p.getName().equals(Const.LASTACTION)) {
				System.out.println("I did " + p.getParameters().get(0));
			}
			if (p.getName().equals(Const.LASTACTIONRESULT)) {
				System.out.println("I did it " + p.getParameters().get(0));
			}
			if (p.getName().equals(Const.ENERGY)) {
				energy = Integer.parseInt(p.getParameters().get(0).toString());
			}

			if (p.getName().equals(Const.MAXENERGY))
				maxenergy = Integer.parseInt(p.getParameters().get(0)
						.toString());

		}

		// check my Mailbox
		System.out.println(myName);
		if (!processMessages(messages, percepts)) {
			System.out.println("could not processMessages");
		}

		// update beliefs and goals
		if (!beliefAndGoalsRevision.beliefAndGoalsRevision(percepts, this)) {
			System.out.println("could not revise goals and precepts");
		}

		// do I still miss some agents?
		if (this.unseenAgentsInt > 0) {
			if (!sendTopologyToUnseenAgents()) {
				System.out.println("could not sendTopologytoUnseenAgents");
			}
		}

		
		LinkedList<LogicBelief> seen = getAllBeliefs(Const.SEEN);
		System.out.println("I do know " + String.valueOf(seen.size())
				+ " agents");

		Collection<LogicBelief> beliefs = getBeliefBase();
		Collection<LogicGoal> goals = getGoalBase();

		// find the Action with the highest priority which fires
		// with current percepts/goals/beliefs
		Vector<Rule> part = getRelation();
		if (part == null) {
			return null;
		}
		Action act = null;

		for (Rule r : part) {
			if (r.fire(percepts, beliefs, goals, this)) {
				act = r.getAction();
				break;
			}
		}

		// Debugging
		System.out.println("--------------->I want to do " + act.getName());

		// check whether we have an InternalAction which needs further
		// processing
		if (act instanceof InternalAction) {
			act = processAction((InternalAction) act, beliefs, goals);
			if (act instanceof InternalAction)
				return selectAction();
		}

		// Debugging
		if (act != null)
			System.out.print("--------------->I do " + act.getName() + " ");
		if (!act.getParameters().isEmpty())
			System.out.print(act.getParameters().get(0));
		System.out.println("");

		return act;

	}// selectAction

	/*********************************************************
	 * @brief second control function, defines the behavior of internalActions
	 * @param the
	 *            internalAction which to process, percepts/beliefs/goals
	 * @return hopefully an externalAction which can be send to the server
	 * @see selectAction()
	 *********************************************************/
	public Action processAction(InternalAction a,
			Collection<LogicBelief> beliefs, Collection<LogicGoal> goals) {

		String dest = "";

		// WARNING: do... function can either return a Action or a List of Goals
		if (a.getName().equals(Const.RANDOMWALK))
			return new DoRandomWalk(beliefs, goals).doIt(this);
		
		else if (a.getName().equals(Const.PLANSURVEY))
			return new DoPlanSurvey(beliefs, goals).doIt(this);
		
		else if (a.getName().equals(Const.INITREPAIR)){
			String agentName = a.getParameters().get(0).toString();
			return new DoInitRepair(beliefs, goals, this, agentName).doIt();
		}
		
		else if (a.getName().equals(Const.GOTONEARESTUNPROBEDVERTEX)) {
			ArrayList<LogicGoal> newGoals = (ArrayList<LogicGoal>) new DoGotoNearestUnprobedVertex(
					beliefs, goals).doIt();

			// fill goals
			int priority=1;
			for (int i = 0; i < newGoals.size() ; i++) {
				dest = newGoals.get(i).getParameters().firstElement()
						.toString();
				addGoal(new LogicGoal(Const.GOTO, String.valueOf(priority),dest));
				priority++;
			}

			// return first step of the route
			dest = newGoals.get(newGoals.size() - 1).getParameters()
					.firstElement().toString();

			// No other agents may have the same plan
			broadcastBelief(new LogicBelief(Const.BLOCK, Const.PROBE,
					newGoals.get(0).getParameters().firstElement()
							.toString()));
			

			return MarsUtil.gotoAction(dest);

		} else if (a.getName().equals(Const.INTIALIZEZONE)) {
			return new DoInitializeZone(beliefs, goals).doIt(this);


		} else if (a.getName().equals(Const.JOINZONE)) {
			return new DoJoinZone(beliefs, goals).doIt(this);
		}
		
		else if (a.getName().equals(Const.INITTWOPARTYZONE)) {
			return new DoInitTwoPartyZone(beliefs, goals).doIt(this);
		}
		
		else if (a.getName().equals(Const.JOINTWOPARTYZONE)) {
			return new DoJoinTwoPartyZone(beliefs, goals).doIt(this, a.getParameters().get(0).toString());
		}
		
		else if (a.getName().equals(Const.PLANDEFENDROUTE)) {
			return new DoPlanDefendRoute(beliefs, goals).doIt(a.getParameters().get(0).toString(), this);
		}
		
		else if (a.getName().equals(Const.ATTACKENEMYZONE)) {
			return new DoAttackEnemyZone(beliefs, goals).doIt(this);
		}
		
		else if (a.getName().equals(Const.ANNOYENEMYZONE)) {
			return new DoAttackEnemyZone(beliefs, goals).doIt(this);
		}
		
		else if (a.getName().equals("getRepair")) {
			return new DoGetRepair(beliefs, goals).doIt(this);
		}
		
		else if (a.getName().equals("probeInZone")) {
			return new DoProbeInZone(beliefs, goals).doIt(this);
		}
		// the Name of the Action could not be processed
		return null;
	}// processAction

	
	/*********************************************************
	 * Initiates the Topology-Map Creates a (NxN)-Matrix with an edges value
	 * 
	 * @param Collection
	 *            <Percept> percepts
	 * @return boolean, success indicator of the function
	 * @see selectAction()
	 *********************************************************/
	public boolean initDirectEdges(Collection<Percept> percepts) {
		int max = 0;
		for (Percept p : percepts) {
			if (p.getName().equals(Const.EDGES))
				max = new Integer(p.getParameters().get(0).toString());
		}
		String s = "vertex";
		for (int i = 1; i <= max; i++) {
			for (int ii = 1; ii <= max; ii++) {
				String s1 = s + new Integer(i).toString();
				String s2 = s + new Integer(ii).toString();
				if (i == ii)
					this.directEdges.put(new FromTo(s1, s2), 0);
				else
					this.directEdges.put(new FromTo(s1, s2), -2);
			}
		}
		if (this.directEdges.size() == (max * max))
			return true;
		else
			return false;
	}

	/*********************************************************
	 * evaluates the MailBox, updates BeliefBase with others agents information,
	 * receive topology from agents which I can see now
	 * 
	 * @param all percepts and messages
	 * @return boolean, success indicator of the function
	 * @see selectAction()
	 *********************************************************/
	public boolean processMessages(Collection<Message> messages,
			Collection<Percept> percepts) {

		// for the first time initialise the initOrder of all agents
		if (getAllBeliefs(Const.INITIALISATION).size() == 0)
			addBelief(new LogicBelief(Const.INITIALISATION, "empty", "empty",
					"empty", "empty", "empty", "empty", "empty", "empty",
					"empty", "empty"));
		// do I still not see some of my team's agents?
		if (unseenAgentsInt > 0)
			// do I see them now?
			updateUnseenAgents(percepts, messages);

		// process the broadcasts which my team sends me
		// careful: only messages from agents I see are processed
		// to maintain a connected map in my BeliefBase
		String agentName = "";
		String predicate = "";

		for (Message m : messages) {
			LogicBelief l = (LogicBelief) m.value;
			// get the inited order of agents
			if (l.getPredicate().equals(Const.INITAGENT)) {
				int i = new Integer(l.getParameters().get(0).toString());
				getAllBeliefs(Const.INITIALISATION).getFirst().getParameters()
						.set(i, m.sender);
			}
			
			if (l.getPredicate().equals(Const.ALLIEDROLE)) {
				addBelief(new LogicBelief(Const.ALLIEDROLE, m.sender, l
						.getParameters().get(1)));
			}
			// have new agents seen me?
			if (l.getPredicate().equals(Const.SEEYOU)) {

				if (!containsBelief(new LogicBelief(Const.SEEN, m.sender))){
					this.unseenAgentsInt -= 1;
					addBelief(new LogicBelief(Const.SEEN, m.sender));
				}			
			}//if

		}

		// get all agents I do not know
		Collection<String> unseen = new LinkedList<String>();
		for (int i = 1; i < ANZANGENTS + 1; i++) {
			agentName = "agent" + myTeam + Integer.valueOf(i);
			if (!agentName.equals(myName)) {
				if (!containsBelief(new LogicBelief(Const.SEEN, agentName))) {
					unseen.add(agentName);
				}// if
			}// if
		}// for

		for (Message m : messages) {
			if (!unseen.contains(m.sender)) {
				// TODO cast correct?
				LogicBelief l = (LogicBelief) m.value;
				predicate = l.getPredicate();
				// check if we have to alter an existing Belief
				if (predicate.equals(Const.EXPLOREDEDGE)) {
					removeBelief(new LogicBelief(Const.UNEXPLOREDEDGE, l
							.getParameters().get(0), l.getParameters().get(1)));
				}
				if (predicate.equals(Const.PROBEDVERTEX)) {
					removeBelief(new LogicBelief(Const.UNPROBEDVERTEX, l
							.getParameters().get(0)));
				}
				if (predicate.equals(Const.ALLIEDPOSITION)) {
					removeBelief(new LogicBelief(Const.ALLIEDPOSITION, l
							.getParameters().get(0)));
				}
				if (predicate.equals(Const.ALLIEDENERGY)) {
					removeBelief(new LogicBelief(Const.ALLIEDENERGY, l
							.getParameters().get(0)));
				}
				if (predicate.equals(Const.ALLIEDHEALTH)) {
					removeBelief(new LogicBelief(Const.ALLIEDHEALTH, l
							.getParameters().get(0)));
				}
				if (predicate.equals(Const.UNBLOCK)) {
					removeBelief(new LogicBelief(Const.BLOCK, l.getParameters()
							.get(0), l.getParameters().get(1)));
				}
				if (predicate.equals(Const.FREENODESOFZONE)) {
					removeBeliefs(Const.FREENODESOFZONE);
				}
				//add the Message to the Belief Base
				if (!containsBelief(l) && !predicate.equals(Const.UNBLOCK))
					addBelief(l);
			}

		}// for

		return true;
	}

	/**
	 * Checks whether a new agent can be seen.
	 * 
	 * @param percepts
	 * @param messages
	 * @return true if something changes, and we see an unknown agent
	 */
	public boolean updateUnseenAgents(Collection<Percept> percepts,
			Collection<Message> messages) {
		String agentName = "";

		// get all agents I do not know
		Collection<String> unseen = new LinkedList<String>();
		for (int i = 1; i < ANZANGENTS + 1; i++) {
			agentName = "agent" + myTeam + Integer.valueOf(i);
			if (!agentName.equals(myName)) {
				if (!containsBelief(new LogicBelief(Const.SEEN, agentName))) {
					unseen.add(agentName);
				}// if
			}// if
		}// for

		// safe all the nodes which I know
		ArrayList<String> myNodes = new ArrayList<String>();
		for (LogicBelief b :getAllBeliefs(Const.PROBEDVERTEX)){
			myNodes.add(b.getParameters().get(0).toString());
		}
		for (LogicBelief b :getAllBeliefs(Const.UNPROBEDVERTEX)){
			myNodes.add(b.getParameters().get(0).toString());
		}
		LinkedList<String> seenAgents = new LinkedList<String>();

		LogicBelief b;

		int agentID = 0;

		for (Message m : messages) {
			if (unseen.contains(m.sender)) {
				agentID = this.helpFunctions.getAgentID(m.sender);
				//if (agentID < myID) {
					b = (LogicBelief) m.value;
					if (b.getPredicate().equals(Const.UNPROBEDVERTEX)
							|| b.getPredicate().equals(Const.PROBEDVERTEX)) {
						// check if I know this node!(is there a more efficient
						// way?)
						if (myNodes.contains(b.getParameters().get(0))) {
							// I see you!
							seenAgents.add(m.sender);
							unseen.remove(m.sender);
							if (!containsBelief(new LogicBelief(Const.SEEN,
									m.sender)))
								addBelief(new LogicBelief(Const.SEEN, m.sender));
							this.unseenAgentsInt -= 1;
						}// if
					}// if
				//}// if
			}// if
		}// for

		// now update my topology
		for (Message m : messages) {
			if (seenAgents.contains(m.sender)) {
				// I want to share your information!
				b = (LogicBelief) m.value;
				if (!containsBelief(b)){
					if(b.getPredicate().equals(Const.PROBEDVERTEX))
						removeBelief(new LogicBelief(Const.UNPROBEDVERTEX,b.getParameters().get(0)));
					if(b.getPredicate().equals(Const.EXPLOREDEDGE))
						removeBelief(new LogicBelief(Const.UNEXPLOREDEDGE,b.getParameters().get(0),b.getParameters().get(1)));
					addBelief(b);
				}//if
			}// else
		}// for

		// I now send you my topology
		Collection<LogicBelief> messagePack = new ArrayList<LogicBelief>();
		messagePack.addAll(getAllBeliefs(Const.UNPROBEDVERTEX));
		messagePack.addAll(getAllBeliefs(Const.PROBEDVERTEX));
		messagePack.addAll(getAllBeliefs(Const.UNEXPLOREDEDGE));
		messagePack.addAll(getAllBeliefs(Const.EXPLOREDEDGE));
		messagePack.add(new LogicBelief(Const.SEEYOU));
		for (String receiver : seenAgents) {
			//System.out.println("Hey, I see " + receiver);
			for (LogicBelief m : messagePack) {
				sendMessage(m, receiver);
			}// for
		}// for

		return true;
	}

	/*********************************************************
	 * Agents send their topology information to all unseen agents
	 * so that the message-receiver can check whether he and the sender
	 * share at least one vertex (i.e. they see each other)
	 * 
	 * @param -
	 * @return boolean, success indicator of the function
	 * @see selectAction()
	 *********************************************************/
	public boolean sendTopologyToUnseenAgents() {

		String agentName = "";
		int agentID = 0;

		// get all agents I do not know
		Collection<String> unseen = new LinkedList<String>();
		for (int i = 1; i < ANZANGENTS + 1; i++) {
			agentName = "agent" + myTeam + Integer.valueOf(i);
			if (!agentName.equals(myName)) {
				if (!containsBelief(new LogicBelief(Const.SEEN, agentName))) {
					unseen.add(agentName);
				}// if
			}// if
		}// for

		Collection<LogicBelief> messagePack = new ArrayList<LogicBelief>();
		// gather all Beliefs which should be sent
		messagePack.addAll(getAllBeliefs(Const.UNPROBEDVERTEX));
		messagePack.addAll(getAllBeliefs(Const.PROBEDVERTEX));
		messagePack.addAll(getAllBeliefs(Const.UNEXPLOREDEDGE));
		messagePack.addAll(getAllBeliefs(Const.EXPLOREDEDGE));

		for (String receiver : unseen) {

			// sent the messages
		   System.out.println("I send a message to " + receiver);

			if (messagePack.size() > 0) {
				for (LogicBelief m : messagePack) {
					sendMessage(m, receiver);
				}
			}
		}// for

		return true;
	}

	/*********************************************************
	 * @brief Removes a single Goal from the goalBase. Checking both name and
	 *        parameter. The provided method only checks for the goal predicate.
	 * @param LogicGoal
	 *            deleteThis (the goal which should be deleted)
	 * @return true if the goal was found in goalBase
	 * @author Dennis
	 *********************************************************/
	public boolean removeGoal(LogicGoal deleteThis) {

		boolean beliefDeleted = false;
		boolean beliefFound = false;
		Collection<LogicGoal> tmp = getGoalBase();
		clearGoals();

		for (LogicGoal b : tmp) {
			beliefFound = true;
			System.out.print("");
			if(!b.getPredicate().equals(deleteThis.getPredicate())){
				addGoal(b);
				beliefFound = false;
			}else{
				for (int i = 0; i < b.getParameters().size(); i++) {
					if (i < deleteThis.getParameters().size()) {
						if (!b.getParameters().get(i).toString().equals(
								deleteThis.getParameters().get(i).toString())) {
							addGoal(b);
							beliefFound = false;
							break;
						}// if
					}// if
				}// for
			}
			if (!beliefFound) {
				beliefDeleted = true;
			}// if
		}// for
		return beliefDeleted;
		
	}// removeGoal

	/*********************************************************
	 * @brief Removes a single Belief from the beliefBase. Checking both name
	 *        and parameter. The provided method only checks for the belief
	 *        predicate.
	 * @param LogicGoal
	 *            deleteThis (the belief which should be deleted)
	 * @return true if the belief was found in beliefBase
	 * @author Dennis
	 *********************************************************/
	protected boolean removeBelief(LogicBelief deleteThis) {

		boolean beliefDeleted = false;
		boolean beliefFound = false;
		Collection<LogicBelief> tmp = getAllBeliefs(deleteThis.getPredicate());
		removeBeliefs(deleteThis.getPredicate());

		for (LogicBelief b : tmp) {
			beliefFound = true;
			if(!b.getPredicate().equals(deleteThis.getPredicate())){
				addBelief(b);
				beliefFound = false;
			}else{
				for (int i = 0; i < b.getParameters().size(); i++) {
					if (i < deleteThis.getParameters().size()) {
						if (!b.getParameters().get(i).toString().equals(
								deleteThis.getParameters().get(i).toString())) {
							addBelief(b);
							beliefFound = false;
							break;
						}// if
					}// if
				}// for
			}
			if (!beliefFound) {
				beliefDeleted = true;
			}// if
		}// for
		return beliefDeleted;
	} // removeBelief
	
	
	/*********************************************************
	 * @brief Removes the goal with the highest priority within
	 * the goal base
	 * @return void
	 *********************************************************/
	public void removeLastGoal(){
		
		Collection<LogicGoal> tmp = getGoalBase();
		LogicGoal deleteThis = new LogicGoal("");
		int highestValue=0;
	

		for (LogicGoal b : tmp) {
			if (Integer.valueOf(b.getParameters().get(0))>highestValue){
				highestValue=Integer.valueOf(b.getParameters().get(0));
				deleteThis=b;
			}
		}// for
		if (deleteThis!=null)
			removeGoal(deleteThis);
	
	}// removeGoal
	
	
	/*********************************************************
	 * @brief returns the goal with the highest priority within
	 * the goal base
	 * @return LogicGoal
	 *********************************************************/
	public LogicGoal getLastGoal(){
		
		Collection<LogicGoal> tmp = getGoalBase();
		LogicGoal deleteThis = new LogicGoal("");
		int highestValue=0;
		
		for (LogicGoal b : tmp) {
			if (Integer.valueOf(b.getParameters().get(0))>highestValue){
				highestValue=Integer.valueOf(b.getParameters().get(0));
				deleteThis=b;
			}
			
		}// for
		return deleteThis;
		
	}// removeGoal
	

	/*********************************************************
	 * @brief set and get functions for all the agents attributes
	 *********************************************************/
	public static int getAnzangents() {
		return ANZANGENTS;
	}
	
	public int getZoneStart() {
		return ZONESTART;
	}

	public String getMyName() {
		return myName;
	}

	public int getMyID() {
		return myID;
	}

	public String getMyTeam() {
		return myTeam;
	}

	public int getMyEnergy() {
		return myEnergy;
	}

	public int getMyMaxEnergy() {
		return myMaxEnergy;
	}
	
	public int getMyMaxEnergyDisabled() {
		return myMaxEnergyDisabled;
	}

	public int getMyHealth() {
		return myHealth;
	}
	
	public int getMyMaxHealth() {
		return myMaxHealth;
	}

	public int getMyStrength() {
		return myStrength;
	}
	
	public String getMyPos() {
		return myPos;
	}

	public String getMyLastActionResult() {
		return myLastActionResult;
	}
	
	public int getStep() {
		return step;
	}
	
	public int getMoney() {
		return money;
	}

	public String getMyLastAction() {
		return myLastAction;
	}

	public HelpFunctions getHelpFunctions() {
		return helpFunctions;
	}
	
	public int getRussianCounter() {
		return russianCounter;
	}

	public void setMyName(String myName) {
		this.myName = myName;
	}

	public void setMyID(int myID) {
		this.myID = myID;
	}

	public void setMyTeam(String myTeam) {
		this.myTeam = myTeam;
	}

	public void setMyEnergy(int myEnergy) {
		this.myEnergy = myEnergy;
	}

	public void setMyMaxEnergy(int myMaxEnergy) {
		this.myMaxEnergy = myMaxEnergy;
	}
	
	public void setMyMaxEnergyDisabled(int value) {
		this.myMaxEnergyDisabled = value;
	}

	public void setMyHealth(int myHealth) {
		this.myHealth = myHealth;
	}
	
	public void setMyMaxHealth(int myMaxHealth) {
		this.myMaxHealth = myMaxHealth;
	}
	
	public void setMyStrength(int value) {
		this.myStrength = value;
	}

	public void setMyPos(String myPos) {
		this.myPos = myPos;
	}

	public void setMyLastActionResult(String myLastActionResult) {
		this.myLastActionResult = myLastActionResult;
	}

	public void setMyLastAction(String myLastAction) {
		this.myLastAction = myLastAction;
	}
	public void setMoney(int money) {
		this.money = money;
	}

	public void setStep(int step) {
		this.step = step;
	}

}// class

