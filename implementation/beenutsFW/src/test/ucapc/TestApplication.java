package test.ucapc;

/**
 * test class for an agentProcessCluster implementation
 * 
 * @author Thomas Vengels
 *
 */
public class TestApplication {
	
	
	public static void main(String args[]) {
		
		// general initialization of an agentProcessCluster:
		// 1: create apr and scheduler
		// 2: create local agents (apcagent and agentProcess)
		// 3: bind local agents to apr and scheduler
		// 4: start scheduler
		
		/*
		BeenutsAgentProcessCluster agentProcessCluster = new BeenutsAgentProcessCluster();
		agentProcessCluster.initialize(null,"agentProcessCluster");
		
		try {
			// wait until agentProcessCluster is ready
			while (!agentProcessCluster.testReady)
				Thread.sleep(100);
			
			// simulation can run in main thread
			agentProcessCluster.testSimulation();
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
		// shutdown agentProcessCluster
		agentProcessCluster.shutdown();
		System.exit(0);
		*/
		
		System.out.println("deprecated application, use LocalMaster or LocalSlave!");
	}
}
