// Internal action code for project mako

package ia;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;

import java.util.logging.Logger;

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
        Logger logger = ts.getAg().getLogger();
        Vertex position = MapAgent.getInstance().getVertex(terms[0].toString());
        // logger.info("getDistance debug: position = " + position);
        Vertex destination = MapAgent.getInstance().getVertex(terms[1].toString());
        // logger.info("getDistance debug: destination = " + destination);
        Term totalHopsTerm = terms[2];

        Integer totalHops = MapAgent.getInstance().getHopsToVertex(position, destination);
        // logger.info("getDistance debug: totalHops = " + totalHops);
        if (totalHops == null) {
            totalHops = -1;
        }
        return unifier.unifies(totalHopsTerm, JasonHelper.getTerm(totalHops));
    }
}
