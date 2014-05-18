package net.sf.beenuts.apc;

import java.util.Vector;
import java.util.concurrent.Semaphore;

import net.sf.beenuts.ap.*;

/**
 * 
 * a multi-threaded round robin scheduler, supports active preemption
 * of agent threads.
 * 
 * @author Thomas Vengels
 *
 */
public class MtrrScheduler extends Thread implements AgentProcessScheduler {

	boolean		runScheduler = false;
	int			maxConcurrency = 1;
	int			cpuTime = 100;
	
	// index holding next agent that will receive some cpu time
	int		prioIndex = 0;
	Vector<MTRunner> agThreads = new Vector<MTRunner>();
	
	class MTRunner extends Thread implements AgentProcessRunner {

		// wait object, used to wait for cpu while not
		// in suspension state
		Semaphore 		waitSem;
		
		// low level agent interface
		AgentProcess	ag;
		
		public MTRunner(AgentProcess ag) {
			this.ag = ag;
			waitSem = new Semaphore(0);
		}
		
		@Override
		public void run() {
			while(true) {
				
				// agent waits for perception here
				try {
						waitSem.acquire();  // block until data is avail
						waitSem.drainPermits();
				} catch (Exception e) {
						// we do not care what exception is causes the trouble
						e.printStackTrace();
				}
				
				// agent processes perceptions here
				while(!ag.canSleep()) {
					ag.execCycle();
				}
			}
		}
		
		@Override
		public void runMe() {
			// permit on waitSem allows waiting agent to continue
			waitSem.release();
		}

		@Override
		public AgentProcess getAgentProcess() {
			return this.ag;
		}

		@Override
		public void enterCritical() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void leaveCritical() {
			// TODO Auto-generated method stub
			
		}				
	}
	
	@Override
	public void run() {
		while(this.runScheduler) {
			try {
				Thread.sleep(this.cpuTime);
				
				// implement a very simple scheduling policy: 
				// lower / increase priorities of agent.
				// this will not cause preemption, and on wide
				// cpu intervalls, everything should be fine
				synchronized (agThreads) {
					// increase priority index
					this.prioIndex += this.maxConcurrency;
					this.prioIndex %= (agThreads.size()+1);
					
					// set every thread to a low priority,
					// expect those who should be scheduled next
					for (int i = 0; i < agThreads.size(); i++) {
						
						// check if thread at index i might receive
						// a high priority
						MTRunner ag = agThreads.get(i);
						if ((i >= this.prioIndex) && (i < (this.prioIndex+this.maxConcurrency)))
							ag.setPriority(Thread.NORM_PRIORITY);
						else
							ag.setPriority(Thread.MIN_PRIORITY);
					}
				}
				 
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void addAgent(AgentProcess ag) {
		MTRunner mtr = new MTRunner(ag);
		ag.setRunner(mtr);
		mtr.start();
		synchronized(this.agThreads) {
			agThreads.add(mtr);
		}
	}

	@Override
	public void removeAgent(AgentProcess ag) {
		// TODO Auto-generated method stub

	}

	@Override
	public void startScheduler() {
		this.runScheduler = true;
		this.start();
	}

	@Override
	public void stopScheduler() {
		this.runScheduler = false;
		try {
			this.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void initialize(int maxConcurrency, int cpuTime) {
		this.maxConcurrency = maxConcurrency;
		this.cpuTime = cpuTime;
	}

}
