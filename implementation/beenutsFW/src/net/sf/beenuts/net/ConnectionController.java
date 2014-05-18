package net.sf.beenuts.net;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;
import javax.swing.Timer;

/**
 * This class manages connections. It calls the getResults method on the connection
 * so that every ConnectionHandler gets informed if a connection got the needed
 * result.
 * Implemented as Singleton.
 * @author Tim Janus
 */
public class ConnectionController {
	
	/** Singleton instance. */
	private static ConnectionController instance;
	
	private List<Connection> to_add = new LinkedList<Connection>();
	private List<Connection> to_remove = new LinkedList<Connection>();
	private List<Connection> connections = new LinkedList<Connection>();
	
	private static int TIMER_DELAY = 33;
	
	private ConnectionController() {
		Timer timer = new Timer(ConnectionController.TIMER_DELAY, new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				synchronized (to_add) {
					connections.addAll(to_add);
					to_add.clear();	
				}
				
				for(Connection c : connections)
					c.update();
				
				synchronized (to_remove) {
					for(Connection c : to_remove)
						connections.remove(c);
				}
			}
		});
		timer.setRepeats(true);
		timer.start();
	}
	
	
	public static ConnectionController getInstance() {
		if(instance == null)
			instance = new ConnectionController();
		return instance;
	}
	
	public void registerConnection(Connection connection) {
		synchronized (to_add) {
			to_add.add(connection);
		}
	}
	
	public void unregisterConnection(Connection connection) {
		synchronized (to_remove) {
			to_remove.add(connection);
		}
		connection.internalStop();
	}
	
	public void shutdown() {
		for(Connection conn : connections) {
			conn.internalStop();
		}
		connections.clear();
	}
	

}
