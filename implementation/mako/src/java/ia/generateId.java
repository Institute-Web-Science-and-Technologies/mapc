// Internal action code for project mako

package ia;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;
import eis.JasonHelper;

/**
 * Call from Jason: generateId(Id)
 * <p>
 * Generates an id from a current timestamp in ms.
 * 
 */
public class generateId extends DefaultInternalAction {

    private static final long serialVersionUID = 1768097879789275154L;

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args)
            throws Exception {
        long time = System.currentTimeMillis();

        Term idTerm = args[0];
        return un.unifies(idTerm, JasonHelper.getTerm(time));
    }
}
