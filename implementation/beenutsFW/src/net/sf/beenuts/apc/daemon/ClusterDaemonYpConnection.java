package net.sf.beenuts.apc.daemon;

import net.sf.beenuts.net.YellowPagesTcpConnection;

/**
 * This class handles the agentProcessCluster connection to the yellow pages server. It registers
 * itself to the yp and reacts on the yp ping messages.
 * @author Tim Janus
 */
public class ClusterDaemonYpConnection extends YellowPagesTcpConnection  {

	public ClusterDaemonYpConnection(String ip, short yp_port, short my_port, int maxagents, String group) {
		super(ip, yp_port);
		this.my_port = my_port;
		this.maxagents = maxagents;
		this.group = group;		
	}

	@Override
	public void onHandle(Object result) {
		if(result instanceof String) {
			String str = (String)result;
			if(str.startsWith("<add>")) {
				id = Integer.parseInt(str.substring(5));
				String[] keys = {"maxagents", "group"};
				String[] values = {Integer.toString(maxagents), group};
				setProperty(id, keys, values);
			}
		}
		super.onHandle(result);
	}

	public void addMe() {
		addToList("mapc", my_port);
	}
	
	public int getId() {
		return id;
	}
	
	private String group;
	
	private short my_port;
	
	private int id = -1;
	
	private int maxagents;
}
