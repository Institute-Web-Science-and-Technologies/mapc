// Internal action code for project mako

package ia;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;
import eis.JasonHelper;
import eis.MapAgent;
import eis.Vertex;

// Call from AgentSpeak: getNextBestValueVertex(Position, Range, Destination)
public class getNextBestValueVertex extends DefaultInternalAction {
    static final long serialVersionUID = 5681783068132914648L;

    @Override
    public Object execute(TransitionSystem ts, Unifier unifier, Term[] terms)
            throws Exception {
        // execute the internal action
        Vertex position = MapAgent.getInstance().getVertex(terms[0].toString());
        int range = Integer.parseInt(terms[1].toString());
        Term destinationTerm = terms[2];
        Vertex destination = MapAgent.getInstance().getNextBestValueVertex(position, range);

        return unifier.unifies(destinationTerm, JasonHelper.getTerm(destination.getIdentifier()));
    }
}
