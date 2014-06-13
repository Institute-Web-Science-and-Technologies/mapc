// Internal action code for project mako

package internalActions;

import eis.iilang.Identifier;
import graph.Graph;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.asSyntax.Term;

/**
 * Syntax isVertexSurveyed(Vertex). Returns true if all edges are surveyed and
 * false otherwise.
 * 
 * @author Sergey Dedukh
 * 
 */
public class isVertexSurveyed extends DefaultInternalAction {

    private static final long serialVersionUID = 1088816365707527642L;

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args)
            throws Exception {
        // execute the internal action
        if ((args.length == 1) && (args[0].isLiteral())) {
            String vertexIDString = ((Literal) args[0]).getFunctor();
            assert (vertexIDString.length() < 5); // v123

            Graph graph = Graph.getInstance();

            return graph.isVertexSurveyed(new Identifier(vertexIDString));
        }
        return false;
    }
}
