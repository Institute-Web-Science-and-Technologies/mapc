package net.sf.beenuts.apc.net.transfer;

import java.io.Serializable;
import java.util.Collection;

import net.sf.beenuts.apc.net.APCTransfer;

public class AgentMessage extends APCTransfer implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1384398105787593731L;
	public Object				message;
	public Collection<String>	recipients;
	
	public AgentMessage(Object message, Collection<String> recipients) {
		super( APCTransfer.T_AGENTMESSAGE );
		this.message = message;
		this.recipients = recipients;
	}
	
	@Override
	public String toString() {
		return "agentProcessCluster agent message transfer";
	}
}
