package net.sf.beenuts.apc.net;

import java.net.Socket;

import net.sf.beenuts.apc.*;
import net.sf.beenuts.apc.net.transfer.AgentAction;
import net.sf.beenuts.apc.net.transfer.AgentMessage;
import net.sf.beenuts.apc.net.transfer.AgentPerception;
import net.sf.beenuts.net.Connection;
import net.sf.beenuts.net.ConnectionHandler;

/**
 * this class holds writer and reader for
 * exchanging information with another agentProcessCluster.
 * 
 * @author Thomas
 *
 */
public class APCConnection implements ConnectionHandler {
	
	APCSocketReader reader = null;
	APCSocketWriter writer = null;
	AgentProcessCluster agentProcessCluster = null;
	
	public APCConnection() {
		
	}
	
	public void init(Socket socket, AgentProcessCluster agentProcessCluster) {
		// register self at agentProcessCluster
		this.agentProcessCluster = agentProcessCluster;
		this.agentProcessCluster.newAPCConnection(this, null);
		
		// setup in/out streams
		this.reader = new APCSocketReader(socket, this);		
		this.writer = new APCSocketWriter(socket);
		this.writer.start();
		this.reader.start();		
	}
	
	/**
	 * sends a message to the agentProcessCluster connected to
	 * by this connection
	 * 
	 * @param msg ACP Message Object to send
	 */
	public void send(APCTransfer msg) {
		msg.sourceAPC = this.agentProcessCluster.getName();
		writer.write(msg);
	}

	@Override
	public void dataReceived(Connection conn, Object result) {
		APCTransfer msg = (APCTransfer) result;
		
		// handle message
		switch (msg.id) {
		case APCTransfer.T_HELLO:
			// a gentle hello from remote endpoint
			System.out.println("got a hello from "+msg.sourceAPC);
			this.agentProcessCluster.newAPCConnection(this, msg.sourceAPC);
			break;
			
		case APCTransfer.T_AGENTMESSAGE:
			// perception from another agent
			System.out.println("got an agent message from "+msg.sourceAPC);
			AgentMessage amsg = (AgentMessage) msg;
			this.agentProcessCluster.receiveAgentMessage(amsg.message, amsg.recipients);
			break;
			
		case APCTransfer.T_AGENTPERCEPTION:
			// perception from a remote simulation
			AgentPerception agper = (AgentPerception) msg;
			this.agentProcessCluster.receiveAgentPerception(agper.dstAgent, agper.perception);
			break;
						
		case APCTransfer.T_AGENTACTION:
			// action from a remote agent
			AgentAction agact = (AgentAction) msg;
			this.agentProcessCluster.receiveAgentAction(agact.srcAgent, agact.plAction);
			break;
			
		default:
			System.out.println("unhandled agentProcessCluster transfer: "+msg);
			break;
		}
	}

	@Override
	public void errorOccurred(Connection conn, String error) {
		// 
	}

	@Override
	public void connectionClosed(Connection conn) {
		// TODO Auto-generated method stub
		
	}
			
}
