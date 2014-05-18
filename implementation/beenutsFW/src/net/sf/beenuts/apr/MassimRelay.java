package net.sf.beenuts.apr;

import net.sf.beenuts.ap.*;

/**
 * Implementation of APR for Massim
 * 
 * @author dhoelzgen
 *
 */
public class MassimRelay extends APR {

	protected MassimAdapter adapter;
	
	@Override
	public void signOff(AgentProcess agent) {
		super.signOff(agent);
		this.getAdapter().close(agent.getName());
	}

	@Override
	public boolean signOn(AgentProcess agent) {
		if (!super.signOn(agent)) return false;
		// must register agent
		// should be synchronized, or r/w locked
		this.agents.add(agent);
		this.agentMap.put(agent.getName(), agent);

		// TODO Make connection name (2nd argument) configurable
		this.getAdapter().open(agent.getName(), agent.getName());
		
		// TODO URGENT Check return value
		return true;
	}
	
	@Override
	public void sendAction(String agentName, Object action) {
		this.getAdapter().act(agentName, action);
	}
		
	protected MassimAdapter getAdapter() {
		if (this.adapter == null) {
			this.adapter = new MassimAdapter(this);
		}
		return this.adapter;
	}

}
