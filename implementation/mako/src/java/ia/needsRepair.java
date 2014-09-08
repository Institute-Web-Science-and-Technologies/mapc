// Internal action code for project mako

package ia;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;
import eis.Agent;
import eis.MapAgent;

// Call from AgentSpeak: needsRepair(VehicleName)
public class needsRepair extends DefaultInternalAction {

    private static final long serialVersionUID = -2690829529868242115L;

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args)
            throws Exception {
        Agent agent = MapAgent.getInstance().getAgent(args[0].toString());
        if (agent != null) {
            return agent.needsRepair();
        }
        return false;
    }
}
