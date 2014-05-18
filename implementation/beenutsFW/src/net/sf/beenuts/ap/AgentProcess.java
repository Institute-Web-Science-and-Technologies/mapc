package net.sf.beenuts.ap;

import java.util.*;

import net.sf.beenuts.apc.*;
import net.sf.beenuts.apr.APR;

/**
 * Low level interface of an agent. the concrete implementation
 * has to be done by an agentProcessCluster, and both an apr and an agent architecture
 * uses this interface to exchange data. the AgentProcess interface
 * provides basic input - process - output functionality for an
 * agent implementation. it does not cover any specific semantics
 * like a BDI model, this has to be implemented in an agent architecture. 
 * 
 * @author Thomas Vengels
 *
 */
public interface AgentProcess {

	/**
	 * offers a perception to an agent process
	 * 
	 * @param perception
	 */
	public void perceive(Object perception);
	
	/**
	 * returns the unique name of this agent
	 */
	public String getName();
	
	/**
	 * returns the agent process instance associated
	 * with this agentProcessCluster agent instance.
	 * 
	 * @return AgentArchitecture instance
	 */
	public AgentArchitecture getAgentArchitecture();
	
	/**
	 * assigns an agentProcess to this agentProcessCluster agent
	 * 
	 * @param AgentArchitecture agent process to bind
	 */
	public void setAgentArchitecture(AgentArchitecture agArch);
	
	/**
	 * send a message to certain recipients. 
	 * 
	 * @param message
	 * @param recipients
	 */
	public void send(Object message, Collection<String> recipients);
	
	/**
	 * send an action to an environment
	 * 
	 * @param action
	 */
	public void act(Object action);
	
	/**
	 * this method is called whenever an agent should execute a cycle
	 */
	public void execCycle();

	/**
	 * assigns an aprunner to this agentProcessCluster-agent
	 * 
	 * @param apr aprunner instance
	 */
	public void setRunner(AgentProcessRunner aprunner);	
	
	/**
	 * assigns an aprlink to this agentProcessCluster-agent
	 * @param aprl
	 */
	public void setAPR(APR apr);
	
	/**
	 * this functions check if an agent requires
	 * an execution cycle, or can be put asleep
	 * 
	 * @return true if perceptions are available, false if not.
	 */
	public boolean canSleep();
	
}
