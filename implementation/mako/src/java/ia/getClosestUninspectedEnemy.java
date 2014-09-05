// Internal action code for project mako

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
 * Returns the closest uninspected enemy agent that is not already reserved for
 * inspection. Call from AgentSpeak: ia.getClosestUninspectedEnemy(MyPosition,
 * Enemy, EnemyPosition)
 * 
 * @author sewell
 * 
 */
public class getClosestUninspectedEnemy extends DefaultInternalAction {
    private static final long serialVersionUID = -5353191587385187928L;

    @Override
    public Object execute(TransitionSystem ts, Unifier unifier, Term[] terms)
            throws Exception {
        // ts.getAg().getLogger().info("executing internal action 'ia.getNextUninspectedEnemy'");
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
