package net.sf.beenuts.apc;

import java.net.Socket;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.sf.beenuts.ap.*;
import net.sf.beenuts.apc.net.APCConnection;
import net.sf.beenuts.apc.net.APCTransfer;
import net.sf.beenuts.apc.net.APCServer;
import net.sf.beenuts.apc.net.transfer.AgentAction;
import net.sf.beenuts.apc.net.transfer.AgentMessage;
import net.sf.beenuts.apc.net.transfer.AgentPerception;
import net.sf.beenuts.apc.util.APCConfiguration;

import net.sf.beenuts.apr.*;
import net.sf.beenuts.util.Pair;

/**
 * test implementation of a local agentProcessCluster node
 * 
 * @author Thomas Vengels
 *
 */
public class BeenutsAgentProcessCluster implements AgentProcessCluster {

	/** TODO add description */
	private AgentProcessScheduler sched = null;
	/** TODO add description */
	private APR apr = null;
	/** TODO add description */
	private APCServer server = null;
	/** TODO add description */
	private APRState state = new APRState();
	/** TODO add description */
	public boolean testReady = false;
	
	class APRState {
		public String name;
		
		public boolean server_ready = false;
		public boolean apr_ready = false;
		
		// agent locations
		public Map<String,String> ag2apcMap = new HashMap<String,String>();
		public Set<String> allAgents = new HashSet<String>();
		public Set<String> localAgents = new HashSet<String>();
		
		// agentProcessCluster connections
		public LinkedList<APCConnection> connectedAPC = new LinkedList<APCConnection>();
		public Map<String,APCConnection> apcMap = new HashMap<String,APCConnection>();
		 
		// apr details
		public boolean isRSR;		// apr is a rsr
		public boolean isLSR;		// apr is an lsr
		public String apcLSRName;	// identifier for lsr agentProcessCluster, or null			
	}
	
	
	/**
	 * simple thread that establishes an connection concurrently
	 * (not blocking the connection inititator)
	 * 
	 * @author Thomas Vengels
	 *
	 */
	class APRConnectThread extends Thread {
		public APRConnectThread(String apcId, String host, int port) {
			this.port = port;
			this.host = host;
			this.apcId = apcId;
		}
		
		int port = 0;
		String host = "";
		String apcId = "";
		
