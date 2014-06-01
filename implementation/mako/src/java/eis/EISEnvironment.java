package eis;

import java.io.IOException;

import jason.asSyntax.Structure;
import jason.environment.Environment;
import c4jason.CartagoEnvironment;
import eis.exceptions.ActException;
import eis.exceptions.ManagementException;
import eis.iilang.Action;

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
	
	@Override
	  public boolean executeAction(String agName, Structure action) {
	    if (action.getFunctor().equals("recharge")) {
	      System.out.println(agName+": i wanna recharge");
	      Action recharge = new Action("recharge");
	      try {
			ei.performAction(agName, recharge);
		} catch (ActException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	      return true;
	    } else {
	      //logger.info("executing: "+action+", but not implemented!");
	      return false;
	    }
	  }
}
