package net.sf.beenuts.apscc.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

import net.sf.beenuts.apscc.APSCCConfig;
import net.sf.beenuts.apscc.APCEntry;
import net.sf.beenuts.apscc.APEntry;
import net.sf.beenuts.apscc.net.APCConfigureConnection;
import net.sf.beenuts.apscc.net.YellowPagesConnector;
import net.sf.beenuts.apscc.net.YellowPagesConnector.YellowPagesView;
import net.sf.beenuts.net.Connection;
import net.sf.beenuts.net.ConnectionController;
import net.sf.beenuts.net.ConnectionHandler;

/**
 * The main windows of the (A)gent (P)rocess (S)uper (C)luster (C)onfigurator.
 * This class also handles the connection to an AgentProcessCluster for configuring purposes (TODO: Move in own class)
 * @author Tim Janus
 */
public class APSCCFrame extends JFrame implements ConnectionHandler, YellowPagesView {

	public static void main(String [] args) {
		APSCCFrame frame = new APSCCFrame();
		frame.setVisible(true);
	}

	public APSCCFrame() {
		this.setTitle("APSCC - (A)gent (P)rocess (S)uper (C)luster (C)onfigurator");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setBounds(100, 100, 600, 600);
		this.setMinimumSize(new Dimension(400, 600));
		
		createGui();
		registerListeners();
		
		ConnectionController.getInstance();
	}

	@Override
	public void dispose() {
		ConnectionController.getInstance().shutdown();
		super.dispose();
	}

	@Override
	public void dataReceived(Connection conn, Object result) {
		if(result instanceof Boolean) {
			JOptionPane.showMessageDialog(this, "Config transmitted.");
		}
	}

	@Override
	public void errorOccurred(Connection conn, String error) {
		JOptionPane.showMessageDialog(this, error, "An error occured.", JOptionPane.ERROR_MESSAGE);
	}

	@Override
	public void connectionClosed(Connection conn) {
		JOptionPane.showMessageDialog(this, conn + " closed.");
	}

	/**
	 * Helper method: Creates the gui of the main frame.
	 */
	private void createGui() {
		this.setLayout(new BorderLayout());
		
		configurationFileChooser = new JFileChooser();
		configurationFileChooser.setFileFilter(new FileFilter() {
			@Override
			public String getDescription() {
				return "*.xml";
			}
			
			@Override
			public boolean accept(File f) {
				return f.getName().endsWith(".xml");
			}
		});
		configurationFileChooser.setCurrentDirectory((new File(".")));
		
		JPanel ypRow = new JPanel();
		JLabel lblAddrYp = new JLabel("Addr. Yellow-pages server:");
		txtYellowPagesIp = new JTextField("127.0.0.1");
		btnYpConnect = new JButton("Connect");
		ypRow.add(lblAddrYp);
		ypRow.add(txtYellowPagesIp);
		ypRow.add(btnYpConnect);
		add(ypRow, BorderLayout.PAGE_START);
		
		JPanel listConfigPanel = new JPanel();
		listConfigPanel.setBorder(BorderFactory.createTitledBorder("(A)gent (P)rocess (C)luster List:"));
		listConfigPanel.setLayout(new BorderLayout());
		
		lstServer = new JList();
		lstServer.setMinimumSize(new Dimension(10, 50));
		lstServer.setModel(serverListModel = new DefaultListModel());
		JScrollPane serverListScroll = new JScrollPane(lstServer);
		listConfigPanel.add(serverListScroll, BorderLayout.CENTER);
		add(listConfigPanel);
		
		JPanel panConfiguration = createConfigurationPanel();
		listConfigPanel.add(panConfiguration, BorderLayout.PAGE_END);
		add(listConfigPanel, BorderLayout.CENTER);
		
		JPanel buttonFlow = new JPanel(new FlowLayout(FlowLayout.LEFT));
		btnLoadConfiguration = new JButton("Load Config...");
		buttonFlow.add(btnLoadConfiguration);
		
		btnSaveConfiguration = new JButton("Save Config...");
		buttonFlow.add(btnSaveConfiguration);
		
		btnStartSimulation = new JButton("Start Simulation");
		buttonFlow.add(btnStartSimulation);
		add(buttonFlow, BorderLayout.PAGE_END);
	}
	
