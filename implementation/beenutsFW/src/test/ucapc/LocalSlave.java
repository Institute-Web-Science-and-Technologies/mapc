package test.ucapc;

import java.util.Map;

import net.sf.beenuts.apc.BeenutsAgentProcessCluster;
import net.sf.beenuts.apc.util.APCConfiguration;

public class LocalSlave {
	public static void main(String args[]) {
		
		Map<String,APCConfiguration> cfg = APCConfiguration.loadConfig("apc_config_1.xml");
		BeenutsAgentProcessCluster apc = new BeenutsAgentProcessCluster();
		apc.initialize(cfg.get("slave_cfg"),"apc2");
		
		try{
			System.out.println("press key to shutdown agentProcessCluster");
			System.in.read();
		} catch (Exception e) {
			e.printStackTrace();
		}
		apc.shutdown();		
	}
}
