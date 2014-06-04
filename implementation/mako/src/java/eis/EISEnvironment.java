package eis;

import jason.asSyntax.Structure;
import jason.environment.Environment;

import java.io.IOException;

import c4jason.CartagoEnvironment;
import eis.exceptions.ManagementException;

/**
 * @author Artur Daudrich
 * @author Michael Sewell
 */
public class EISEnvironment extends Environment {

    public static String NAME = EISEnvironment.class.getName();
    private EnvironmentInterfaceStandard ei;
    private CartagoEnvironment cartagoEnvironment;
    private AgentHandler agentHandler;

    private AgentLogger logger = new AgentLogger(EISEnvironment.NAME);

    /*
     * jason lifecycle: init -> user-init -> compile -> run -> user-end
     */
    @Override
    public void init(String[] args) {
        // logger
        logger.setVisible(false);

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
            Agent agent = agentHandler.getAgent(agName);
            boolean actionResult = agent.doAction(ActionHandler.recharge());
            if (actionResult)
                logger.info(agName + "I recharged");
            else
                logger.info(agName + "I cannot recharge");
        }
        return true;
    }
}