	/**
	 * Helper method: Creates the Configuration Panel of the APSCC Mainframe.
	 * @return reference to the configuration panel of the APSCC Mainframe.
	 */
	private JPanel createConfigurationPanel() {
		JPanel panConfiguration = new JPanel();
		panConfiguration.setLayout(new BorderLayout());
		panConfiguration.setBorder(BorderFactory.createTitledBorder("Configuration:"));		
		
		JPanel agentListAndFieldAndBtn = new JPanel(new BorderLayout());
		
		lstAgents = new JList();
		lstAgents.setModel(agentListModel = new DefaultListModel());
		JScrollPane agentScroll = new JScrollPane(lstAgents);
		agentListAndFieldAndBtn.add(agentScroll, BorderLayout.CENTER);
		
		JPanel fieldAndBtn = new JPanel(new BorderLayout());
		fieldAndBtn.add(genAgentFields(), BorderLayout.PAGE_START);
		
		JPanel btnFlow = new JPanel(new FlowLayout(FlowLayout.LEFT));
		btnAddAgent = new JButton("Add Agent");
		btnFlow.add(btnAddAgent);
		
		btnEditAgent = new JButton("Edit Agent");
		btnFlow.add(btnEditAgent);
		fieldAndBtn.add(btnFlow, BorderLayout.PAGE_END);
		
		agentListAndFieldAndBtn.add(fieldAndBtn, BorderLayout.PAGE_END);
		agentListAndFieldAndBtn.setBorder(BorderFactory.createTitledBorder("Agents specific"));
		
		panConfiguration.add(agentListAndFieldAndBtn, BorderLayout.CENTER);
		panConfiguration.add(genGlobalConfigFields(), BorderLayout.PAGE_END);

		return panConfiguration;
	}

	private JPanel genAgentFields() {
		GridBagLayout gbl = new GridBagLayout();
		GridBagConstraints constraints = new GridBagConstraints();
		JPanel agentFields = new JPanel(gbl);
		constraints.gridwidth = 1;
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 1/3.0;
		constraints.weighty = 0;
		constraints.fill = GridBagConstraints.BOTH;
		JLabel lblAgent = new JLabel("Agent - Name:");
		gbl.setConstraints(lblAgent, constraints);
		agentFields.add(lblAgent);
		
		constraints.gridx = 1;
		constraints.weightx = 2/3f;
		txtAgentName = new JTextField();
		txtAgentName.setMinimumSize(new Dimension(100, 30));
		gbl.setConstraints(txtAgentName, constraints);
		agentFields.add(txtAgentName);
		
		constraints.gridx = 1;
		constraints.gridy = 2;
		txtAgentPassword = new JTextField();
		gbl.setConstraints(txtAgentPassword, constraints);
		agentFields.add(txtAgentPassword);
		
		constraints.gridx = 0;
		constraints.weightx = 1/3f;
		JLabel lblAgentPassword = new JLabel("Agent - Password:");
		gbl.setConstraints(lblAgentPassword, constraints);
		agentFields.add(lblAgentPassword);
		
		return agentFields;
	}
	
	private JPanel genGlobalConfigFields() {
		GridBagLayout gbl = new GridBagLayout();
		GridBagConstraints constraints = new GridBagConstraints();
		
		JPanel globalConfigPan = new JPanel(gbl);
		globalConfigPan.setBorder(BorderFactory.createTitledBorder("Global Config:"));
		
		constraints.gridx = 0; 
		constraints.gridy = 0;
		constraints.gridheight = 1;
		constraints.gridwidth = 1;
		constraints.weightx = 1/3f;
		constraints.fill = GridBagConstraints.BOTH;
		JLabel lblEnvironmentUrl = new JLabel("URL-Environment: ");
		gbl.setConstraints(lblEnvironmentUrl, constraints);
		globalConfigPan.add(lblEnvironmentUrl);
		
		constraints.gridx = 1;
		constraints.weightx = 2/3f;
		txtEnvironemntUrl = new JTextField();
		gbl.setConstraints(txtEnvironemntUrl, constraints);
		globalConfigPan.add(txtEnvironemntUrl);
		
		constraints.gridy = 1;
		txtAgentClass = new JTextField();
		gbl.setConstraints(txtAgentClass, constraints);
		globalConfigPan.add(txtAgentClass);
		
		constraints.gridx = 0;
		constraints.weightx = 1/3f;
		JLabel lblJarName = new JLabel("Agent-Class:");
		gbl.setConstraints(lblJarName, constraints);
		globalConfigPan.add(lblJarName);
		
		constraints.gridy = 2;
		JLabel lblAprClass = new JLabel("APR-Class");
		gbl.setConstraints(lblAprClass, constraints);
		globalConfigPan.add(lblAprClass);
		
		constraints.gridx = 1;
		constraints.weightx = 2/3f;
		txtRelayClass = new JTextField();
		gbl.setConstraints(txtRelayClass, constraints);
		globalConfigPan.add(txtRelayClass);
		
		constraints.gridy = 3;
		txtSchedulerClass = new JTextField();
		gbl.setConstraints(txtSchedulerClass, constraints);
		globalConfigPan.add(txtSchedulerClass);
		
		constraints.gridx = 0;
		constraints.weightx = 1/3f;
		JLabel lblSchedulerClass = new JLabel("Scheduler-Class:");
		gbl.setConstraints(lblSchedulerClass, constraints);
		globalConfigPan.add(lblSchedulerClass);
		
		constraints.gridy = 4;
		JLabel lblGroupName = new JLabel("Group:");
		gbl.setConstraints(lblGroupName, constraints);
		globalConfigPan.add(lblGroupName);
		
		constraints.gridx = 1;
		constraints.weightx = 2/3f;
		txtGroupName = new JTextField();
		gbl.setConstraints(txtGroupName, constraints);
		globalConfigPan.add(txtGroupName);
		
		return globalConfigPan;
	}
	
