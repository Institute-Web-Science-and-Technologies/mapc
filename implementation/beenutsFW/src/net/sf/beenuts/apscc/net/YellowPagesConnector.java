package net.sf.beenuts.apscc.net;


import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JOptionPane;

import net.sf.beenuts.apscc.APCEntry;
import net.sf.beenuts.net.Connection;
import net.sf.beenuts.net.ConnectionHandler;
import net.sf.beenuts.net.YellowPagesTcpConnection;

/**
 * This class is responsible for the communication with the yellow pages server after a connection
 * is established.
 * @author Tim Janus
 */
public class YellowPagesConnector implements ConnectionHandler {
	/**
	 * The interface between the connection to the yellowPages server and the view.
	 * @author Tim Janus
	 */
	public interface YellowPagesView {
		JList<APCEntry> getServerList();
		DefaultListModel<APCEntry> getServerListModel();
	}
	
	public YellowPagesConnector(String ip, short port, YellowPagesView view) {
		communication = new YellowPagesTcpConnection(ip, port);
		communication.addHandler(this);
		this.view = view;
	}
	
	public void asyncGetServerList(APCEntry actualSelected) {
		mActualSelected = actualSelected;
		communication.sendLine("list");
	}
	
	@Override
	public void dataReceived(Connection conn, Object result) {
		if(result instanceof String) {
			String received = (String)result;
			if(received.startsWith("<add>")) {
				
			} else if(received.startsWith("<list>")) {
				onList(received.substring(6));
			}
		}
	}
	
	@Override
	public void errorOccurred(Connection conn, String error) {
		JOptionPane.showMessageDialog(null, error);
	}
	
	@Override
	public void connectionClosed(Connection conn) {
		view.getServerListModel().clear();
	}

	private void onList(String para) {
		int selectedIndex = -1;
		view.getServerListModel().clear();
		String [] servers = para.split("\\|");
		for(String server : servers) {
			APCEntry actEntry = new APCEntry(server);
			if(!actEntry.isValid())
				continue;
			
			if(actEntry.equals(mActualSelected)) {
				selectedIndex = view.getServerListModel().getSize();
			}
			view.getServerListModel().addElement(actEntry);
		}
		view.getServerList().setSelectedIndex(selectedIndex);
	}
	
	private APCEntry mActualSelected;
	
	private YellowPagesTcpConnection communication;
	
	private YellowPagesView view;

}
