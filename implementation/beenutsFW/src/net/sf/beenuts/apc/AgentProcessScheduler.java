package net.sf.beenuts.apc;

import net.sf.beenuts.ap.*;

/**
 * this interface defines a scheduler used by
 * an agentProcessCluster to assign cpu time to agents
 * 
 * @author Thomas Vengels
 *
 */
public interface AgentProcessScheduler extends Runnable {

	public void addAgent(AgentProcess ag);
	
	public void removeAgent(AgentProcess ag);
	
	public void startScheduler();
	
	public void stopScheduler();
	
	public void initialize(int maxConcurrency, int cpuTime);
}
