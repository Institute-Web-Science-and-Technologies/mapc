package ia;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;
import eis.JasonHelper;
import eis.MapAgent;
import eis.Vertex;

/**
 * Call from AgentSpeak: getDistance(Position, Destination, *TotalHops)
 * <p>
 * Given the agent's Position and his Destination, unifies TotalHops with the
 * number of hops required to reach that destination. Unifies TotalHops with the
 * value -1 if the distance is unknown.
 */
public class getDistance extends DefaultInternalAction {
    private static final long serialVersionUID = 3595982059266499907L;

    @Override
    public Object execute(TransitionSystem ts, Unifier unifier, Term[] terms)
            throws Exception {
        Vertex position = MapAgent.getInstance().getVertex(terms[0].toString());
        Vertex destination = MapAgent.getInstance().getVertex(terms[1].toString());
        Term totalHopsTerm = terms[2];

        Integer totalHops = MapAgent.getInstance().getHopsToVertex(position, destination);
        if (totalHops == null) {
            totalHops = -1;
        }
        return unifier.unifies(totalHopsTerm, JasonHelper.getTerm(totalHops));
    }
}
