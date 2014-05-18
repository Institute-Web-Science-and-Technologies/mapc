package net.sf.beenuts.apscc;

import java.io.IOException;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Encapsulates the configuration of the APSCC for new simulations.
 * @author Tim Janus
 */
public class APSCCConfig implements Serializable {

	/**
	 * kill warning
	 */
	private static final long serialVersionUID = 4270375710233411879L;

	public APSCCConfig() {
	}
	
	/**
	 * Ctor: Creates a APSCCConfig from the given filename
	 * @param filename
	 */
	public APSCCConfig(String filename) {
		this(APSCCConfig.fromFile(filename));
	}
	
	/**
	 * Ctor: Creates a APSCCConfig from the given dom-document.
	 * @param domDoc
	 */
	public APSCCConfig(Document domDoc) {
		this(APSCCConfig.fromDocument(domDoc));
	}
	
	/**
	 * Ctor: Copy-Ctor
	 * @param other
	 */
	public APSCCConfig(APSCCConfig other) {
		environment = other.environment;
		agentClass = other.agentClass;
		relayClass = other.relayClass;
		schedulerClass = other.schedulerClass;
		group = other.group;
		agents.addAll(other.agents);
	}
	
	/**
	 * Converts the APSCCConfig into a dom-document
	 * @return
	 * @throws ParserConfigurationException
	 */
	public Document toDom() throws ParserConfigurationException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware( true );
		// factory.setValidating( true );
		DocumentBuilder builder  = null;
		Document        document = null;
		
		builder  = factory.newDocumentBuilder();
		document = builder.newDocument();
		Element root = document.createElementNS(null, "apsccconfig");
		
		Element env = (Element)root.appendChild(document.createElementNS(null, "environment"));
		env.setAttribute("url", environment);
		
		Element agent = (Element)root.appendChild(document.createElementNS(null, "agentclass"));
		agent.setAttribute("name", agentClass);
		
		Element relayclass = (Element)root.appendChild(document.createElementNS(null, "relayclass"));
		relayclass.setAttribute("name", relayClass);
		
		Element schedulerslass = (Element)root.appendChild(document.createElementNS(null, "schedulerclass"));
		schedulerslass.setAttribute("name", schedulerClass);
		
		Element g = (Element)root.appendChild(document.createElementNS(null, "group"));
		g.setAttribute("name", group);
		
		Element el_agents = (Element)root.appendChild(document.createElementNS(null, "agents"));
		for(int i=0; i<agents.size(); ++i) {
			APEntry entry = agents.get(i);
			Element actual = (document.createElementNS(null, "agent"));
			actual.setAttribute("name", entry.getName());
			actual.setAttribute("password", entry.getPassword());
			el_agents.appendChild(actual);      	
			el_agents.appendChild(actual);
		}
		document.appendChild(root);
		return document;
	}
	
	public static APSCCConfig fromFile(String filename) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder  = null;
    	Document        document = null;
        
    	try {
    		builder  = factory.newDocumentBuilder();
        	document = builder.parse( filename );
        } catch (ParserConfigurationException ex) {
        	lastCreationError = ex.getMessage();
        	return null;
        } catch (SAXException ex) {
        	lastCreationError = ex.getMessage();
        	return null;
        } catch (IOException ex) {
        	lastCreationError = ex.getMessage();
        	return null;
        } 

        return fromDocument(document);
	}

	public static APSCCConfig fromDocument(Document document) {
		APSCCConfig reval = new APSCCConfig();
		Element root = document.getDocumentElement();
        
        for(int i=0; i<root.getChildNodes().getLength(); ++i) {
        	if(root.getChildNodes().item(i) instanceof Element) {
        		Element actual = (Element)root.getChildNodes().item(i);
        		String name = actual.getNodeName();
        		if(name.equalsIgnoreCase("environment")) {
        			reval.setEnvironment(actual.getAttribute("url"));
        		} else if(name.equalsIgnoreCase("agentclass")) {
    				reval.setAgentClass(actual.getAttribute("name"));
        		} else if(name.equalsIgnoreCase("relayclass")) {
        			reval.setRelayClass(actual.getAttribute("name"));
        		} else if(name.equalsIgnoreCase("schedulerclass")) {
        			reval.setSchedulerClass(actual.getAttribute("name"));
        		} else if(name.equalsIgnoreCase("group")) {
        			reval.setGroup(actual.getAttribute("name"));
        		} else if(name.equalsIgnoreCase("agents")) {
    				for(int k=0; k<actual.getChildNodes().getLength(); ++k) {
    					Element actualAgent = (Element)actual.getChildNodes().item(k);
    					APEntry newEntry = new APEntry(actualAgent.getAttribute("name"), actualAgent.getAttribute("password"));
    					reval.getAgents().add(newEntry);
    				}
    			} else {
    				lastCreationError = "Element: '" + name + "' not valid.";
    				return null;
    			}
    		}
        }
        
        return reval;
	}
	
	public static String getLastError() {
		return lastCreationError.equals("") ? null : lastCreationError;
	}
	
	public String getEnvironment() {
		return environment;
	}

	public void setEnvironment(String environment) {
		this.environment = environment;
	}

	public String getAgentClass() {
		return agentClass;
	}

	public void setAgentClass(String agentClass) {
		this.agentClass = agentClass;
	}

	public String getRelayClass() {
		return relayClass;
	}

	public void setRelayClass(String relayClass) {
		this.relayClass = relayClass;
	}

	public String getSchedulerClass() {
		return schedulerClass;
	}

	public void setSchedulerClass(String schedulerClass) {
		this.schedulerClass = schedulerClass;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}
	
	public List<APEntry> getAgents() {
		return agents;
	}
	
	private String environment;

	private String agentClass;
	
	private String relayClass;
	
	private String schedulerClass;
	
	private String group;
	
	private List<APEntry> agents = new LinkedList<APEntry>();
	
	private static String lastCreationError = "";
}
