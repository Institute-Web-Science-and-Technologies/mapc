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
 * Call from AgentSpeak: ia.getClosestUninspectedEnemy(MyPosition,
 * *Enemy, *EnemyPosition)
 * <p>
 * Given MyPosition, finds the closest uninspected enemy and unifies Enemy
 * and EnemyPosition with that enemy's name and position, respectively. Returns
 * false if no such enemy is found.
 *
 * @author sewell
 */
public class getClosestUninspectedEnemy extends DefaultInternalAction {
    private static final long serialVersionUID = -5353191587385187928L;

    @Override
    public Object execute(TransitionSystem ts, Unifier unifier, Term[] terms)
            throws Exception {
        Vertex position = MapAgent.getInstance().getVertex(terms[0].toString());
        Term enemyTerm = terms[1];
        Term enemyPositionTerm = terms[2];

        Agent enemy = MapAgent.getInstance().getClosestUninspectedEnemy(position);
        if (enemy == null) {
            return false;
        }
        Vertex enemyPosition = enemy.getPosition();

        unifier.unifies(enemyTerm, JasonHelper.getTerm(enemy.getServerName()));
        unifier.unifies(enemyPositionTerm, JasonHelper.getTerm(enemyPosition.getIdentifier()));
        return true;
    }
}
