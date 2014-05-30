package eis;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggerHandler {
	public static void setLoggerVars() {
		Logger.getLogger(SimulationState.class.getCanonicalName()).setLevel(Level.WARNING);;
		Logger.getLogger(Agent.class.getCanonicalName()).setLevel(Level.WARNING);
		//Logger.getLogger("AgentHandler").setLevel(Level.WARNING);
	}

}
