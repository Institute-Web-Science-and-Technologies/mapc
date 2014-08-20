// Internal action code for project mako

package ia;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;
import eis.JasonHelper;
import eis.MapAgent;
import eis.Vertex;

// Call from AgentSpeak: getNextUnprobedVertex(Position, Destination)
public class getNextUnprobedVertex extends DefaultInternalAction {

    /**
     * 
     */
    private static final long serialVersionUID = 2591263020881686750L;

    @Override
    public Object execute(TransitionSystem ts, Unifier unifier, Term[] terms)
            throws Exception {
        Vertex position = MapAgent.getInstance().getVertex(terms[0].toString());
        Term destinationTerm = terms[1];

        Vertex destination = MapAgent.getInstance().getNextUnprobedVertex(position);

        return unifier.unifies(destinationTerm, JasonHelper.getTerm(destination.getIdentifier()));
    }
}
