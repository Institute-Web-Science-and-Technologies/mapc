// Internal action code for project mako

package internalActions;

import eis.iilang.Identifier;
import graph.Graph;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.asSyntax.Term;

public class isVertexUnsurveyed extends DefaultInternalAction {

    /**
     * 
     */
    private static final long serialVersionUID = -4051367142149430976L;

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args)
            throws Exception {
        // execute the internal action
        if ((args.length == 1) && (args[0].isLiteral())) {
            String vertexIDString = ((Literal) args[0]).getFunctor();
            assert (vertexIDString.length() < 5); // v123

            Graph graph = Graph.getInstance();

            return !graph.isVertexSurveyed(new Identifier(vertexIDString));
        }
        return false;
    }
}
