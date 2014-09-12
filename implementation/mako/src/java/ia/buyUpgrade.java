// Internal action code for project mako

package ia;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;
import eis.Agent;
import eis.JasonHelper;
import eis.MapAgent;

/**
 * Call from Jason: buyUpgrade(Agent, *Upgrade)
 * <p>
 * Chooses the best upgrade to buy for the agent. Returns false if no money for
 * upgrades is available.
 * 
 */
public class buyUpgrade extends DefaultInternalAction {

    private static final long serialVersionUID = -2180832453021570338L;

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args)
            throws Exception {
        // ts.getAg().getLogger().info("executing internal action 'ia.buyUpgrade'");
        int currentMoney = MapAgent.getInstance().getMoney();
        if (currentMoney < 2) {
            return false;
        }
        MapAgent.getInstance().increaseMoneySpentThisStep();
        Agent agent = MapAgent.getInstance().getAgent(args[0].toString());
        Term upgradeTerm = args[1];
        String upgrade = agent.getBestUpgrade();
        return un.unifies(upgradeTerm, JasonHelper.getTerm(upgrade));
    }
}
