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

/** @author Michael Ruster */
public class addVertex extends DefaultInternalAction {

    private static final long serialVersionUID = 8944884334402619450L;

    /**
     * This method will add (or update) a vertex to/on the {@code Graph}. If the
     * agent also perceived the team this vertex belonged to, it gets added to
     * the vertex.
     * <p>
     * If else a value is perceived, then it is added to the vertex associated
     * with it.
     */
    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args)
            throws Exception {

        if (args[0].isLiteral()) {
            String vertexIDString = ((Literal) args[0]).getFunctor();
            assert (vertexIDString.length() < 5); // v123

            Graph graph = Graph.getInstance();
            Identifier vertexID = new Identifier(vertexIDString);
            if (args[1].isLiteral()) { // got a team value:
                String teamIDString = ((Literal) args[1]).getFunctor();
                assert (teamIDString.equals("A") || teamIDString.equals("B") || teamIDString.equals("none"));

                graph.addVertex(vertexID, new Identifier(teamIDString));
                return true;
            }

            if (args[1].isNumeric()) { // got a Vertex value:
                double value = ((NumberTerm) args[1]).solve();

                graph.updateVertexValue(vertexID, new Numeral(value));
            } else {// no team
                graph.addVertex(vertexID);
                return true;
            }
        }
        return false;
    }
}
