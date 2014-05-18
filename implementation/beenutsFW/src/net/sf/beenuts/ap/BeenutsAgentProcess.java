package net.sf.beenuts.ap;

import java.util.*;

import eis.iilang.Action;

import net.sf.beenuts.apc.*;
import net.sf.beenuts.apr.*;

/**
 * Beenuts reference implementation of an agent process.
 *  
 * @author Thomas Vengels, Matthias Thimm
 */
public class BeenutsAgentProcess implements AgentProcess {
	
	
	protected APR apr;
	protected AgentArchitecture agArch;
	protected String agName;
	protected List<Object> perceptions = new LinkedList<Object>();
	protected AgentProcessRunner runner;	
	protected boolean lastResult;
	
	public BeenutsAgentProcess(APR apr, String agName, AgentProcessScheduler sched, AgentArchitecture ap) {
		this.lastResult = false;
		this.agName = agName;
		this.apr = apr;
		this.agArch = ap;
		ap.init(this);
		sched.addAgent(this);
		apr.signOn(this);		
	}
	
	@Override
	public void setAPR(APR apr ) {
		this.apr = apr;
	}
	
	@Override
	public void setRunner(AgentProcessRunner runner) {
		this.runner = runner;
	}

	@Override
	public void perceive(Object perception) {
		// new perception, has to be queued first,
		// then inform the scheduler to run
		// the agent.
		// note: caller is aprl, so we do not have
		// to protect the runner from preemption here
		synchronized (perceptions) {
			this.perceptions.add(perception);
		}
		this.runner.runMe();
	}

	@Override
	public String getName() {
		return this.agName;
	}

	@Override
	public AgentArchitecture getAgentArchitecture() {
		return this.agArch;
	}

	@Override
	public void send(Object message, Collection<String> recipients) {
		// to prevent a deadlock, enter critical mode (scheduler
		// will not preempt) and send messages to aprl
		this.runner.enterCritical();
		this.apr.sendMessage(this.agName, message, recipients);
		this.runner.leaveCritical();
	}

	@Override
	public void act(Object action) {
		// to prevent a deadlock, enter critical mode (scheduler
		// will not preempt) and commit action to aprl
		if(action instanceof Action) {
			this.runner.enterCritical();
			this.apr.sendAction(this.agName, action);
			this.runner.enterCritical();
		} else {
			throw new ClassCastException("The Action object of an Beenuts Agent must extend the eis.ilang.Action");
		}
		
	}

	@Override
	public void execCycle() {
		assert (agArch != null);

		Object per = null;
		
		// access to shared ressource, must avoid preemption here
		this.runner.enterCritical();
		synchronized (perceptions) {
			if (this.perceptions.size() > 0)
				per = this.perceptions.remove(0);
		}
		this.runner.leaveCritical();
		
		// run agent iff there is really something to do
		if (( per != null) || !lastResult) {
			lastResult = this.agArch.cycle(per);
		} 
	}

	@Override
	public void setAgentArchitecture(AgentArchitecture agentProcess) {
		this.agArch = agentProcess;
	}

	@Override
	public boolean canSleep() {	
		return ((this.perceptions.size() == 0) && lastResult);
	}
}
