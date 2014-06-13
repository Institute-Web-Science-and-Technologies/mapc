// Internal action code for project mako

package internalActions;

import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;

public class getBestUnexploredVertex extends DefaultInternalAction {

    /**
     * 
     */
    private static final long serialVersionUID = 7114494255361793246L;

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args)
            throws Exception {
        // execute the internal action
        ts.getAg().getLogger().info("executing internal action 'internalActions.getBestUnexploredVertex'");
        if (true) { // just to show how to throw another kind of exception
            throw new JasonException("not implemented!");
        }
        Term agentPosition = args[0];
        Term Vertex = args[1];

        // everything ok, so returns true
        return true;
    }
}
