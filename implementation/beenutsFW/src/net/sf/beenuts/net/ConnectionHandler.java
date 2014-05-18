package net.sf.beenuts.net;

/**
 * Interface for dealing with connection events
 * @author Tim Janus
 */
public interface ConnectionHandler {
	/**
	 * Is called by a connection when the data for a task was received.
	 * This is the case when a send operation is finished or the connection
	 * receives new data from its reader.
	 * @param conn		Reference to the connection
	 * @param result	Object containing the resulting data of the task.
	 */
	public void dataReceived(Connection conn, Object result);
	
	/**
	 * Is called by a connection if an error occurs.
	 * @param conn	Reference to the connection causing the error.
	 * @param error	String with a description of the error.
	 */
	public void errorOccurred(Connection conn, String error);
	
	/**
	 * Is called by a connection if the connection was closed.
	 * @param conn	Reference to the connection which was closed.
	 */
	public void connectionClosed(Connection conn);
}
