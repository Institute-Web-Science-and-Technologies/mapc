package eis;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import jason.environment.Environment;
import c4jason.CartagoEnvironment;
import eis.exceptions.ManagementException;

public class EISEnvironment extends Environment {

	private EnvironmentInterfaceStandard ei;
	private CartagoEnvironment cartagoEnvironment;
	private AgentHandler agentHandler;

	/*
	 * jason lifecycle: init -> user-init -> compile -> run -> user-end
	 */
	@Override
	public void init(String[] args) {
		// set logger vars
		LoggerHandler.setLoggerVars();
		
		// init EISMASSIM environment
		try {
			ei = EILoader.fromClassName("massim.eismassim.EnvironmentInterface");
		} catch (IOException e) {
			e.printStackTrace();
		}
		agentHandler = new AgentHandler(ei);
		agentHandler.initAgents(args);
		try {
			ei.start();
		} catch (ManagementException e) {
			e.printStackTrace();
		}
	}

	// Strange thing with args??? Why init with new empty String?
	public void startCartago(String[] args) {
		cartagoEnvironment = new CartagoEnvironment();
		cartagoEnvironment.init(new String[0]);
	}

	@Override
	public void stop() {
		if (cartagoEnvironment != null) {
			cartagoEnvironment.stop();
		}
		if (ei != null) {
			try {
				if (ei.isKillSupported())
					ei.kill();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
