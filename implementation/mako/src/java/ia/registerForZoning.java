// Internal action code for project mako

package ia;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;
import eis.Agent;
import eis.MapAgent;

// Call from AgentSpeak: registerForZoning
public class registerForZoning extends DefaultInternalAction {

    private static final long serialVersionUID = -6580903245557318560L;

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args)
            throws Exception {
        String agentName = ts.getUserAgArch().getAgName();
        Agent agent = MapAgent.getInstance().getAgent(agentName);
        agent.setAvailableForZoning(true);
        return true;
    }
}
