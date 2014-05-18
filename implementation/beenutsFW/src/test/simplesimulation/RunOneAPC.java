package test.simplesimulation;

import java.util.Map;

import net.sf.beenuts.apc.AgentProcessCluster;
import net.sf.beenuts.apc.BeenutsAgentProcessCluster;
import net.sf.beenuts.apc.util.APCConfiguration;

/**
 * test application for the mt scheduler. preliminary verssion.
 * 
 * @author Thomas Vengels
 *
 */
public class RunOneAPC {

	public static void main(String args[]) {
		
		// configuration ist test_manyapc.xml
		Map<String,APCConfiguration> cfg = APCConfiguration.loadConfig("singleapc_config_1.xml");
		
		AgentProcessCluster m;
		// create master
		m = new BeenutsAgentProcessCluster();
		m.initialize(cfg.get("cfg1"), "apc1");
		m.waitReady();
		
		SimpleSimulation ssim = (SimpleSimulation) m.getAPR();
		for (int i = 0; i < 10; i++) {
			ssim.nextStep();
			try {
				Thread.sleep(2000);
			} catch (InterruptedException intEx) {
				
			}
		}
		m.shutdown();
	}
}
