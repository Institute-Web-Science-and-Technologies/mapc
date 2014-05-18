package net.sf.beenuts.apc.net.transfer;

import java.io.Serializable;

import net.sf.beenuts.apc.net.APCTransfer;

public class AgentPerception extends APCTransfer implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2434075645261874252L;

	public AgentPerception(String dstAgent, String perception) {
		super( APCTransfer.T_AGENTPERCEPTION );
		this.dstAgent = dstAgent;
		this.perception = perception;
	}

	public String dstAgent;
	public String perception;
	
	@Override
	public String toString() {
		return "agentProcessCluster agent perception transfer to agent: "+this.dstAgent;
	}
}
