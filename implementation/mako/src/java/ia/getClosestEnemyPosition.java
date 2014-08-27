// Internal action code for project mako

package ia;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;
import eis.JasonHelper;
import eis.MapAgent;
import eis.Vertex;

//Call from AgentSpeak: getClosestEnemyPosition(Position, EnemyPosition)
public class getClosestEnemyPosition extends DefaultInternalAction {

    /**
     * 
     */
    private static final long serialVersionUID = 5971894875840931912L;

    @Override
    public Object execute(TransitionSystem ts, Unifier unifier, Term[] terms)
            throws Exception {
        Vertex position = MapAgent.getInstance().getVertex(terms[0].toString());
        Term enemyPositionTerm = terms[1];

        Vertex enemyPosition = MapAgent.getInstance().getClosestEnemyPosition(position);

        unifier.unifies(enemyPositionTerm, JasonHelper.getTerm(enemyPosition.getIdentifier()));

        return enemyPosition != position;
    }
}
