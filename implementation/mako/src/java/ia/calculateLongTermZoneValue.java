package ia;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.Term;
import jason.asSyntax.VarTerm;

/**
 * If it takes longer to reach the zone than we calculate its value, it's
 * worth nothing (0). Else, the value is determined by the value times the
 * amount of steps we actually stay within the zone. This assumes that there
 * is nothing in the way to reach our goal.
 * 
 * Use this internal action as follows:
 * {@code ia.calculateLongTermZoneValue(Value, Distance, PlannedZoneTimeInSteps, PrognosedValue)}
 * where {@code PrognosedValue} quantifies with the result.
 */
public class calculateLongTermZoneValue extends DefaultInternalAction {
    private static final long serialVersionUID = -5894450673263444382L;

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args)
            throws Exception {
        double value = (double) ((NumberTerm) args[0]).solve();
        int distance = (int) ((NumberTerm) args[1]).solve();
        int plannedZoneTimeInSteps = (int) ((NumberTerm) args[2]).solve();
        VarTerm prognosedValue = ((VarTerm) args[3]);

        double calculationResult = 0D;
        if (plannedZoneTimeInSteps > distance) {
            calculationResult = (plannedZoneTimeInSteps - distance) * value;
        }
        NumberTerm newValue = new NumberTermImpl(calculationResult);
        un.bind(prognosedValue, newValue);
        // everything ok, so returns true
        return true;
    }
}