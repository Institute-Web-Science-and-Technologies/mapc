package ia;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;

import eis.Agent;
import eis.JasonHelper;
import eis.MapAgent;
import eis.Vertex;

/**
 * Call from AgentSpeak: getClosestEnemy(MyPosition, *EnemyPosition, *Enemy)
 * <p>
 * Given the agent's position (MyPosition), finds the closest enemy to that
 * position that the MapAgent knows about and unifies EnemyPosition and Enemy
 * with the enemy's position and its name, respectively. If there are multiple
 * agents at the same minimal distance, and one of them is a saboteur, it will
 * choose the saboteur. Returns false if no closest enemy is found.
 */
public class getClosestEnemy extends DefaultInternalAction {

    private static final long serialVersionUID = -3662866054390804856L;

    @Override
    public Object execute(TransitionSystem ts, Unifier unifier, Term[] terms)
            throws Exception {
        Vertex position = MapAgent.getInstance().getVertex(terms[0].toString());
        Term enemyPositionTerm = terms[1];
        Term enemyVehicleTerm = terms[2];
        Agent enemyVehicle = MapAgent.getInstance().getClosestEnemy(position);
        if (enemyVehicle == null || enemyVehicle.getPosition() == null) {
            return false;
        }
        Vertex enemyPosition = enemyVehicle.getPosition();
        unifier.unifies(enemyPositionTerm, JasonHelper.getTerm(enemyPosition.getIdentifier()));
        unifier.unifies(enemyVehicleTerm, JasonHelper.getTerm(enemyVehicle.getServerName()));
        return true;
    }
}
