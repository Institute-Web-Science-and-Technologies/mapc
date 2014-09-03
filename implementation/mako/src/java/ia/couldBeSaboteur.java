// Internal action code for project mako

package ia;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;
import eis.Agent;
import eis.JasonHelper;
import eis.MapAgent;

public class couldBeSaboteur extends DefaultInternalAction {

    /**
     * Call from Jason: couldBeSaboteur(Vehicle, VisRange). Returns true if the
     * agent could be a saboteur, that is, if we're not sure it is not a
     * saboteur. The VisRange terms gets unified with the visibility range of
     * the vehicle, or 1 if it is unknown (the default saboteur visibility
     * range).
     * 
     */
    private static final long serialVersionUID = -5309048515703132539L;

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args)
            throws Exception {
        Agent agent = MapAgent.getInstance().getAgent(args[0].toString());
        String agentRole = agent.getRole();
        Integer visRange = agent.getVisRange();
        Term visRangeTerm = args[1];
        un.unifies(visRangeTerm, JasonHelper.getTerm(visRange));
        return ((agentRole == "saboteur") || agentRole == null);
    }
}
