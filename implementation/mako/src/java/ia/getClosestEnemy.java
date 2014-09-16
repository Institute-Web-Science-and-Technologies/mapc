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
     * Call from Jason:ia.getClosestEnemy(MyPosition, EnemyPosition, Enemy)
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
        Agent enemyVehicle = MapAgent.getInstance().getClosestEnemy(position);
        if (enemyVehicle == null || enemyVehicle.getPosition() == null) {
            // logger.info("No reachable enemy found.");
            return false;
        }
        Vertex enemyPosition = enemyVehicle.getPosition();
        unifier.unifies(enemyPositionTerm, JasonHelper.getTerm(enemyPosition.getIdentifier()));
        unifier.unifies(enemyVehicleTerm, JasonHelper.getTerm(enemyVehicle.getServerName()));
        return true;
    }
}
