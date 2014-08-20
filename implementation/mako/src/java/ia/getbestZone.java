// Internal action code for project mako

package ia;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.StringTerm;
import jason.asSyntax.Term;

import java.util.logging.Logger;

import eis.MapAgent;

public class getbestZone extends DefaultInternalAction {

    private static final long serialVersionUID = -6937681288781906625L;

    // input: position - range
    // output: ZoneValuePerAgent - centerVertex - List of Vertices
    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args)
            throws Exception {
        Logger logger = ts.getAg().getLogger();
        logger.info("executing internal action 'ia.getbestZone'");
        for (int i = 0; i < args.length; i++) {
            logger.info("terms[" + i + "]: " + args[i]);
        }
        StringTerm position = (StringTerm) args[0];
        NumberTerm range = (NumberTerm) args[1];
        MapAgent mapAgent = MapAgent.getInstance();

        return true;
    }
}
