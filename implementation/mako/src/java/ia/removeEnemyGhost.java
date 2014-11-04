package ia;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;
import eis.Agent;
import eis.MapAgent;

/**
 * Call from AgentSpeak: removeEnemyGhost(Enemy)
 * <p>
 * Used to tell the MapAgent that it is wrong about the location of an enemy
 * agent. This happens when enemy agents move outside of the visibility range of
 * our own agents.
 *
 * @author sewell
 */
public class removeEnemyGhost extends DefaultInternalAction {

    private static final long serialVersionUID = -7882442591918145284L;

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args)
            throws Exception {
        Agent agent = MapAgent.getInstance().getAgent(args[0].toString());
        agent.setPosition(null);
        return true;
    }
}
