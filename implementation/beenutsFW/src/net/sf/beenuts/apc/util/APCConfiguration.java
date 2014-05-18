package net.sf.beenuts.apc.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.*;

import net.sf.beenuts.util.*;

public class APCConfiguration implements Serializable{
	
	/**
	 * kill warning
	 */
	private static final long serialVersionUID = -7027514496398403273L;

	// agentProcessCluster configuration
	public String apcName;
	
	// the first version of the config (created from an apscc)
	public boolean first_config = true;
	public String group;
	
	
	// yellow pages configuration
	public boolean yp_enable = true;
	public String yp_host = "beenuts.cs.uni-dortmund.de";
	public int yp_port = 20200;
	
	// server configuration
	public boolean server_enable = true;
	public int server_port = 20200;
	
	// apr configuration
	public String apr_class = "net.sf.beenuts.apr.BeenutsAPR";
	public String apr_lsr_name = null;
	
	// agent configuration
	public Map<String,String> agentHosts = new HashMap<String,String>();
	public Map<String,String> agentClass = new HashMap<String,String>();
	
	// connect to targets, pair of (agentProcessCluster-id, (host,port))
	public Map<String, Pair<String, Integer>> connect2APC = new HashMap<String, Pair<String,Integer>>();
	
	// scheduler class and setting
	public String schedClass = "net.sf.beenuts.apc.StrrScheduler";
	public int schedCpuTime = 100;
	public int schedMaxConcurrency = 3;
	
	public APCConfiguration() {
		
	}
	
	public APCConfiguration(APCConfiguration toCopy) {
		apcName = toCopy.apcName;
		group = toCopy.group;
		
		yp_enable = toCopy.yp_enable;
		yp_host = toCopy.yp_host;
		yp_port = toCopy.yp_port;
		
		server_enable = toCopy.server_enable;
		server_port = toCopy.server_port;
		
		apr_class = toCopy.apr_class;
		apr_lsr_name = toCopy.apr_lsr_name;
		agentHosts.putAll(toCopy.agentHosts);
		connect2APC.putAll(toCopy.connect2APC);
		agentClass.putAll(toCopy.agentClass);
		
		schedClass = toCopy.schedClass;
		schedCpuTime = toCopy.schedCpuTime;
		schedMaxConcurrency = toCopy.schedMaxConcurrency;
	}
	
