package net.sf.beenuts.apc.daemon;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.sf.beenuts.apc.*;
import net.sf.beenuts.apc.util.APCConfiguration;
import net.sf.beenuts.apscc.*;
import net.sf.beenuts.apscc.net.*;
import net.sf.beenuts.net.*;
import net.sf.beenuts.util.*;

/**
 * This class provides the Main method the entry point for an ClusterDaemon, it starts
 * a connection to the yp server and listens for configurations given by the APSCC
 * or by another agentProcessCluster.
 * 
 * An instances of this class is responsible for translating the received configuration
 * in a format which can be understand by the AgentProcessCluster.initialize method and also for 
 * instantiating and initializing an AgentProcessCluster. So it handles a received configuration.
 * 
 * @author Tim Janus
 */
public class ClusterDaemon implements ConnectionHandler {

	/** id used by the yp server to identify this daemons **/
	private int id;
	/** reference to the reworked configuration which can be used to initialize an AgentProcessCluster **/
	private APCConfiguration config;
	/** flag indicating if the daemon instance (handler) has finished the translation*/
	private boolean working;
	/** A hint for the agent division algorithm to decide how many agent a daemon should process */
	private int maxAgents;
	/** An String ID used for grouping APCs. In the APSCC you can select a group so only APCs
	 * 	in this selected group will be used for simulating agents. */
	private String group;
	/** This thread is used for receiving new simulation configurations from the APSCC or another AgentProcessCluster*/
	private static Thread serverThread;
	
	/**
	 * Instantiate a new Daemon Handler
	 * @param id		the id of the daemon given by yp server.
	 * @param maxAgents	maximum agents which should run on this daemon
	 * @param group		Only handles simulations of the given groupname
	 */
	public ClusterDaemon(int id, int maxAgents, String group) {
		this.id = id;
		this.maxAgents = maxAgents;
		this.group = group;
	}

	/**
	 * @return the id of the server, (the number used by the yp server to identify this daemon)
	 */
	public int getId() {
		return id;
	}
	
	/* (non-Javadoc)
	 * @see net.sf.beenuts.net.ConnectionHandler#handle(java.lang.Object)
	 */
	@Override
	public void dataReceived(Connection conn, Object result) {
		if(result instanceof String) {
			String str = (String)result;
			if(str.startsWith("<list>")) {
				str = str.substring(6);
				handleYPList(str);
			}
		}
	}

	/**
	 * Helper method: 	works on the returned server list of the yp server. Adapts the APCConfig
	 * 					dividing the agents on different APCs over the network.
	 * @param str	The string received from the yp-server without the "<list>" prefix.
	 */
	private void handleYPList(String str) {
		List<APCEntry> serverList = new LinkedList<APCEntry>();
		String [] servers = str.split("\\|");
		APCEntry yourself = null;
		for(String server : servers) {
			APCEntry entry = new APCEntry(server);
			if(entry.getId() != id && entry.getGroup().equals(config.group))
				serverList.add(entry);
			else if(entry.getId() == id)
				yourself = entry;
		}		
		for(String key : config.agentHosts.keySet()) {
			config.agentHosts.put(key, String.valueOf(id));
		}		
		if(serverList.size() != 0) {
			config.server_enable = true;			
			// add yourself...
			if(yourself != null)
				serverList.add(yourself);
			else
				serverList.add(new APCEntry(id, "localhost", (short) 20201, maxAgents, group));			
			for(APCEntry entry : serverList) {
				config.connect2APC.put(String.valueOf(entry.getId()), 
						new Pair<String, Integer>(entry.getIp(), (int)entry.getPort()));
			}			
			int countMaxAgents = 0;
			double helperAry [] = new double [serverList.size()];
			for(APCEntry entry : serverList) {
				countMaxAgents += entry.getMaxAgents();
			}			
			for(int i=0; i<helperAry.length; ++i) {
				helperAry[i] = serverList.get(i).getMaxAgents() / (double)countMaxAgents;
			}			
			double scoreAry[] = helperAry.clone();
			Iterator<String> it = config.agentHosts.keySet().iterator();
			while(it.hasNext()) {
				for(int i=0; i<helperAry.length; ++i) {
					scoreAry[i] += helperAry[i];
					if(scoreAry[i] >= 1) {
						APCEntry entry = serverList.get(i);
						config.agentHosts.put(it.next(), String.valueOf(entry.getId()));
						scoreAry[i] -= 1.0;
					}
				}
			}			
			// Inform the other daemons
			for(APCEntry entry : serverList) {
				if(entry.equals(yourself))
					continue;				
				APCConfiguration config_to_send = new APCConfiguration(config);
				config_to_send.connect2APC.remove(entry.getId());
				
				//TODO: what's the following for?
				ConnectionHandler temp = new ConnectionHandler() {					
					/* (non-Javadoc)
					 * @see net.sf.beenuts.net.ConnectionHandler#handle(java.lang.Object)
					 */
					@Override
					public void dataReceived(Connection conn, Object result) {
						if(result instanceof Boolean) {
							System.out.println("Config transfered to another agentProcessCluster.");
						}
					}					
					/* (non-Javadoc)
					 * @see net.sf.beenuts.net.ConnectionHandler#error(java.lang.String)
					 */
					@Override
					public void errorOccurred(Connection conn, String error) { }					
					/* (non-Javadoc)
					 * @see net.sf.beenuts.net.ConnectionHandler#close()
					 */
					@Override
					public void connectionClosed(Connection conn) { }
				};
				System.out.println("Send config information to: " + entry.getIp() + ":" + entry.getPort());
				APCConfigureConnection connection = new APCConfigureConnection(entry.getIp(), entry.getPort(), config_to_send);
				connection.addHandler(temp);
				connection.startConfiguration();				
			}			
			// dont connect to myself.
			config.connect2APC.remove(String.valueOf(id));			
		// no server but our found: take the old config
		} else {
			config.server_enable = false;			
		}		
		working = false;
	}
	
