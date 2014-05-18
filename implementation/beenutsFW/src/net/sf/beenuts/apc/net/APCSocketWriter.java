package net.sf.beenuts.apc.net;

import java.util.concurrent.*;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.*;

/**
 * writer thread used by agentProcessCluster connections, allowing
 * non-blocking write of agentProcessCluster messages.
 * 
 * @author Thomas Vengels
 *
 */
public class APCSocketWriter extends Thread {

	protected LinkedBlockingQueue<Serializable> writeQueue = null;
	protected Socket socket;
	
	public APCSocketWriter(Socket socket) {
		this.writeQueue = new LinkedBlockingQueue<Serializable>();
		this.socket = socket;
	}
	
	public void run() {
		try {
			ObjectOutputStream objOut = new ObjectOutputStream(socket.getOutputStream());
			while(true) {
				Serializable toSend = writeQueue.take();
				objOut.writeObject(toSend);
				objOut.flush();
			}
		} catch (Exception e) {
			System.err.println("agentProcessCluster-writer error:");
			e.printStackTrace();
		}
	}
	
	public void write(Serializable o) {
		this.writeQueue.offer(o);
	}
}
