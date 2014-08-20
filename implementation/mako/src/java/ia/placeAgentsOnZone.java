// Internal action code for project mako

package ia;

import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;

// input: centerZoneVertex, list of agents
// output: list of agents positions
public class placeAgentsOnZone extends DefaultInternalAction {

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args)
            throws Exception {
        // execute the internal action
        ts.getAg().getLogger().info("executing internal action 'ia.placeAgentsOnZone'");
        if (true) { // just to show how to throw another kind of exception
            throw new JasonException("not implemented!");
        }

        // everything ok, so returns true
        return true;
    }
}
