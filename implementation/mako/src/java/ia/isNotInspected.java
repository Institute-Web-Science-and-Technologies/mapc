// Internal action code for project mako

package ia;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;
import eis.MapAgent;

// Call from AgentSpeak: isNotInspected(Vehicle)
public class isNotInspected extends DefaultInternalAction {
    private static final long serialVersionUID = 9032663067998146025L;

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args)
            throws Exception {
        String agentName = args[0].toString();
        boolean isInspected = MapAgent.getInstance().getAgent(agentName).isInspected();
        if (isInspected) {
            return false;
        } else {
            return true;
        }
    }
}
