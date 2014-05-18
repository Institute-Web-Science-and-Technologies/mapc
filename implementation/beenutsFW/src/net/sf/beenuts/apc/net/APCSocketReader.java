package net.sf.beenuts.apc.net;

import java.io.ObjectInputStream;
import java.net.Socket;

import net.sf.beenuts.net.ConnectionHandler;

/**
 * threaded reader for agentProcessCluster connections,
 * allowing non-blocking reads.
 * 
 * @author Thomas Vengels
 *
 */
public class APCSocketReader extends Thread {
	
	ObjectInputStream objIn = null;
	Socket	socket = null;
	ConnectionHandler handler = null;
	
	public APCSocketReader(Socket sock, ConnectionHandler handler) {
		this.socket = sock;	
		this.handler = handler;
	}
	
	@Override
	public void run() {
		try {
			objIn = new ObjectInputStream( socket.getInputStream() );
			
			while (true) {
				APCTransfer msg = (APCTransfer) objIn.readObject();
				// TODO: Also use the Connection base class.
				handler.dataReceived( null, msg );
			}
			
		} catch (Exception e) {
			//e.printStackTrace();
		}
	}
}
