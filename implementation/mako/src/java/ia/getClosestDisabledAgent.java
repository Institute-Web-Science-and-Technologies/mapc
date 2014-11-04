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
 * Call from AgentSpeak: getClosestDisabledAgent(Repairer, *DisabledAgent,
 * *DisabledAgentPosition)
 * <p>
 * Given the Repairer, unifies the DisabledAgent variable with the closest
 * friendly, disabled, non-reserved agent, and DisabledAgentPosition with his
 * position. Returns false if no such agent could be found.
 */
public class getClosestDisabledAgent extends DefaultInternalAction {

    private static final long serialVersionUID = 1776735461289451166L;

    @Override
    public Object execute(TransitionSystem ts, Unifier unifier, Term[] args)
            throws Exception {
        Agent repairer = MapAgent.getInstance().getAgent(args[0].toString());
        Term disabledAgentTerm = args[1];
        Term disabledAgentPositionTerm = args[2];

        Agent disabledAgent = MapAgent.getInstance().getClosestDisabledAgent(repairer);
        if (disabledAgent == null) {
            return false;
        }
        Vertex disabledAgentPosition = disabledAgent.getPosition();
        unifier.unifies(disabledAgentTerm, JasonHelper.getTerm(disabledAgent.getJasonName()));
        unifier.unifies(disabledAgentPositionTerm, JasonHelper.getTerm(disabledAgentPosition));
        return true;
    }
}
