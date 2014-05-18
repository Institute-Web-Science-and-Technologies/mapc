package net.sf.beenuts.apc.net.transfer;

import net.sf.beenuts.apc.net.APCTransfer;
import java.io.Serializable;

public class AgentAction extends APCTransfer implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 510998653901694260L;

	public AgentAction(String src, String action) {
		super(APCTransfer.T_AGENTACTION);
		
		this.srcAgent = src;
		this.plAction = action;
	}

	// source identifier
	public String srcAgent;
	
	// action, as prolog-like string
	public String plAction;
	
	@Override
	public String toString() {
		return "agentProcessCluster agent action transfer: src: "+this.srcAgent+", action: "+this.plAction;
	}
}
