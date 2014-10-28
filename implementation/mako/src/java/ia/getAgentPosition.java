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
 * Call from AgentSpeak: getAgentPosition(JasonName, *Position)
 * <p>
 * Unifies the *Position of the agent with the given JasonName, or returns false
 * if the agent's position is not known.
 */
public class getAgentPosition extends DefaultInternalAction {
    private static final long serialVersionUID = 3056480662675453121L;

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args)
            throws Exception {
        Agent agent = MapAgent.getInstance().getAgent(args[0].toString());
        Vertex position = agent.getPosition();

        if (position != null) {
            return un.unifies(args[1], JasonHelper.getTerm(position));
        } else {
            return false;
        }
    }
}
