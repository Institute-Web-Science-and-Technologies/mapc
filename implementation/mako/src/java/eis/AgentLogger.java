/*
 * @author Artur Daudrich
 */
package eis;

import java.util.logging.Logger;

public class AgentLogger {
	private Logger logger;
	private boolean visible = true;
	
	public AgentLogger(String source) {
		logger = Logger.getLogger(source);
	}
	
	public void info(String msg) {
		if (this.isVisible()){
			logger.info(msg);
		}
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean isVisible) {
		this.visible = isVisible;
	}
}
