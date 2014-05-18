package net.sf.beenuts.apc;

import java.util.*;

import net.sf.beenuts.ap.*;


/**
 * a single threaded round robin scheduler for agent processes.
 * does not support time slicing, is not aware of priorities.
 * 
 * @author Thomas Vengels
 *
 */
public class StrrScheduler extends Thread implements AgentProcessScheduler {

	// scheduler lock
	Object				lock;
	
	// active: list of agents on schedule
	// waiting: list of agents to become active
	// dying: list of agents to become removed
	List<MyAPRunner>	active, waiting, dying;
	Set<String> agents;
	boolean terminate;
	
	public StrrScheduler() {
		lock = new Object();
		active = new LinkedList<MyAPRunner>();
		waiting = new LinkedList<MyAPRunner>();
		dying = new LinkedList<MyAPRunner>();
		terminate = false;
		agents = new HashSet<String>();
	}
	
	class MyAPRunner implements AgentProcessRunner {

		AgentProcess ag;
		
		public MyAPRunner(StrrScheduler owner, AgentProcess ag) {
			this.ag = ag;
			ag.setRunner(this);
		}
		
		@Override
		public void runMe() {
			// inform the scheduler to get active
			synchronized (lock) {
				lock.notifyAll();
			}
		}

		@Override
		public AgentProcess getAgentProcess() {
			return this.ag;
		}

		@Override
		public void enterCritical() {
			// not required by this type of scheduler
		}

		@Override
		public void leaveCritical() {
			// not required by this type of scheduler
		}
		
	}
	
	@Override
	public void addAgent(AgentProcess ag) {
		// queue unknown agent into list of waiting agents
		synchronized (lock) {
			if (!agents.contains(ag.getName())) {
				agents.add(ag.getName());
				waiting.add(new MyAPRunner(this,ag));
			}
			lock.notifyAll();
		}		
	}

	@Override
	public void removeAgent(AgentProcess ag) {
		synchronized(lock) {
			String agName = ag.getName();
			if (agents.contains(agName)) {
				agents.remove( agName );
				// must also remove agentProcessCluster from active queue
				// TODO: remove agent
			}
		}
		// removing an agent does not cause new scheduler activities
		ag.getAgentArchitecture().shutdown();
	}

	@Override
	public void startScheduler() {
		this.start();		
	}

	@Override
	public void stopScheduler() {
		this.terminate = true;
		synchronized (lock) {
			lock.notifyAll();
		}
		int time = 10000;
		try {
			this.join(time);
		} catch (Exception e) {
			System.err.println("strrs: could not terminate within time ("+time+"ms)");
		}
	}

	@Override
	public void run() {
		while( !terminate ) {
			try {
				if (!terminate) {
					// wait 100ms or until something happens
					synchronized (lock) {
						lock.wait( 100 );
						active.addAll(waiting);
						waiting.clear();
					}
					
					// cycle through each agents
					for (MyAPRunner ag : active) {
						// agent decides in execCycle if there is something to do
						ag.getAgentProcess().execCycle();
					}
					
					// TODO: handle dying list
					
				}
			} catch (InterruptedException eInt) {
				System.err.print("\n strrs interruption!\n"+eInt);
			} /*catch (Exception e) {
				System.err.print("\n strrs error!\n"+e);
				System.err.print(e.getStackTrace());
			}	
			*/			
		}
		if (terminate) {
			System.err.println("strrs: graceful shutdown!");
		}
	}

	@Override
	public void initialize(int maxConcurrency, int cpuTime) {
		// not required
	}
}