	/* (non-Javadoc)
	 * @see net.sf.beenuts.net.ConnectionHandler#error(java.lang.String)
	 */
	@Override
	public void errorOccurred(Connection conn, String error) { }
	
	/* (non-Javadoc)
	 * @see net.sf.beenuts.net.ConnectionHandler#close()
	 */
	@Override
	public void connectionClosed(Connection conn) { }
	
	/**
	 * Give the daemon handler instance a new configuration to work on.
	 * @param config
	 */
	public void setConfig(APCConfiguration config) {
		this.config = config;
		if(config != null)
			working = true;
	}
	
	/**
	 * @return 	the configuration for a new simulation build by the ClusterDaemon after it receives a 
	 * 			new configuration connection from the APSCC or another AgentProcessCluster.
	 */
	public APCConfiguration getConfig() {
		return config;
	}
	
	/**
	 * @return true while the APCDameon instance builds a configuration for a new simulation.
	 */
	public boolean isWorking() {
		return working;
	}
	
	/**
	 * Main method of an ClusterDaemon.
	 * Starts a connection to the yp server and listens for configurations send from the APSCC or other
	 * APCs.
	 * @see ClusterDaemonServer ClusterDaemonYpConnection
	 * @param args 
	 */
	public static void main(String[] args) {
		ClusterDaemonConfig daemon_conf = ClusterDaemonConfig.loadFromFile("apc_daemon.xml");
		short port = -1;
		int maxagents = -1;
		String group = "";
		
		if(args.length >= 1) {
			port = Short.parseShort(args[0]);
			if(args.length >= 2) {
				maxagents = Integer.parseInt(args[1]);
				if(args.length >= 3) {
					group = args[2];
				}
			}
		}
		
		if(port == -1)
			port = (short) daemon_conf.cs_port;
		
		if(maxagents == -1)
			maxagents = daemon_conf.maxagents;
		
		ClusterDaemonServer server = new ClusterDaemonServer(port);
		serverThread = new Thread(server);
		serverThread.start();
		
		ClusterDaemonYpConnection yp = new ClusterDaemonYpConnection(daemon_conf.yp_ip, (short) daemon_conf.yp_port, port, maxagents, group);
		yp.addMe();
		
		ClusterDaemon handler = null;
		APCConfiguration config = null;
		
		while(serverThread.isAlive()) {
			try {
				if(handler == null) {
					if(yp.getId() != -1)
						handler = new ClusterDaemon(yp.getId(), maxagents, group);
				} else if(config != null) {
					Thread.sleep(100);
				} else {
					config = server.getConfigs().take();
				}
			} catch (InterruptedException e) {
				// TODO handle exception
				// do nothing here.
			}
			
			if(config != null) {
				// handler not working yet? set the config and start the work.
				if(!handler.isWorking() && handler.getConfig() == null) {
					handler.setConfig(config);					
					if(config.first_config) {
						yp.addHandler(handler);
						yp.sendLine("list");
					}
				// handler finished working start the BeenutsAgentProcessCluster with the new configuration.
				} else if((!handler.isWorking() && handler.getConfig() != null) || handler.getConfig() != null && !handler.getConfig().first_config) {
					config = handler.getConfig();
					handler.setConfig(null);
						
				// TODO: Instantiate see what causes the exceptions in BeenutsAgentProcessCluster 
				// When using more than one AgentProcessCluster.
					BeenutsAgentProcessCluster apc = new BeenutsAgentProcessCluster();
					apc.initialize(config, String.valueOf(handler.getId()));
					config = null;
				}	
			}
		}
		
		String reason = "";
		server.kill();
		
		System.out.println("Exit with reason:" + reason);
		System.exit(0);
	}
	
	
}
