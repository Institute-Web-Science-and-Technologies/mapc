package ia;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Atom;
import jason.asSyntax.Term;
import eis.AgentLogger;
import eis.MapAgent;

public class getNextUnsurveyedVertices extends DefaultInternalAction {
    private static final long serialVersionUID = 4113666835276193698L;
    private AgentLogger logger = new AgentLogger("getNextUnsurveyedVertices");

    // input: position
    // output: closest unsurveyed Vertex and next Hop
    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] terms)
            throws Exception {
        // Date start = new Date();
        logger.setVisible(true);
        // logger.info("TransitionSystem: " + ts);
        logger.info("Unifier: " + un);
        for (int i = 0; i < terms.length; i++) {
            logger.info("terms[" + i + "]: " + terms[i]);
        }
        String agentPosition = ((Atom) terms[0]).getFunctor();
        logger.info("agentPposition: " + agentPosition);
        Term vertices = terms[1];
        logger.info("vertices: " + vertices);

        return un.unifies(vertices, MapAgent.getInstance().getNextUnsurveyedVertices(agentPosition));
        // Date end = new Date();
        // Long diff = end.getTime() - start.getTime();
        // logger.info("diff :" + diff);
    }
}
