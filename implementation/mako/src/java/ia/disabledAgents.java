// Internal action code for project mako

package ia;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;
import eis.MapAgent;

// Call from AgentSpeak: disabledAgents()
public class disabledAgents extends DefaultInternalAction {

    private static final long serialVersionUID = -5173347495114004878L;

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args)
            throws Exception {
        return MapAgent.getInstance().disabledAgents();
    }
}
