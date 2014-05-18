package net.sf.beenuts.ap;

import java.util.*;

import eis.iilang.*;


/**
 * Ancestor class for agent architecture implementations.
 * an agent architecture is a formal agent model with
 * specific semantics going beyond the input-process-output
 * semantics specified by an AgentProcess.
 * 
 * @author Thomas Vengels, Matthias Thimm
 */
public abstract class AgentArchitecture {

	/** The agent architecture of this agent process. */
	private AgentProcess ap;
	
	/**
	 * Initialization method, any agent process should
	 * run start-up code here.
	 *
	 * @param apca the agent architecture of this agent process.
	 * @return "true" iff initialization was successful.
	 */
	public boolean init(AgentProcess apca ){
		this.ap = apca;		
		return true;
	}
	
	/**
	 * send a message to certain recipients. 
	 * 
	 * @param message
	 * @param recipients
	 */
	protected void send(Object message, Collection<String> recipients){
		this.ap.send(message, recipients);
	}
	
	/**
	 * send an action to an environment.
	 * 
	 * @param action
	 */
	protected void act(Action action){
		this.ap.act(action);
	}
	
	/**
	 * returns the unique name of this agent
	 */
	public String getName(){
		return this.ap.getName();
	}
	
	/**
	 * Reasoning cycle of an agent. agents should
	 * perform calculation here, and use the given
	 * agentProcessCluster interface from its initialization to
	 * send actions or commit actions.
	 * 
	 * @param perception perception or null
	 * @return true indicating an agent has completed its rc, false if it requires another cycle
	 */
	public abstract boolean cycle(Object perception);
	
	/**
	 * Cleanup procedure, agents should release
	 * resources here.
	 */
	public abstract void shutdown();
}
