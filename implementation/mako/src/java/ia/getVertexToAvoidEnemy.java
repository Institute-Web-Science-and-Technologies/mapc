// Internal action code for project mako

package ia;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;

import java.util.ArrayList;

import eis.JasonHelper;
import eis.MapAgent;
import eis.Vertex;

// Call from AgentSpeak: getVertexToAvoidEnemy(Position, Destination)
public class getVertexToAvoidEnemy extends DefaultInternalAction {

    private static final long serialVersionUID = -4778054080904036925L;

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args)
            throws Exception {
        Vertex position = MapAgent.getInstance().getVertex(args[0].toString());
        ArrayList<Vertex> safeNeighbours = MapAgent.getInstance().getSafeNeighbours(position);
        if (safeNeighbours.size() == 0) {
            return un.unifies(args[1], args[0]);
        } else {
            return un.unifies(args[1], JasonHelper.getTerm(safeNeighbours.get(0)));
        }
    }
}
