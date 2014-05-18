package net.sf.beenuts.apscc.net;

import java.io.Serializable;

import net.sf.beenuts.net.Connection;

/**
 * This connection is established when a APSCC wants an AgentProcessCluster to start
 * a simulation
 * 
 * @author Tim Janus
 */
public class APCConfigureConnection extends Connection {

	public static final String TASK_CONFIGURE = "CONFIGURE";
	
	public APCConfigureConnection(String ip, short port, Serializable configuration) {
		super(ip, port, Connection.OBJECT_BASED_CONNECTION);
		this.configuration = configuration;
	}
	
	public void startConfiguration() {
		sendObject(configuration);
	}
	
	@Override
	protected void onHandle(Object obj) {
		if(obj instanceof Boolean) {
			Boolean b = (Boolean)obj;
			if(b)
				internalStop();
		}
	}
	
	private Serializable configuration;
}
