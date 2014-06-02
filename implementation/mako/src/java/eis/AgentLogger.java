/*
 * @author Artur Daudrich
 */
package eis;

import java.util.logging.Logger;

public class AgentLogger {
	private Logger logger;
	
	public AgentLogger(String source) {
		logger = Logger.getLogger(source);
	}
	
	public void info(String msg) {
		if (logger.getName().equalsIgnoreCase("eis.EISEnvironment")){
			logger.info(msg);
		}
	}
}
