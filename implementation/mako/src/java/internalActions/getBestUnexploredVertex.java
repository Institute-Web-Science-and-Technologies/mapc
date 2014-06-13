// Internal action code for project mako

package internalActions;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Atom;
import jason.asSyntax.Term;
import eis.iilang.Identifier;

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
        Term agentPosition = args[0];

        Identifier vertexIdentifier = null; // =
        // Graph.getPosition(agentPosition.toString());
        // // fill this out please!

        Term vertexTerm = new Atom(vertexIdentifier.toString());
        return un.unifiesNoUndo(args[1], vertexTerm);
    }
}
