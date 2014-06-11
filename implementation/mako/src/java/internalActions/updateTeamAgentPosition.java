// Internal action code for project mako

package internalActions;

import eis.iilang.Identifier;
import graph.Graph;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.asSyntax.Term;

public class updateTeamAgentPosition extends DefaultInternalAction {

    private static final long serialVersionUID = -4676929073688556989L;

    /**
     * @param ts
     *            leads to agent executing the message Unifier is return value
     *            (setting
     * @param Unifier
     *            will send it back to Jason)
     * @param terms
     *            are the arguments from Jason (like the vertex)
     **/
    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] terms)
            throws Exception {

        if (terms[0].isLiteral()) {
            String agentNameString = ((Literal) terms[0]).getFunctor();
            if (terms[1].isLiteral()) {
                String vertexIDString = ((Literal) terms[1]).getFunctor();

                Graph graph = Graph.getInstance();
                graph.updateTeamAgentPosition(new Identifier(agentNameString), new Identifier(vertexIDString));

                return true;
            }
        }
        return false;
    }
}
