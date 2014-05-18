package test.simplesimulation;

import java.util.Map;

import net.sf.beenuts.apc.AgentProcessCluster;
import net.sf.beenuts.apc.BeenutsAgentProcessCluster;
import net.sf.beenuts.apc.util.APCConfiguration;

public class RunManyAPC {

	public static void main(String args[]) {
		
		// configuration ist test_manyapc.xml
		Map<String,APCConfiguration> cfg = APCConfiguration.loadConfig("manyapc_config_1.xml");
		
		AgentProcessCluster m, s1, s2;
		// create master
		m = new BeenutsAgentProcessCluster();
		m.initialize(cfg.get("master"), "apc1");
		m.waitReady();
		// slave 1
		s1 = new BeenutsAgentProcessCluster();
		s1.initialize(cfg.get("slave1"), "apc2");
		s1.waitReady();
		// slave2
		s2 = new BeenutsAgentProcessCluster();
		s2.initialize(cfg.get("slave2"), "apc3");
		s2.waitReady();
		
		SimpleSimulation ssim = (SimpleSimulation) m.getAPR();
		for (int i = 0; i < 10; i++) {
			ssim.nextStep();
			try {
				Thread.sleep(2000);
			} catch (InterruptedException intEx) {
				
			}
		}
		s2.shutdown();
		s1.shutdown();
		m.shutdown();
	}
}
