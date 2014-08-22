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
 * For a zone of a given size, returns the best node to put an agent on to
 * increase the size of the zone by one. If no zone of a bigger size is stored
 * for that node, then return the original center node.
 * <p>
 * Jason call: ia.getExtraZoneSpot(CenterNode, CurrentSize, ExtraAgentPosition)
 * 
 * @author sewell
 */
public class getExtraZoneSpot extends DefaultInternalAction {
    private static final long serialVersionUID = 7846071232823858873L;

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args)
            throws Exception {
        Vertex centerNode = MapAgent.getInstance().getVertex(args[0].toString());
        int currentSize = Integer.parseInt(args[1].toString());
        Vertex extraAgentPosition = centerNode.getZoneMap().getNextAgentPosition(currentSize);
        if (extraAgentPosition == null) {
            return un.unifies(args[2], args[0]);
        } else {
            return un.unifies(args[2], JasonHelper.getTerm(extraAgentPosition));
        }
    }
}
