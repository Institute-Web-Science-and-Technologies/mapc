package net.sf.beenuts.apc.util;

import java.io.File;
import java.util.HashMap;

import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * this class stores local machine settings used to
 * setup an agentProcessCluster. Implemented as a singleton.
 * 
 * @author Thomas Vengels
 *
 */
public class LocalSettings {

	private LocalSettings(String file) {
		data = new HashMap<String,String>();
		
		// test if file exists
		try {
			File f = new File("local_settings.xml");
			if (!f.exists()) {				
				JOptionPane.showMessageDialog(null, "Warning! local_settings.xml not found, please create one","AgentProcessCluster Local Settings", JOptionPane.OK_OPTION);				
			} else {
				;//JOptionPane.showMessageDialog(null, "local_settings.xml found!","AgentProcessCluster Local Settings", JOptionPane.OK_OPTION);
			}
		} catch (Exception e) {
			
		}
		
		try {
			
			DocumentBuilder docbld = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = docbld.parse(new File(file));			
			NodeList nl = doc.getElementsByTagName("local_settings");
			for (int i = 0; i < nl.getLength(); i++) {
				
				Element config = (Element) nl.item(i);
				
				NodeList entries = config.getElementsByTagName("config");
				
				for (int j = 0; j< entries.getLength(); j++) {
					
					Element entry = (Element) entries.item(j);
					String key = entry.getAttribute("key");
					String value = entry.getAttribute("value");						
					data.put(key,value);
				}
			}
								
		} catch (Exception e){
			System.err.println("local setting error!");
			e.printStackTrace();
		}
	}
		
	private HashMap<String,String>	data;
		
	
	private static LocalSettings instance = null; 
	
	public static synchronized LocalSettings getSettings() {
		if (instance == null)
			instance = new LocalSettings("local_settings.xml");
		return instance;
	}
	
	
	public String getValue(String key) {
		return data.get(key);
	}
}
