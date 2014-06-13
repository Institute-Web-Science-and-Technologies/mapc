// Internal action code for project mako

package internalActions;

import jason.*;
import jason.asSemantics.*;
import jason.asSyntax.*;

public class isVertexSurveyed extends DefaultInternalAction {

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        // execute the internal action
        ts.getAg().getLogger().info("executing internal action 'internalActions.isVertexSurveyed'");
        if (true) { // just to show how to throw another kind of exception
            throw new JasonException("not implemented!");
        }
        
        // everything ok, so returns true
        return true;
    }
}
