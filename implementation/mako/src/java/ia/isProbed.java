// Internal action code for project mako

package ia;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;
import eis.JasonHelper;
import eis.MapAgent;
import eis.Vertex;

//Call from AgentSpeak: isProbed(Vertex, Result)
public class isProbed extends DefaultInternalAction {
    private static final long serialVersionUID = 2385104427608117167L;

    @Override
    public Object execute(TransitionSystem ts, Unifier unifier, Term[] terms)
            throws Exception {
        Vertex vertex = MapAgent.getInstance().getVertex(terms[0].toString());
        Term resultTerm = terms[1];

        boolean result = MapAgent.getInstance().isVertexProbed(vertex);

        return unifier.unifies(resultTerm, JasonHelper.getTerm(result));
    }
}
