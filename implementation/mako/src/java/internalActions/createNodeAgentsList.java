package internalActions;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ListTerm;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.Literal;
import jason.asSyntax.LiteralImpl;
import jason.asSyntax.Term;
import jason.asSyntax.VarTerm;

public class createNodeAgentsList extends DefaultInternalAction {

    private static final long serialVersionUID = -6678665894129279334L;

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args)
            throws Exception {
        VarTerm nodeAgentNames = ((VarTerm) args[0]);
        ListTerm l = new ListTermImpl();
        for (int i = 0; i < 625; i++) {
            Literal currentVertexName = new LiteralImpl("v" + i);
            l.append(currentVertexName);
        }
        un.bind(nodeAgentNames, l);
        // everything ok, so returns true
        return true;
    }
}
