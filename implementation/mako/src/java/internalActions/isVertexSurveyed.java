// Internal action code for project mako

package internalActions;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Atom;
import jason.asSyntax.Term;

public class isVertexSurveyed extends DefaultInternalAction {

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args)
            throws Exception {
        // execute the internal action
        ts.getAg().getLogger().info("executing internal action 'internalActions.isVertexSurveyed'");

        Term agentPosition = args[0];

        boolean isVertexSurveyed = false; // <-- TODO: implement this!

        Term vertexSurveyedState = new Atom(String.valueOf(isVertexSurveyed));
        return un.unifiesNoUndo(args[1], vertexSurveyedState);
    }
}