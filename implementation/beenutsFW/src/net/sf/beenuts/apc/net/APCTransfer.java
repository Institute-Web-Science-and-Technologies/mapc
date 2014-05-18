package net.sf.beenuts.apc.net;

import java.io.Serializable;

/**
 * message class used for agentProcessCluster to agentProcessCluster communication
 * 
 * @author Thomas Vengels
 *
 */
public class APCTransfer implements Serializable {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3208739566550735456L;
	public static final int T_HELLO = 1;
	public static final int T_AGENTMESSAGE = 2;
	public static final int T_AGENTACTION = 3;
	public static final int T_AGENTPERCEPTION = 4;
	
	// identifier of message
	public final int id;
	
	// agentProcessCluster source, set by the underlying messaging system 
	public String sourceAPC;
	
	public APCTransfer(int transfer_type) {
		this.id = transfer_type;
	}
	
	@Override
	public String toString() {
		return "agentProcessCluster transfer type "+id+ " from "+sourceAPC;
	}
}
