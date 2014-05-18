package test.ucapc;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.*;

import test.ucapc.events.APCEvent;

import net.sf.beenuts.ap.*;
import net.sf.beenuts.apc.*;
import net.sf.beenuts.apc.net.APCConnection;
import net.sf.beenuts.apc.net.APCServer;
import net.sf.beenuts.apc.util.APCConfiguration;
import net.sf.beenuts.apr.*;


/**
 * test implementation of an agentProcessCluster. the core supports 
 * asynchronous message handling.
 * 
 * paused until a blocking agentProcessCluster is finished.
 * 
 * @author Thomas Vengels
 *
 */
public class TestAPCAsync implements AgentProcessCluster, Runnable {

	AgentProcessScheduler sched = null;
	APR apr = null;
	BlockingQueue<APCEvent> events = null;
	APCServer server = null;
	boolean exit = false;
	APRState state = new APRState();
	
	class APRState {
		public boolean server_ready = false;
		public boolean apr_ready = false;
	}
	
	public boolean testReady = false;
	
	@Override
	public Collection<String> getLocalAgents() {
		return null;
	}

	@Override
	public Collection<String> getAgents() {
		return null;
	}
	
	public TestAPCAsync() {
		super();
		this.events = new LinkedBlockingQueue<APCEvent>();
	}
		
	@Override
	public void run() {
		try {
			while(!exit) {
				APCEvent evnt = events.take();
				handleEvent(evnt);
			}
		} catch (InterruptedException intErr) {
			System.err.println("agentProcessCluster critical error: thread interruption\n"+intErr);
		}
	}
	
	protected void handleEvent(APCEvent e) {
		switch(e.event_id) {
		case APCEvent.E_APCINIT:
			onAPCInit(e);
			break;
		
		case APCEvent.E_APCSHUTDOWN:
			onAPCShutdown(e);
			break;
			
		case APCEvent.E_CONNECT:
			onAPCConnect(e);
			break;
			
		default:
			System.err.println("agentProcessCluster event handler: unknown event "+e.event_id);
		}
	}
	
	public void testSimulation() {
		System.out.println("running simulation");
		// perform a simulation.
		for (int i = 0; i < 10; i++) {			
			//apr.simulationStep();
			
			// sleep some time
			try {
				Thread.sleep(2000);
			} catch (Exception e) {
				
			}
		}
		
		sched.stopScheduler();		
	}
	
	public void queueEvent(APCEvent event) {
		this.events.offer(event);
	}
	
	/**
	 * initialization handler, agentProcessCluster is setup here
	 * 
	 * @param e
	 */
	protected void onAPCInit(APCEvent e) {
		System.out.println("agentProcessCluster init event");
		init();
		this.testReady = true;
	}
	
	/**
	 * shutdown handler of an agentProcessCluster
	 * 
	 * @param e
	 */
	protected void onAPCShutdown(APCEvent e) {
		System.out.println("agentProcessCluster shutdown event");
		this.exit = true;
	}
	
	/**
	 * event handler, invoked whenever another agentProcessCluster
	 * connected to this agentProcessCluster.
	 * 
	 * @param e
	 */
	protected void onAPCConnect(APCEvent e) {
		System.out.println("agentProcessCluster got connection from another agentProcessCluster");
	}
	
	/**
	 * event handler for incoming messages from other
	 * agentProcessCluster's.
	 * 
	 */
	protected void onAPCRecieve(APCEvent e) {
		
	}
	
	/**
	 * real agentProcessCluster initialization code, called by an E_APCINIT event handler
	 */
	protected void init() {
		
		// create a server
		server = new APCServer();
		server.init(4040, this);
		state.server_ready = true;
		
		// create apr
		//apr = new APR();
		state.apr_ready = true;
		
		// create some agents
		String[] agNames = { "Alice", "Bob", "Charlie", "Dave", "Emma" };				
		LinkedList<BeenutsAgentProcess> agents = new LinkedList<BeenutsAgentProcess>();
		
		// create a scheduler
		sched = new StrrScheduler();
		sched.startScheduler();
		
		// create agents
		for (String agName : agNames) {
			BeenutsAgentProcess ag = null;//new BeenutsAgentArchitecture(apr, agName);
			TestAgent agImpl = new TestAgent();			
			ag.setAgentArchitecture(agImpl);
			agImpl.init(ag);
			agents.add(ag);
			sched.addAgent(ag);
		}
	}

	@Override
	public void initialize(APCConfiguration config, String uniqueName) {
		this.queueEvent( new APCEvent( APCEvent.E_APCINIT ));
	}

	@Override
	public void shutdown() {
		this.queueEvent( new APCEvent( APCEvent.E_APCSHUTDOWN));
	}

	@Override
	public void newAPCConnection(APCConnection con, String dstAPC) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void sendAgentMessage(Object message, Collection<String> recipients) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void receiveAgentMessage(Object message,
			Collection<String> recipients) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendAgentAction(String agent, String action) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendAgentPerception(String agent, String perception) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void receiveAgentPerception(String dstAgent, String perception) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void receiveAgentAction(String srcAgent, String action) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void waitReady() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public APR getAPR() {
		// TODO Auto-generated method stub
		return null;
	}	

}
