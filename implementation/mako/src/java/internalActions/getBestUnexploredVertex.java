// Internal action code for project mako

package internalActions;

import eis.iilang.Identifier;
import graph.Graph;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Atom;
import jason.asSyntax.Literal;
import jason.asSyntax.Term;

/**
 * Syntax getBestUnexploredVertex(InitialVertex, NextVertex). Returns true if
 * input values are correct. Updates the NextVertex so that it corresponds to
 * unvisited vertex with lowest edge weight. If there are no unvisited vertices
 * in the neighborhood sets NextVertex = InitialVertex.
 * 
 * @author Sergey Dedukh
 * 
 */
public class getBestUnexploredVertex extends DefaultInternalAction {

    private static final long serialVersionUID = 7114494255361793246L;

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args)
            throws Exception {
        // execute the internal action
        if ((args.length == 2) && (args[0].isLiteral())) {

            String vertexIDString = ((Literal) args[0]).getFunctor();
            assert (vertexIDString.length() < 5); // v123

            Graph graph = Graph.getInstance();

            Identifier vertexID = new Identifier(vertexIDString);

            Identifier nextVertexID = graph.getBestUnexploredVertex(vertexID);

            Term vertexTerm = new Atom(nextVertexID.toString());

            return un.unifiesNoUndo(args[1], vertexTerm);
        }
        return false;
    }
}
