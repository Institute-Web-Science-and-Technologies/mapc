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

/**
 * Call from AgentSpeak: getClosestRepairer(DisabledAgent, Repairer,
 * RepairerPosition)
 * <p>
 * Given the DisabledAgent, unifies the Repairer variable with the closest
 * friendly, not-disabled, not-reserved repairer agent, and RepairerPosition
 * with his position. Returns false if no such agent could be found.
 */
public class getClosestRepairer extends DefaultInternalAction {

    private static final long serialVersionUID = -5270616698277058299L;

    @Override
    public Object execute(TransitionSystem ts, Unifier unifier, Term[] args)
            throws Exception {
        Agent disabledAgent = MapAgent.getInstance().getAgent(args[0].toString());
        Term repairerTerm = args[1];
        Term repairerPositionTerm = args[2];

        Agent repairer = MapAgent.getInstance().getClosestRepairer(disabledAgent);
        if (repairer == null) {
            return false;
        }
        Vertex repairerPosition = repairer.getPosition();
        unifier.unifies(repairerTerm, JasonHelper.getTerm(repairer.getJasonName()));
        unifier.unifies(repairerPositionTerm, JasonHelper.getTerm(repairerPosition));
        return true;
    }
}
