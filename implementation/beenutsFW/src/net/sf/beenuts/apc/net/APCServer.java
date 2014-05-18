package net.sf.beenuts.apc.net;

import java.net.*;
import java.io.*;

import net.sf.beenuts.apc.AgentProcessCluster;


/**
 * 
 * this class models an agentProcessCluster server, responsible for
 * handling connections between agentProcessCluster's.
 * 
 * @author Thomas Vengels
 *
 */
public class APCServer extends Thread {

	ServerSocket	listen;
	AgentProcessCluster	agentProcessCluster;
	
	/**
	 * initializes the server
	 * 
	 * @param port
	 */
	public void init(int port, AgentProcessCluster agentProcessCluster) {
		try {
			this.agentProcessCluster = agentProcessCluster;
			this.listen = new ServerSocket(port);
			
		} catch (IOException ioErr) {
			System.err.println("server::init: "+ioErr);
		}
		this.start();
	}
	
	@Override
	public void run() {
		
		while(listen != null) {
			
			try {
				// wait for incoming connetions
				Socket clientSocket = this.listen.accept();
				
				// create a new client
				System.out.println("server: new connection");
				APCConnection clientCon = new APCConnection();
				
				// initialize connection and send a hello
				clientCon.init(clientSocket, this.agentProcessCluster);
				clientCon.send( new APCTransfer(APCTransfer.T_HELLO ) );
				//agentProcessCluster.newAPCConnection( clientCon, null );
				
			} catch (IOException ioErr) {
				System.err.println("server io error:\n"+ioErr);
			}
			
		}
		
	}
	
	
	/**
	 * shuts down the agentProcessCluster server. a goodbye message
	 * is sent to all connected apcs, and all remaining
	 * tasks are aborted.
	 */
	public void shutdown() {
		try {
			this.listen.close();
		} catch (Exception e) {
			System.err.println("server::shutdown error:"+e);
		}
		
	}
			
}

