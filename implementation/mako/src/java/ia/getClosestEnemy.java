// Internal action code for project mako

package ia;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;

import java.util.logging.Logger;

import eis.Agent;
import eis.JasonHelper;
import eis.MapAgent;
import eis.Vertex;

public class getClosestEnemy extends DefaultInternalAction {

    /**
     * 
     */
    private static final long serialVersionUID = -3662866054390804856L;

    @Override
    public Object execute(TransitionSystem ts, Unifier unifier, Term[] terms)
            throws Exception {
        Logger logger = ts.getAg().getLogger();
        Vertex position = MapAgent.getInstance().getVertex(terms[0].toString());
        Term enemyPositionTerm = terms[1];
        Term enemyVehicleTerm = terms[2];
        // logger.info("Closest enemy debug: Entering ia.getClosestEnemy(" +
        // position + "," + enemyPositionTerm.toString() + "," +
        // enemyVehicleTerm.toString() + ")");

        // Vertex enemyPosition =
        // MapAgent.getInstance().getClosestEnemyPosition(position);
        Agent enemyVehicle = MapAgent.getInstance().getClosestEnemy(position);
        if (enemyVehicle == null) {
            logger.info("No reachable enemy found.");
            return false;
        }
        Vertex enemyPosition = enemyVehicle.getPosition();
        // logger.info("enemyVehicle: " + enemyVehicle + ". enemyPosition: " +
        // enemyPosition);

        unifier.unifies(enemyPositionTerm, JasonHelper.getTerm(enemyPosition.getIdentifier()));
        unifier.unifies(enemyVehicleTerm, JasonHelper.getTerm(enemyVehicle.getJasonName()));
        // logger.info("Closest enemy debug: Leaving ia.getClosestEnemy(" +
        // position + "," + enemyPositionTerm.toString() + "," +
        // enemyVehicleTerm.toString() + ")");
        return true;
    }
}
