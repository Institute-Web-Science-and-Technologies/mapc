package internalActions;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ListTerm;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.Literal;
import jason.asSyntax.LiteralImpl;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Term;
import jason.asSyntax.VarTerm;

public class createNodeAgentsList extends DefaultInternalAction {

    private static final long serialVersionUID = -6678665894129279334L;

    /**
     * Returns a list of vertex names ["v0", "v1", ...]. It generates as many
     * items as specified with {@code args[0]}.
     */
    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args)
            throws Exception {
        int amount = (int) ((NumberTerm) args[0]).solve();
        VarTerm nodeAgentNames = ((VarTerm) args[1]);
        ListTerm l = new ListTermImpl();
        for (int i = 0; i < amount; i++) {
            Literal currentVertexName = new LiteralImpl("v" + i);
            l.append(currentVertexName);
        }
        un.bind(nodeAgentNames, l);
        // everything ok, so returns true
        return true;
    }
}
