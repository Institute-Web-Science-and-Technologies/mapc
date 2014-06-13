// Internal action code for project mako

package internalActions;

import eis.iilang.Identifier;
import eis.iilang.Numeral;
import graph.Graph;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.Term;

/**
 * 
 * Syntax isVertexProbed(Vertex, Value). returns boolean If vertex probed -
 * updates Value variable.
 * 
 * @author Sergey Dedukh
 * 
 */
public class isVertexProbed extends DefaultInternalAction {

    private static final long serialVersionUID = 5022106404316206669L;

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args)
            throws Exception {
        // execute the internal action
        if (args.length == 2) {
            if (args[0].isLiteral()) {

                String vertexIDString = ((Literal) args[0]).getFunctor();
                assert (vertexIDString.length() < 5); // v123

                Graph graph = Graph.getInstance();

                Identifier vertexID = new Identifier(vertexIDString);
                boolean isProbed = graph.isVertexProbed(vertexID);
                if (isProbed) {
                    Numeral vertexValue = graph.getVertexValue(vertexID);
                    un.unifiesNoUndo(args[1], new NumberTermImpl(vertexValue.getValue().intValue()));
                }
                return isProbed;
            }
        }
        return false;
    }
}