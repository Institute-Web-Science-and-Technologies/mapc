// Internal action code for project mako

package ia;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;
import eis.MapAgent;

// Call from AgentSpeak: registerForZoning(Agent)
public class registerForZoning extends DefaultInternalAction {

    private static final long serialVersionUID = -6580903245557318560L;

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args)
            throws Exception {
        String agent = args[0].toString();
        MapAgent.getInstance().registerForZoning(agent);
        return true;
    }
}
