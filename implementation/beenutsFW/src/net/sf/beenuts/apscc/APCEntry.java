package net.sf.beenuts.apscc;

/**
 * (A)gent (P)rocess (C)luster 
 * This class defines an entry for one AgentProcessCluster in the serverlist of the APSCC Frame.
 * @author Tim Janus
 */
public class APCEntry {
	public APCEntry(int id, String ip, short port, int maxAgents, String group) {
		this.ip = ip;
		this.port = port;
		this.id = id;
		this.maxAgents = maxAgents;
		this.group = group;
		
		// TODO: Check IP address validly
		mValid = true;
	}
	
	public APCEntry(String stringRepresentation) {
		String [] args = stringRepresentation.split(";");
		
		if(args.length < 4) {
			mValid = false;
		} else if(mValid = args[3].equalsIgnoreCase("mapc")) {
			id = Integer.parseInt(args[0]);
			ip = args[1];
			port = Short.parseShort(args[2]);
			
			for(int i=4; i<args.length; i++) {
				String [] pair = args[i].split("=");
				if(pair.length == 2) {
					if(pair[0].equalsIgnoreCase(PROPERTY_NAME_MAXAGENTS)) {
						maxAgents = Integer.parseInt(pair[1]);
					} else if(pair[0].equalsIgnoreCase(PROPERTY_NAME_GROUP)) {
						group = pair[1];
					}
				} // else ignore
			}
		}
	}
	
	@Override
	public String toString() {
		return ip + ":" + port + " - [" + group + "] - MAX: " + maxAgents;
	}
	
	public String getIp() {
		return ip;
	}
	
	public short getPort() {
		return port;
	}
	
	public int getMaxAgents() {
		return maxAgents;
	}
	
	public boolean isValid() {
		return mValid;
	}
	
	public String getGroup() {
		return group;
	}
	
	@Override
	public boolean equals(Object other) {
		if(! (other instanceof APCEntry))
			return false;
		APCEntry oth = (APCEntry)other;
		return this.ip.equals(oth.ip) && this.port == oth.port;
	}
	
	public int getId() {
		return id;
	}
	
	public static final String PROPERTY_NAME_MAXAGENTS = "maxagents";
	
	public static final String PROPERTY_NAME_GROUP = "group";
	
	private String ip;
	
	private short port;
	
	private int maxAgents;
	
	private String group;
	
	private boolean mValid;
	
	private int id;
}
