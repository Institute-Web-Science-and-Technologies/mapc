// Internal action code for project mako

package ia;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;
import eis.MapAgent;
import eis.Vertex;

// Call from AgentSpeak: destroyZone(CenterVertex, Size)
public class destroyZone extends DefaultInternalAction {

    private static final long serialVersionUID = -2140618985282077257L;

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args)
            throws Exception {
        // execute the internal action
        Vertex centerVertex = MapAgent.getInstance().getVertex(args[0].toString());
        int size = Integer.parseInt(args[1].toString());
        MapAgent.getInstance().destroyZone(centerVertex, size);
        return true;
    }
}
