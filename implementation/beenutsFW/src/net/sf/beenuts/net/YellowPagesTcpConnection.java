package net.sf.beenuts.net;

/**
 * This class encapsulates a connection to the yellow pages server.
 * The class is responsible for the low level communication.
 * @author Tim Janus
 */
public class YellowPagesTcpConnection extends Connection{
		
	/**
	 * Ctor
	 * @param ip	Ip address of the YellowPages Server
	 * @param port 	Port of the YellowPages Server
	 */
	public YellowPagesTcpConnection(String ip, short port) {
		super(ip, port, Connection.LINE_BASED_CONNECTION);
	}

	public void addToList(String application, short port) {
		sendLine("add " + port + " " + application);
	}
	
	public void setProperty(int id, String key, String value) {
		sendLine("set " + id + " " + key + "=" + value);
	}
	
	public void setProperty(int id, String [] key, String [] value) {
		String key_value_part = "";
		for(int i=0; i<key.length; ++i) {
			if(i!=0)
				key_value_part += " ";
			key_value_part += key[i] + "=" + value[i];
		}
		sendLine("set " + id + " " + key_value_part);
	}
	
	@Override
	protected void onHandle(Object obj) {
		String task = (String)obj;
		if(task.equalsIgnoreCase("<ping>")) {
			sendLine("pong");
		}
		super.onHandle(obj);
	}
}
