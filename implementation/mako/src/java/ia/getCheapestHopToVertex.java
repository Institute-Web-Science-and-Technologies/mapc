package ia;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;
import eis.JasonHelper;
import eis.MapAgent;
import eis.Vertex;

/**
 * Call from AgentSpeak: getCheapestHopToVertex(Position, Destination, *NextHop)
 * <p>
 * Given an agent's current Position, unifies NextHop with
 * the next hop along the cheapest path to the given Destination.
 *
 */
public class getCheapestHopToVertex extends DefaultInternalAction {
    private static final long serialVersionUID = 2412754008233388900L;

    @Override
    public Object execute(TransitionSystem ts, Unifier unifier, Term[] terms)
            throws Exception {
        Vertex position = MapAgent.getInstance().getVertex(terms[0].toString());
        Vertex destination = MapAgent.getInstance().getVertex(terms[1].toString());
        Term nextHopTerm = terms[2];

        Vertex nextHop = MapAgent.getInstance().getCheapestHopToVertex(position, destination);

        if (nextHop == null) {
            return false;
        } else {
            return unifier.unifies(nextHopTerm, JasonHelper.getTerm(nextHop.getIdentifier()));
        }
    }
}
