// Internal action code for project mako

package ia;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;
import eis.Agent;
import eis.MapAgent;

// Call from AgentSpeak: isSaboteur(Vehicle)
/**
 * 
 * Returns true if the given vehicle is known to be a saboteur, and false
 * otherwise.
 * 
 * @author sewell
 */
public class isSaboteur extends DefaultInternalAction {

    private static final long serialVersionUID = 4850947179738263674L;

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args)
            throws Exception {
        String agentName = args[0].toString();
        Agent agent = MapAgent.getInstance().getEnemyInfo(agentName);
        if (agent.getRole() == "saboteur") {
            return true;
        } else {
            return false;
        }
    }
}
