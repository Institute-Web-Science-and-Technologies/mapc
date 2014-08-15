package ia;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Atom;
import jason.asSyntax.Term;

import java.util.Date;

import eis.AgentLogger;
import eis.MapAgent;

public class getNextUnsurveyedVertices extends DefaultInternalAction {
    private AgentLogger logger = new AgentLogger("getNextUnsurveyedVertices");

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] terms)
            throws Exception {
        Date start = new Date();
        logger.setVisible(true);
        logger.info("TransitionSystem: " + ts);
        logger.info("Unifier: " + un.toString());
        logger.info("Term[]: " + terms.toString());
        String position = ((Atom) terms[0]).getFunctor();
        logger.info("position: " + position);
        Term vertices = terms[1];
        logger.info("vertices: " + vertices);

        un.unifiesNoUndo(vertices, MapAgent.getInstance().getNextUnsurveyedVertices(position));
        Date end = new Date();
        Long diff = end.getTime() - start.getTime();
        logger.info("diff :" + diff);
        return true;
    }
}
