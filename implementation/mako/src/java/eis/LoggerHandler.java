package eis;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggerHandler {
	public static void setLoggerVars() {
		Logger.getLogger("SimulationState").setLevel(Level.WARNING);;
		Logger.getLogger("Agent").setLevel(Level.WARNING);
	}

}
