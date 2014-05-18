package net.sf.beenuts.apc;

import net.sf.beenuts.ap.*;

/**
 * execution interface for agent processes (agentProcess). this interface
 * is provided by an agentProcess-scheduler.
 * 
 * @author Thomas Vengels
 *
 */
public interface AgentProcessRunner {
	
	/**
	 * this method signals the agent scheduler
	 * to run the associated agent 
	 */
	public void	runMe();
	
	/**
	 * returns the agent process associated with
	 * this runner instance
	 * 
	 * @return agentProcessCluster agent instance
	 */
	public AgentProcess getAgentProcess();
	
	
	/**
	 * this method signals a scheduler that a caller
	 * entered a critical section. preemption is
	 * disabled for the agent thread.
	 */
	public void enterCritical();
	
	/**
	 * this method signals a scheduler that
	 * an agent no longer resides in a critical
	 * section. preemption is enabled for an
	 * agent's thread.
	 */
	public void leaveCritical();
}
