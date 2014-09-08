// Internal action code for project mako

package ia;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;
import eis.Agent;
import eis.JasonHelper;
import eis.MapAgent;
import eis.Vertex;

// Call from AgentSpeak: getNextHopToClosestDisabledAgent(RepairerName, Destination)
public class getNextHopToClosestDisabledAgent extends DefaultInternalAction {

    private static final long serialVersionUID = 1776735461289451166L;

    @Override
    public Object execute(TransitionSystem ts, Unifier unifier, Term[] args)
            throws Exception {
        Agent repairer = MapAgent.getInstance().getAgent(args[0].toString());
        Term destinationTerm = args[1];

        Vertex destination = MapAgent.getInstance().getClosestDisabledAgent(repairer);
        if (destination != null) {
            Vertex nextHop = MapAgent.getInstance().getBestHopToVertex(repairer.getPosition(), destination);
            return unifier.unifies(destinationTerm, JasonHelper.getTerm(nextHop));
        } else {
            return unifier.unifies(destinationTerm, JasonHelper.getTerm(repairer.getPosition()));
        }
    }
}
