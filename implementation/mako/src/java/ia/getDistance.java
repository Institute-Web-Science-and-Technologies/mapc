// Internal action code for project mako

package ia;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;
import eis.JasonHelper;
import eis.MapAgent;
import eis.Vertex;

// input: position, destination
// output: totalHops

// Call from AgentSpeak: getDistance(Position, Destination, TotalHops)
public class getDistance extends DefaultInternalAction {
    private static final long serialVersionUID = 3595982059266499907L;

    @Override
    public Object execute(TransitionSystem ts, Unifier unifier, Term[] terms)
            throws Exception {
        Vertex position = MapAgent.getInstance().getVertex(terms[0].toString());
        Vertex destination = MapAgent.getInstance().getVertex(terms[1].toString());
        Term totalHopsTerm = terms[2];

        int totalHops = MapAgent.getInstance().getHopsToVertex(position, destination);

        return unifier.unifies(totalHopsTerm, JasonHelper.getTerm(totalHops));
    }
}
