/*
 * @author Artur Daudrich
 */
package eis;

import java.util.logging.Logger;

public class AgentLogger {

	private String source;
	private Logger logger;
	
	public AgentLogger(String source) {
		logger = Logger.getLogger(source);
		System.out.println(source);
	}

	public void info(String msg) {
		if (isActive()) {
			logger.info(msg);
		}
	}

	private boolean isActive() {
		return source.equalsIgnoreCase("");
	}

}
