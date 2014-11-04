package ia;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;
import eis.MapAgent;
import eis.Vertex;

/**
 * Call from AgentSpeak: isNotProbed(Vertex)
 * <p>
 * Given a Vertex, returns true if that vertex has not been probed before.
 */
public class isNotProbed extends DefaultInternalAction {
    private static final long serialVersionUID = 2385104427608117167L;

    @Override
    public Object execute(TransitionSystem ts, Unifier unifier, Term[] terms)
            throws Exception {
        Vertex vertex = MapAgent.getInstance().getVertex(terms[0].toString());
        return !vertex.isProbed();
    }
}
