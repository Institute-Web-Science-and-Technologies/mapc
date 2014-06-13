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

import java.util.HashMap;
import java.util.Random;

/**
 * Syntax getBestUnexploredVertex(InitialVertex, NextVertex). Returns are there
 * unsurveyed edges as a result and updates the NextVertex so that it
 * corresponds to unvisited vertex with lowest edge weight. If there are no
 * unvisited vertices in the neighborhood sets NextVertex = InitialVertex. If
 * there are no neighboring vertices corresponding to the above criterion and
 * there are usurveyed edges, sets NextVertex to a random vertex from the
 * neighborhood.
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

            HashMap<Identifier, Integer> vertexNeighborhood = graph.getNeighborhood(vertexID, 1);

            Identifier nextVertexID;
            boolean unsurveyedEdges = false;

            if ((vertexNeighborhood == null) || (vertexNeighborhood.size() <= 1)) {
                nextVertexID = vertexID;
            } else {
                vertexNeighborhood.remove(vertexID);
                nextVertexID = vertexID;
                int minWeght = Integer.MAX_VALUE;

                for (Identifier vertexNeighID : vertexNeighborhood.keySet()) {

                    if (graph.isEdgeSurveyed(vertexID, vertexNeighID)) {
                        if (graph.isVertexVisited(vertexNeighID))
                            continue;

                        int weight = graph.getEdgeWeight(vertexID, vertexNeighID).getValue().intValue();
                        if (weight < minWeght) {
                            minWeght = weight;
                            nextVertexID = vertexNeighID;
                        }
                    } else {
                        unsurveyedEdges = true;
                    }
                    // if nothing found but there are unsurveyed edges - return
                    // random edge
                    if (unsurveyedEdges && (minWeght == Integer.MAX_VALUE)) {
                        Random rnd = new Random();
                        int rndVal = rnd.nextInt(vertexNeighborhood.size());
                        for (int i = 0; i < rndVal; i++)
                            nextVertexID = vertexNeighborhood.keySet().iterator().next();
                    }
                }
            }

            Term vertexTerm = new Atom(nextVertexID.toString());
            un.unifiesNoUndo(args[1], vertexTerm);
            return unsurveyedEdges;
        }
        return false;
    }
}
