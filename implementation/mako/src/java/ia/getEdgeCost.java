// Internal action code for project mako

package ia;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;
import eis.JasonHelper;
import eis.MapAgent;
import eis.Vertex;

// Call from AgentSpeak: getEdgeCost(Position, Destination, Costs) 
public class getEdgeCost extends DefaultInternalAction {

    private static final long serialVersionUID = 7036124114716986040L;

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args)
            throws Exception {
        Vertex position = MapAgent.getInstance().getVertex(args[0].toString());
        Vertex destination = MapAgent.getInstance().getVertex(args[1].toString());
        Vertex nextHop = position.getPath(destination).getNextHopVertex();
        int costs = position.getEdgeCost(nextHop);
        un.unifies(args[2], JasonHelper.getTerm(costs));
        return costs != -1;
    }
}
