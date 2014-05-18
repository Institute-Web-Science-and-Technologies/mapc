package test.ucapc.events;

/**
 * event class, declaring all possible events
 * an agentProcessCluster implementation has to handle. all
 * 
 * @author Thomas Vengels
 *
 */
public class APCEvent {
	// events related to agentProcessCluster
	public static final int E_APCINIT = 1;
	public static final int E_APCSHUTDOWN = 2;

	// events related to inter-agentProcessCluster connection
	public static final int E_CONNECT = 100;
	public static final int E_DISCONNECT = 101;
	public static final int E_CONNECTIONLOST = 102;
	public static final int E_PING = 103;
	public static final int E_RECIEVE = 104;
	
	// events related to agent interaction
	public static final int E_AG_BROADCAST = 200;
	public static final int E_AG_MESSAGE = 201;
	public static final int E_AG_PERCEPTION = 202;
	public static final int E_AG_ACTION = 203;		
	
	// agentProcessCluster timer services
	public static final int E_TIMER = 300;
	
	// event-id, one of the statics
	public final int event_id;
	
	/**
	 * constructor assigning the base class apcevent
	 * fields.
	 * 
	 * @param id
	 */
	public APCEvent(int id) {
		this.event_id = id;
	}
	
	@Override
	public String toString() {
		return "generic AgentProcessCluster event code: "+this.event_id;
	}
}
