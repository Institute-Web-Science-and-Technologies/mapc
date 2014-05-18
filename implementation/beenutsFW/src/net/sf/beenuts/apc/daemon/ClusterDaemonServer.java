package net.sf.beenuts.apc.daemon;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

import net.sf.beenuts.apc.util.APCConfiguration;
import net.sf.beenuts.apscc.APSCCConfig;
import net.sf.beenuts.apscc.APEntry;
import net.sf.beenuts.net.Connection;
import net.sf.beenuts.net.ConnectionController;
import net.sf.beenuts.net.ConnectionHandler;

/**
 * This class acts as a server for receiving the apcss configurations or
 * agentProcessCluster configurations.
 * @author Tim Janus
 */
public class ClusterDaemonServer implements Runnable, ConnectionHandler{
	
	/**
	 * Port used for the AgentProcessCluster for communication about the actual simulation.
	 */
	private static int APC_SERVER_PORT = 20202;
	
	public ClusterDaemonServer(short port) {
		this.port = port;
	}
	
	public void run() {
		ConnectionController.getInstance();
		ServerSocket server;
		System.out.println("Starting Daemon on port: " + port);
		
		try {
			server = new ServerSocket(port);
			while(run) {
				Socket sock = server.accept();
				System.out.println("Connection accepted");
				Connection con = new Connection(sock, Connection.OBJECT_BASED_CONNECTION);
				con.addHandler(this);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	@Override
	public void dataReceived(Connection conn, Object result) {
		APCConfiguration config = null;
		
		if(result instanceof APSCCConfig) {
			System.out.println("Received apsccconfig Document");
			
			conn.sendObject(new Boolean(true));
			APSCCConfig recvConfig = (APSCCConfig)result;
			
			config = new APCConfiguration();
			config.first_config = true;
			config.server_port = APC_SERVER_PORT;
			config.apr_class = recvConfig.getRelayClass();
			config.schedClass = recvConfig.getSchedulerClass();
			config.group = recvConfig.getGroup();
			for(APEntry entry : recvConfig.getAgents()) {
				config.agentHosts.put(entry.getName(), "localhost");
				config.agentClass.put(entry.getName(), recvConfig.getAgentClass());
			}
		} else if(result instanceof APCConfiguration) {
			System.out.println("Received apcconfig Document");
			
			config = (APCConfiguration)result;
			config.first_config = false;
			config.server_port = APC_SERVER_PORT;
		} else {
			return;
		}
		
		apcConfigs.offer(config);	
	}

	@Override
	public void errorOccurred(Connection conn, String error) {
	}

	@Override
	public void connectionClosed(Connection conn) {
		System.out.println("Connection closed");
	}
	
	public void kill() {
		synchronized (sync) {
			run = false;
			
		}
	}

	public LinkedBlockingQueue<APCConfiguration> getConfigs() {
		return apcConfigs;
	}
	
	private LinkedBlockingQueue<APCConfiguration> apcConfigs = new LinkedBlockingQueue<APCConfiguration>();
	
	private Object sync = new Object();
	
	private boolean run = true;
	
	private short port;
}
