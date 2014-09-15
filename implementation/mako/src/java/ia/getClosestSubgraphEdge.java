// Internal action code for project mako

package ia;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;
import eis.JasonHelper;
import eis.MapAgent;
import eis.Vertex;

/**
 * Call from AS: ia.getClosestSubgraphEdge(AgentPosition, *EdgeNode)
 * 
 * Unifies EdgeNode with the closest node that the agent can reach and that
 * hasn't been visited before. Returns false if no such node exists.
 * 
 * @author sewell
 */
public class getClosestSubgraphEdge extends DefaultInternalAction {

    private static final long serialVersionUID = 6131658812400888714L;

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args)
            throws Exception {
        Vertex agentPosition = MapAgent.getInstance().getVertex(args[0].toString());
        Term edgeNodeTerm = args[1];

        Vertex edgeNode = MapAgent.getInstance().getClosestSubgraphEdge(agentPosition);
        if (edgeNode == null) {
            return false;
        }
        return un.unifies(edgeNodeTerm, JasonHelper.getTerm(edgeNode.getIdentifier()));
    }
}
