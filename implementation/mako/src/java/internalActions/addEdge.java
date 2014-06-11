// Internal action code for project mako

package internalActions;

import eis.iilang.Identifier;
import eis.iilang.Numeral;
import graph.Graph;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Term;

/**
 * 
 * @author Sergey Dedukh
 * 
 */
public class addEdge extends DefaultInternalAction {

    private static final long serialVersionUID = 7640879183789045302L;

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args)
            throws Exception {
        // execute the internal action
        if (args.length == 2) {
            if (args[0].isLiteral() && args[1].isLiteral()) {
                String VertexAID = ((Literal) args[0]).getFunctor();

                String VertexBID = ((Literal) args[0]).getFunctor();

                Graph graph = Graph.getInstance();
                graph.addEdge(new Identifier(VertexAID), new Identifier(VertexBID));

                return true;
            }

        } else if (args.length == 3) {
            if (args[0].isLiteral() && args[1].isLiteral() && args[2].isNumeric()) {
                String VertexAID = ((Literal) args[0]).getFunctor();

                String VertexBID = ((Literal) args[0]).getFunctor();

                double edgeWeight = ((NumberTerm) args[2]).solve();

                Graph graph = Graph.getInstance();
                graph.addEdge(new Identifier(VertexAID), new Identifier(VertexBID), new Numeral(edgeWeight));

                return true;
            }
        }
        return false;
    }
}
