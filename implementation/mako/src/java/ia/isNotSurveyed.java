// Internal action code for project mako

package ia;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;
import eis.MapAgent;
import eis.Vertex;

//Call from AgentSpeak: isNotSurveyed(Vertex)
public class isNotSurveyed extends DefaultInternalAction {
    private static final long serialVersionUID = 1410005108812986441L;

    @Override
    public Object execute(TransitionSystem ts, Unifier unifier, Term[] terms)
            throws Exception {
        Vertex vertex = MapAgent.getInstance().getVertex(terms[0].toString());
        return !MapAgent.getInstance().isVertexSurveyed(vertex);
    }
}
