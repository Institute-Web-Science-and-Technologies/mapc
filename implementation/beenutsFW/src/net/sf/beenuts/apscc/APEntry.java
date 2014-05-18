package net.sf.beenuts.apscc;

import java.io.Serializable;

/**
 * (A)gent (P)rocess
 *	This class defines one entry for an AgentProcess in the Agentlist of the APSCC Frame.
 * 	@author Tim Janus
 */
public class APEntry implements Serializable {
	/**
	 * kill warnign
	 */
	private static final long serialVersionUID = -2442296689787633517L;

	public APEntry(String name, String password) {
		set(name, password);
	}
	
	public void set(String name, String password) {
		mName = name;
		mPassword = password;
	}
	
	@Override
	public String toString() {
		return "AgentProcess: " + mName + " - " + mPassword;
	}
	
	public String getName() {
		return mName;
	}
	
	public String getPassword() {
		return mPassword;
	}
	
	private String mName;
	
	private String mPassword;
}
