package net.sf.beenuts.apc.daemon;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class ClusterDaemonConfig {
	public String yp_ip;
	
	public int yp_port;
	
	public int cs_port;
	
	public int maxagents;
	
	public String group;
	
	public static ClusterDaemonConfig loadFromFile(String filename) {
		ClusterDaemonConfig reval = null;
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();
			Document doc = db.parse(filename);
			
			reval = new ClusterDaemonConfig();
			Element yp = (Element) doc.getElementsByTagName("yellowpages").item(0);
			reval.yp_ip = yp.getAttribute("ip");
			reval.yp_port = Integer.parseInt(yp.getAttribute("port"));
			reval.maxagents = Integer.parseInt(yp.getAttribute("maxagents"));
			reval.group = yp.getAttribute("group");
			
			Element cs = (Element) doc.getElementsByTagName("configserver").item(0);
			reval.cs_port = Integer.parseInt(cs.getAttribute("port"));
			
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return reval;
	}
}
