package test.ucapc;

import eis.iilang.Action;
import eis.iilang.Identifier;
import net.sf.beenuts.ap.AgentArchitecture;

/**
 * a simple test agent, waiting for some perceptions.
 * if an instance is named alice, she will broadcast
 * some information.
 * 
 * @author Thomas
 *
 */
public class TestAgent extends AgentArchitecture {

	private String simStep = null;
	
	/* (non-Javadoc)
	 * @see net.sf.beenuts.ap.AgentProcess#cycle(java.lang.Object)
	 */
	@Override
	public boolean cycle(Object perception) {
		if (perception instanceof String) {
			String per = (String) perception;
			System.out.println(this.getName() + ": " + per);
			
			if (per.startsWith("percept: "))
				simStep = per.substring(9);
			
			// 30% chance to do a broadcast now
			if ((simStep != null) && ("alice".equalsIgnoreCase(this.getName()))) {
				this.send("broadcast: from " +this.getName() + " "+simStep, null);
			}
			
			if ("eve".equalsIgnoreCase(this.getName())) {
				Action act = new Action("shoot", new Identifier("alice")); 
				this.act( act );
			}
		}
		
		return true;
	}

	@Override
	public void shutdown() {		
	}

}