	public Document toDocument() {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware( true );
		// factory.setValidating( true );
		DocumentBuilder builder  = null;
		Document        document = null;
		
		try {
			builder  = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		document = builder.newDocument();
		
		Element root = document.createElementNS(null, "apcconfig");
		root.setAttribute("id", "1");
		
		Element apcserver = document.createElement("apcserver");
		apcserver.setAttribute("enable", String.valueOf(server_enable));
		apcserver.setAttribute("port", String.valueOf(server_port));
		root.appendChild(apcserver);
		
		Element apr = document.createElement("apr");
		apr.setAttribute("class", apr_class);
		apr.setAttribute("lsr", apr_lsr_name);
		root.appendChild(apr);
		
		Element scheduler = document.createElement("scheduler");
		scheduler.setAttribute("class", schedClass);
		scheduler.setAttribute("cpuTime", String.valueOf(schedCpuTime));
		scheduler.setAttribute("maxConcurrency", String.valueOf(schedMaxConcurrency));
		root.appendChild(scheduler);
		
		Element agents = document.createElement("agents");
		for(java.util.Map.Entry<String, String> entry : agentHosts.entrySet()) {
			Element ag = document.createElement("agent");
			ag.setAttribute("name", entry.getKey());
			ag.setAttribute("agentProcessCluster", entry.getValue());
			agents.appendChild(ag);
		}
		root.appendChild(agents);
		
		for(java.util.Map.Entry<String, Pair<String, Integer> > entry : connect2APC.entrySet()) {
			Element connect_to = document.createElement("connect_to");
			connect_to.setAttribute("agentProcessCluster", entry.getKey());
			connect_to.setAttribute("host", entry.getValue().getKey());
			connect_to.setAttribute("port", entry.getValue().getValue().toString());
			root.appendChild(connect_to);
		}
		
		document.appendChild(root);
		return document;
	}
	
	public static HashMap<String, APCConfiguration> loadConfig(String xmlFile) {
		HashMap<String, APCConfiguration> ret = new HashMap<String, APCConfiguration>();
		
		try {
			
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			
			Document doc = db.parse(xmlFile);
			
			NodeList nl = doc.getElementsByTagName("apcconfig");
			for (int i = 0; i < nl.getLength(); i++) {
				Element el = (Element) nl.item(i);
				if (el.hasAttribute("id")) {
					String configId = el.getAttribute("id").trim();
					APCConfiguration cfg = loadConfigFromElement(el);
					ret.put(configId,cfg);
				}
			}
			
		} catch (Exception e) {
			System.err.println("apcconfig error!");
			e.printStackTrace();
		}
		
		return ret;
	}
	
	public static APCConfiguration loadConfigFromElement(Element el) {
		APCConfiguration ret = new APCConfiguration();
		
		// agentProcessCluster server config, only first entry effects configuration 
		NodeList nl = el.getElementsByTagName("apcserver");
		if (nl.getLength() > 0) {
			Element serverConfig = (Element) nl.item(0);
			if ("true".equalsIgnoreCase(serverConfig.getAttribute("enable"))) {
				ret.server_enable = true;
				if (serverConfig.hasAttribute("port")) {
					ret.server_port = Integer.parseInt(  serverConfig.getAttribute("port") );
				}								
			} else {
				ret.server_enable = false;
			}
		}
		
		// scheduler config, only first entry counts
		nl = el.getElementsByTagName("scheduler");
		if (nl.getLength() > 0) {
			Element schedConfig = (Element) nl.item(0);
			ret.schedClass = schedConfig.getAttribute("class");
			if (schedConfig.hasAttribute("cpuTime"))
				ret.schedCpuTime = Integer.parseInt(schedConfig.getAttribute("cpuTime"));
			if (schedConfig.hasAttribute("maxConcurrency"))
				ret.schedMaxConcurrency = Integer.parseInt(schedConfig.getAttribute("maxConcurrency"));
		}
		
		// apr config, only first entry effects configuration
		// agentProcessCluster server config, only first entry effects configuration 
		nl = el.getElementsByTagName("apr");
		if (nl.getLength() > 0) {
			Element aprConfig = (Element) nl.item(0);
			if (aprConfig.hasAttribute("class"))
				ret.apr_class = aprConfig.getAttribute("class");
			if (aprConfig.hasAttribute("lsr"))
				ret.apr_lsr_name = aprConfig.getAttribute("lsr");
		}
		
		// agent config. only regard hosts for now
		nl = el.getElementsByTagName("agents");
		for (int i = 0; i < nl.getLength(); i++) {
			// find all agent entrys inside agents
			Element el2 = (Element) nl.item(i);			
			NodeList nl2 = el2.getElementsByTagName("agent");
			for (int j = 0; j < nl2.getLength(); j++) {
				Element agElem = (Element) nl2.item(j);
				
				// ag must have name and host				
				if (agElem.hasAttribute("name") && agElem.hasAttribute("agentProcessCluster")) {
					
					String agName = agElem.getAttribute("name").trim();
					String agAPC = agElem.getAttribute("agentProcessCluster").trim();
					
					// store config
					ret.agentHosts.put(agName, agAPC);
				}
				
				// ag must have name & class
				if (agElem.hasAttribute("name") && agElem.hasAttribute("class")) {
					String agName = agElem.getAttribute("name").trim();
					String agClass = agElem.getAttribute("class").trim();
					ret.agentClass.put(agName, agClass); 
				} 
			}
		}
		
		// connect_to config. used to configure an agentProcessCluster to automatically
		// connect to another agentProcessCluster.
		nl = el.getElementsByTagName("connect_to");
		for (int i = 0; i < nl.getLength(); i++) {
			Element el2 = (Element) nl.item(i);
			if (el2.hasAttribute("agentProcessCluster") && el2.hasAttribute("host") && el2.hasAttribute("port")) {
				ret.connect2APC.put( el2.getAttribute("agentProcessCluster").trim(),
						new Pair<String,Integer>(
								el2.getAttribute("host"),
								Integer.parseInt( el2.getAttribute("port"))));
			}
		}
		
		return ret;
	}
}
