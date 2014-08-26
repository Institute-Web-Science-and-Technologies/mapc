// Internal action code for project mako

package ia;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ListTerm;
import jason.asSyntax.Term;

import java.util.ArrayList;

import eis.JasonHelper;
import eis.MapAgent;
import eis.Vertex;

// Call from AgentSpeak: getClosestVertexFromList(Position, List[Destination*], Vertex)
public class getClosestVertexFromList extends DefaultInternalAction {
    private static final long serialVersionUID = -7762150652742699602L;

    @Override
    public Object execute(TransitionSystem ts, Unifier unifier, Term[] terms)
            throws Exception {
        Vertex position = MapAgent.getInstance().getVertex(terms[0].toString());
        ListTerm list = ((ListTerm) terms[1]);
        Term vertexTerm = terms[2];

        ArrayList<Vertex> vertices = new ArrayList<Vertex>();
        for (Term term : list) {
            vertices.add(MapAgent.getInstance().getVertex(term.toString()));
        }
        Vertex vertex = MapAgent.getInstance().getClosestVertex(position, vertices);

        return unifier.unifies(vertexTerm, JasonHelper.getTerm(vertex.getIdentifier()));
    }
}