	/**
	 * Helper method: Registers the listeners which describe the logic of the APSCC Mainframe.
	 */
	private void registerListeners() {
		// Get actual server list every 3 seconds.
		Timer timer = new Timer(3000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(connector != null) {
					connector.asyncGetServerList((APCEntry)lstServer.getSelectedValue());
				}
			}
		});
		timer.setRepeats(true);
		timer.start();
		
		btnStartSimulation.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					onStartSimulationClicked();
				} catch(ParserConfigurationException ex) {
					errorOccurred(null, "Cant start simulation: " + ex.getMessage());
				}
			}
		});
		
		btnYpConnect.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				startConnection();
			}
		});
		
		txtYellowPagesIp.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent event) {
				if(event.getKeyCode() == KeyEvent.VK_ENTER) 
					startConnection();
			}
		});
		
		btnAddAgent.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				onAgentAdd();
			}
		});
		
		btnEditAgent.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				onAgentEdit();
			}
		});
		
		lstAgents.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				onAgentSelectionChanged();
			}
		});
		
		btnSaveConfiguration.addActionListener(new ActionListener() {		
			@Override
			public void actionPerformed(ActionEvent e) {
				onSaveConfiguration();
			}
		});
		
		btnLoadConfiguration.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				onLoadConfiguration();
			}
		});
	}
	
	/**
	 * Helper method: Shows an open file dialog and tries to load the selected xml file as
	 * APSCC Configuration file.
	 */
	private void onLoadConfiguration() {
		configurationFileChooser.setCurrentDirectory(new File("."));
		int result = configurationFileChooser.showOpenDialog(this);
		if(result == JFileChooser.APPROVE_OPTION) {
			APSCCConfig config = APSCCConfig.fromFile(configurationFileChooser.getSelectedFile().getName());
			agentListModel.clear();
			for(APEntry entry : config.getAgents())
				agentListModel.addElement(entry);
			txtEnvironemntUrl.setText(config.getEnvironment());
			txtAgentClass.setText(config.getAgentClass());
			txtRelayClass.setText(config.getRelayClass());
			txtSchedulerClass.setText(config.getSchedulerClass());
			txtGroupName.setText(config.getGroup());
			}
	}
	
	/**
	 * Helper method: creates a dom document of the actual configuration and saves it to an user selected
	 * file.
	 */
	private void onSaveConfiguration() {
		configurationFileChooser.setCurrentDirectory(new File("."));
		int result=configurationFileChooser.showSaveDialog(this);
		if(result == JFileChooser.APPROVE_OPTION) {
            try {
            	Document document = createConfigurationObject().toDom();
            	
            	// ---- Use a XSLT transformer for writing the new XML file ----
                Transformer transformer = TransformerFactory.newInstance().newTransformer();
                DOMSource        source = new DOMSource( document );
                FileOutputStream os     = new FileOutputStream( configurationFileChooser.getSelectedFile() );
                StreamResult     strResult = new StreamResult( os );
                transformer.transform( source, strResult );
            } catch(TransformerConfigurationException ex) {
            	errorOccurred(null, "Transformer Config: " + ex.getMessage());
            } catch(FileNotFoundException ex) {
            	errorOccurred(null, "File not found: " + ex.getMessage());
            } catch(TransformerException ex) {
            	errorOccurred(null, "Transformer: " + ex.getMessage());
            } catch (ParserConfigurationException ex) {
            	errorOccurred(null, "During Document creation (Saving): " + ex.getMessage());
            }
		}
	}

	/**
	 * Helper method: Create a dom document instance representing the actual configuration of the
	 * APSCC Frame.
	 * @return reference to the configuration instance.
	 */
	private APSCCConfig createConfigurationObject() {
		APSCCConfig config = new APSCCConfig();
		config.setEnvironment(txtEnvironemntUrl.getText());
		config.setAgentClass(txtAgentClass.getText());
		config.setRelayClass(txtRelayClass.getText());
		config.setSchedulerClass(txtSchedulerClass.getText());
		config.setGroup(txtGroupName.getText());
		for(int i=0; i<agentListModel.getSize(); ++i) {
			config.getAgents().add((APEntry)agentListModel.get(i));
		}
		return config;
	}
	
	/**
	 * Helper method: Callback called if the selection of the agent list changes. Update the 
	 * associated textfields
	 */
	private void onAgentSelectionChanged() {
		if(lstAgents.getSelectedValue() != null) {
			APEntry actual = (APEntry)lstAgents.getSelectedValue();
			txtAgentName.setText(actual.getName());
			txtAgentPassword.setText(actual.getPassword());
		}
	}
	
	/**
	 * Helper method: called if the edit agent button is clicked. Updates the actual selected AgentProcess-Listentry
	 * with the data in the textfields.
	 */
	private void onAgentEdit() {
		if(lstAgents.getSelectedValue() == null) {
			JOptionPane.showMessageDialog(this, "No Agent selected.");
			return;
		}
		
		APEntry actual = (APEntry)lstAgents.getSelectedValue();
		if(agentNameExists(txtAgentName.getText(), actual)) {
			JOptionPane.showMessageDialog(this, "Agent with name '" + txtAgentName.getText() + "' already exist." );
		} else {
			actual.set(txtAgentName.getText(), txtAgentPassword.getText());
			lstAgents.repaint();
		}
	}
	
	/**
	 * Helper method: called if the add agent button is clicked. Adds an new entry to the agent list using
	 * the data in the textfields.
	 */
	private void onAgentAdd() {
		if(txtAgentName.getText().isEmpty()) {
			JOptionPane.showMessageDialog(this, "No Agent name given.");
			return ;
		}
		
		if(agentNameExists(txtAgentName.getText(), null)) {
			JOptionPane.showMessageDialog(this, "Agent with name '" + txtAgentName.getText() + "' already exist." );			
			return ;
		}
		
		agentListModel.addElement(new APEntry(txtAgentName.getText(), txtAgentPassword.getText()));
	}

	/**
	 * Helper method: proofs if the given agent name exists in the agent list.
	 * @param name		the name of the agent.
	 * @param exclude	A reference to a list entry which should be ignored by the test.
	 * @return true if the given name is found in the list, otherwise false.
	 */
	private boolean agentNameExists(String name, APEntry exclude) {
		for(int i=0; i<agentListModel.getSize(); ++i) {
			APEntry entry = (APEntry)agentListModel.get(i);
			if(entry != exclude && entry.getName().equals(name)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Helper method: Callback called if the 'Start Simulation' Button is clicked.
	 * @throws ParserConfigurationException 
	 */
	private void onStartSimulationClicked() throws ParserConfigurationException {
		Object obj = lstServer.getSelectedValue();
		if(obj != null) {
			APCEntry entry = (APCEntry)obj;
			APCConfigureConnection apcCC = new APCConfigureConnection(
					entry.getIp(), entry.getPort(), createConfigurationObject());
			apcCC.addHandler(this);
			apcCC.startConfiguration();
		}
	}
	
	/**
	 * Helper method: Start the connection to the yellowPages server.
	 * @see YellowPagesConnector for more details
	 */
	private void startConnection() {
		String ip = txtYellowPagesIp.getText();
		connector = new YellowPagesConnector(ip, (short) 20200, this);	
	}

	private JTextField txtYellowPagesIp;
	
	private JList lstServer;
	public JList getServerList() {
		return lstServer;
	}

	private DefaultListModel serverListModel;
	public DefaultListModel getServerListModel() {
		return serverListModel;
	}
	
	private JFileChooser configurationFileChooser;
	
	private JButton btnYpConnect;
	
	private JTextField txtAgentName;
	
	private JTextField txtAgentPassword;
	
	private JButton btnAddAgent;
	
	private JButton btnEditAgent;
	
	private JList lstAgents;
	
	private DefaultListModel agentListModel;
	public DefaultListModel getAgentListModel() {
		return agentListModel;
	}
	
	private JTextField txtAgentClass;
	
	private JTextField txtRelayClass;
	
	private JTextField txtSchedulerClass;
	
	private JTextField txtEnvironemntUrl;
	
	private JTextField txtGroupName;
	
	private JButton btnSaveConfiguration;
	
	private JButton btnLoadConfiguration;
	
	private JButton btnStartSimulation;
	
	/**
	 * kill warning
	 */
	private static final long serialVersionUID = -8837440864318683067L;

	private YellowPagesConnector connector;
}
