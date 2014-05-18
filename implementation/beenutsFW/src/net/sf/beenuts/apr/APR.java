package net.sf.beenuts.apr;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.sf.beenuts.ap.*;
import net.sf.beenuts.apc.AgentProcessCluster;

/**
 * beenuts apr implementation, can be used as a foundation
 * for apr's supporting custom environments, but want to
 * use the agentProcessCluster messaging system. 
 * 
 * TODO: make methods thread save (still ok if agent initialization
 * happens at startup).
 * 
 * * local broadcast: ok
 * * local send to target(s): preliminary
 * * global broadcast: ok
 * * global send to target(s): preliminary 
 * 
 * @author Thomas Vengels
 *
 */
public abstract class APR {
	
	public List<AgentProcess> agents = new LinkedList<AgentProcess>();
	public Map<String,AgentProcess> agentMap = new HashMap<String,AgentProcess>();
	int step = 0;
	AgentProcessCluster agentProcessCluster;		
	
	public APR() {
		
	}
		
	public boolean signOn(AgentProcess ag) {
		System.out.println("apr: registered agent " + ag.getName());
		agents.add(ag);
		this.agentMap.put(ag.getName(), ag);
		
		// TODO URGENT Check return value
		return true;
	}

	public void signOff(AgentProcess ag) {
		// not required here
	}
	

	public AgentProcessCluster getAPC() {
		return agentProcessCluster;
	}


	public void setAPC(AgentProcessCluster agentProcessCluster) {
		this.agentProcessCluster = agentProcessCluster;
	}


	/**
	 * this method relays a perception only to local agents.
	 * used by the agentProcessCluster layer to send data to local agents.
	 * this method MUST NOT call any agentProcessCluster services to forward
	 * a message within the super cluster.
	 * 
	 * @param perception object representing a perception
	 * @param agents list of agent names, or null for all local agents
	 * 
	 */
	public void localRelay(Object perception, Collection<String> agents) {
		if (agents == null) {
			for (AgentProcess agent : this.agents) {
				agent.perceive(perception);
			}
		} else {
			for (String ag : agents) {
				if (this.agentMap.containsKey(ag)) {
					AgentProcess agent = agentMap.get(ag);
					agent.perceive(perception);
				}				
			}
		}
	}
	
	/**
	 * this method is used to send a message over the agentProcessCluster network. it automatically
	 * handles local and remote agents. however, agents are expected to use
	 * the APRLink interface
	 * 
	 * @param sender name of agent initiating message transfer
	 * @param message information to send
	 * @param recipients target agents
	 */
	public void sendMessage(String sender, Object message, Collection<String> recipients) {
		// apr send should distinct between a broadcast (recipient == null)
		// or directed send. it should also distinct between local
		// and remote agents.
		
		if (recipients == null) {
			// broadcast
			
			// send message to local agents
			for (AgentProcess a : agents) {
				if (a.getName().equalsIgnoreCase(sender))
					continue;
				a.perceive(message);
			}
			
			// use agentProcessCluster to broadcast message
			getAPC().sendAgentMessage(message, null);
		} else {				
			// targeted cast
			
			// list of agents living on another agentProcessCluster
			List<String> externalAgents = new LinkedList<String>();
			
			// send msg to local agents first
			for (String agName : recipients) {
				if (agentMap.containsKey(agName)) {
					AgentProcess a = agentMap.get(agName);
					a.perceive( message );
				} else {
					// if a target is not within the agentMap,
					// suspect it being anywhere
					externalAgents.add(agName);
				}
			}
			
			// remaining agents are expected to be somewhere else
			if (externalAgents.size() > 0)
				getAPC().sendAgentMessage(message, externalAgents);
		}
	}
	
	public abstract void sendAction(String agentName, Object action);	
	
}
