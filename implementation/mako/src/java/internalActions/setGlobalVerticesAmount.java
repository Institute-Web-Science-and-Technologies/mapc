// Internal action code for project mako

package internalActions;

import eis.iilang.Numeral;
import graph.Graph;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Term;

/**
 * 
 * @author Sergey Dedukh
 * 
 */
public class setGlobalVerticesAmount extends DefaultInternalAction {

    private static final long serialVersionUID = -670351908774381635L;

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args)
            throws Exception {
        // execute the internal action
        if (args.length == 1) {
            if (args[0].isNumeric()) {
                double globalVerticesAmount = ((NumberTerm) args[0]).solve();

                Graph graph = Graph.getInstance();
                graph.setGlobalVerticesAmount(new Numeral(globalVerticesAmount));

                return true;
            }
        }
        return false;
    }
}
