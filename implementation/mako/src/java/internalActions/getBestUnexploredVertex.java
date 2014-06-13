// Internal action code for project mako

package internalActions;

import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Atom;
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

        String vertexIdentifier = null; // =
                                        // Graph.getPosition(agentPosition.toString());
                                        // // fill this out please!

        if (vertexIdentifier == null) {
            return false;
        } else {
            Term vertexTerm = new Atom(vertexIdentifier);
            return un.unifiesNoUndo(args[1], vertexTerm);
        }
    }
}