		@Override 
		public void run() {
			boolean connected = false;
			while (!connected) {
				try {
				Socket sock = new Socket(host, port);
				connected = true;
				addAPCConnection( apcId, sock );
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				// sleep 5 seconds if connection fails
				if (!connected) {
					try {
						System.out.println("connection to AgentProcessCluster failed, reconnecting in 5 seconds");
						Thread.sleep(5000);
					} catch (Exception e) {
						// don't care if sleep gets interrupted
					}
				}
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see net.sf.beenuts.apc.AgentProcessCluster#getLocalAgents()
	 */
	@Override
	public Collection<String> getLocalAgents() {
		return null;
	}

	/* (non-Javadoc)
	 * @see net.sf.beenuts.apc.AgentProcessCluster#getAgents()
	 */
	@Override
	public Collection<String> getAgents() {
		return this.state.allAgents;
	}
	
	/**
	 * Creates a new BeenutsAgentProcessCluster
	 */
	public BeenutsAgentProcessCluster() {
		super();
	}
			
	/* (non-Javadoc)
	 * @see net.sf.beenuts.apc.AgentProcessCluster#initialize(net.sf.beenuts.apc.util.APCConfiguration, java.lang.String)
	 */
	@Override
	public void initialize(APCConfiguration config, String uniqueName) {
		if (config == null) { 
			System.err.println("agentProcessCluster error: no configuration given");
			return;
		}
		
		// keep unique name
		state.name = uniqueName;
		
		// create apr
		try { 
			System.out.println("apr class: "+config.apr_class);
			apr = (APR) Class.forName(config.apr_class).newInstance();
		} catch (Exception e) {
			System.err.println("apr loader error!");
			e.printStackTrace();
		}
		if (apr instanceof LocalSimulationRelay)
			state.isLSR = true;
		else
			state.isLSR = false;
		if (apr instanceof RemoteSimulationRelay)
			state.isRSR = true;
		else
			state.isRSR = false;
		
		assert(apr != null);
		apr.setAPC(this);
		state.apr_ready = true;
		
		// create scheduler and start it
		try {
		sched = (AgentProcessScheduler) Class.forName(config.schedClass).newInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
		sched.initialize(config.schedMaxConcurrency, config.schedCpuTime);
		sched.startScheduler();
		
		// create server for incoming agentProcessCluster connections
		if (config.server_enable) {
			this.server = new APCServer();
			server.init(config.server_port, this);
			this.state.server_ready = true;
		} else {
			this.state.server_ready = true;
		}
		
		// connect to other agentProcessCluster's
		// must enumerate over whole entry set
		for (Entry<String, Pair<String, Integer> > e : config.connect2APC.entrySet()) {
			String apcId = e.getKey();
			String host = e.getValue().getKey();
			int port = e.getValue().getValue();
			APRConnectThread ct = new APRConnectThread(apcId,host,port);
			ct.run();
		}

		// create local agents
		for (String agName : config.agentHosts.keySet()) {
			
			System.out.println("creating "+agName);
			
			String host = config.agentHosts.get(agName);
			
			// keep agent name
			this.state.allAgents.add(agName);
			this.state.ag2apcMap.put(agName, host);
			
			// start agent here:
			if (host.equalsIgnoreCase(uniqueName)) {
				
				this.state.localAgents.add(agName);
								
				AgentArchitecture agImpl = null;
				try {
					agImpl = (AgentArchitecture) Class.forName(config.agentClass.get(agName)).newInstance();
				} catch (Exception e) {
					System.err.println("AgentProcessCluster Fatal Error! Cannot create Agent\n"+
							"Agent name: "+agName+"\n"+
							"Agent class: "+config.agentClass.get(agName));
					System.exit(0);
				}
				
				BeenutsAgentProcess ag = new BeenutsAgentProcess(apr, agName,sched,agImpl);								
				/*
				ag.setAgentArchitecture(agImpl);
				agImpl.init(ag);
				*/
				
				System.out.println(uniqueName + " starting agent " + agName);
				
			}
		}
	}

	/* (non-Javadoc)
	 * @see net.sf.beenuts.apc.AgentProcessCluster#shutdown()
	 */
	@Override
	public void shutdown() {
		this.sched.stopScheduler();
		System.exit(0);
	}

	/* (non-Javadoc)
	 * @see net.sf.beenuts.apc.AgentProcessCluster#sendAgentMessage(java.lang.Object, java.util.Collection)
	 */
	@Override
	public void sendAgentMessage(Object message, Collection<String> recipients) {		
		// simply send message to all other agentProcessCluster's
		// TODO: optimize to not waste network traffic
		synchronized (this) {
			for (APCConnection con : this.state.connectedAPC) {
				con.send( new AgentMessage(message, recipients ));
				System.out.println("relayed ag message");
			}
		}		 
	}
	
	/* (non-Javadoc)
	 * @see net.sf.beenuts.apc.AgentProcessCluster#receiveAgentMessage(java.lang.Object, java.util.Collection)
	 */
	@Override
	public void receiveAgentMessage(Object message, Collection<String> local_recipients) {
		// local_recipients might be out of date, apr must check against that
		if (this.state.apr_ready)
			apr.localRelay(message, local_recipients);
	}

	/* (non-Javadoc)
	 * @see net.sf.beenuts.apc.AgentProcessCluster#newAPCConnection(net.sf.beenuts.apc.net.APCConnection, java.lang.String)
	 */
	@Override
	public void newAPCConnection(APCConnection con, String dstAPC) {
		synchronized (this) {
			this.state.connectedAPC.add(con);
			this.state.apcMap.put(dstAPC,con);
		}
	}

	/* (non-Javadoc)
	 * @see net.sf.beenuts.apc.AgentProcessCluster#getName()
	 */
	@Override
	public String getName() {
		return state.name;
	}

	/* (non-Javadoc)
	 * @see net.sf.beenuts.apc.AgentProcessCluster#sendAgentAction(java.lang.String, java.lang.String)
	 */
	@Override
	public void sendAgentAction(String agent, String action) {
		if (this.state.isRSR) {
			APCConnection lsrCon = null;
			synchronized(this) {
				lsrCon = this.state.apcMap.get( this.state.apcLSRName );
			}
			
			if (lsrCon != null) {
				lsrCon.send( new AgentAction(agent,action));
			} else {
				//TODO: error: agentProcessCluster has no connection to lsr-agentProcessCluster node
			}
		} else {
			//TODO: error, agentProcessCluster node has no rsr-apr
		}
	} 

	/* (non-Javadoc)
	 * @see net.sf.beenuts.apc.AgentProcessCluster#sendAgentPerception(java.lang.String, java.lang.String)
	 */
	@Override
	public void sendAgentPerception(String agent, String perception) {
		if (this.state.isLSR) {
			// get agentProcessCluster of agent
			String agApc = this.state.ag2apcMap.get(agent);
			
			// get connection to that agentProcessCluster
			APCConnection rsrCon = null;
			synchronized(this) {
				rsrCon = this.state.apcMap.get( agApc );
			}
			
			if (rsrCon != null) {
				rsrCon.send( new AgentPerception(agent,perception));
			} else {
				//TODO: error: agentProcessCluster has no connection to lsr-agentProcessCluster node
			}			
		} else {
			//TODO: error, this agentProcessCluster does not have a lsr-apr
		}
	}

	/* (non-Javadoc)
	 * @see net.sf.beenuts.apc.AgentProcessCluster#receiveAgentPerception(java.lang.String, java.lang.String)
	 */
	@Override
	public void receiveAgentPerception(String srcAgent, String perception) {
		// only handle this if the apr is an rsr
		if (this.state.isRSR && this.state.localAgents.contains(srcAgent)) {
			RemoteSimulationRelay rsr = (RemoteSimulationRelay) this.apr;
			rsr.receivePerception(srcAgent, perception);
		}
	}

	/* (non-Javadoc)
	 * @see net.sf.beenuts.apc.AgentProcessCluster#receiveAgentAction(java.lang.String, java.lang.String)
	 */
	@Override
	public void receiveAgentAction(String srcAgent, String action) {
		// only handle this if the apr is an lsr
		if (this.state.isLSR) {
			 LocalSimulationRelay lsr = (LocalSimulationRelay) this.apr;
			 lsr.receiveAction(srcAgent, action);
		} else {
			//TODO: handle error here
		}
	}

	/* (non-Javadoc)
	 * @see net.sf.beenuts.apc.AgentProcessCluster#waitReady()
	 */
	@Override
	public void waitReady() {
		try {
			while (!this.state.apr_ready || !this.state.server_ready) {
				Thread.sleep(100);
			}
		} catch(InterruptedException e) {
			System.err.println("agentProcessCluster waitready:" + e);
		}
	}

	/* (non-Javadoc)
	 * @see net.sf.beenuts.apc.AgentProcessCluster#getAPR()
	 */
	@Override
	public APR getAPR() {		
		return this.apr;
	}
	

	/**
	 * TODO add JavaDoc
	 * @param apcId
	 * @param sock
	 */
	protected synchronized void addAPCConnection(String apcId, Socket sock) {
		// port created, create connection here
		APCConnection conn = new APCConnection();
		conn.init(sock, this);
		this.state.apcMap.put(apcId, conn);
		// send a hello
		conn.send( new APCTransfer(APCTransfer.T_HELLO ));
	}
}
