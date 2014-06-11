// Internal action code for project mako

package internalActions;

import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;

public class updateTeamAgentPosition extends DefaultInternalAction {

    /**
     * @param ts
     *            leads to agent executing the message Unifier is return value
     *            (setting
     * @param Unifier
     *            will send it back to Jason)
     * @param args
     *            are the arguments from Jason (like the vertex)
     **/
    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args)
            throws Exception {
        // execute the internal action
        ts.getAg().getLogger().info("executing internal action 'internalActions.updateTeamAgentPosition'");
        if (true) { // just to show how to throw another kind of exception
            throw new JasonException("not implemented!");
        }

        // everything ok, so returns true
        return true;
    }
}
