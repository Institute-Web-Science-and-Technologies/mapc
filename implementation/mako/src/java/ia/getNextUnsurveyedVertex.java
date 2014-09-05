package ia;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;
import eis.JasonHelper;
import eis.MapAgent;
import eis.Vertex;

//Call from AgentSpeak: getNextUnsurveyedVertex(Position, Destination)
public class getNextUnsurveyedVertex extends DefaultInternalAction {
    private static final long serialVersionUID = 4113666835276193698L;

    @Override
    public Object execute(TransitionSystem ts, Unifier unifier, Term[] terms)
            throws Exception {
        Vertex position = MapAgent.getInstance().getVertex(terms[0].toString());
        Term destinationTerm = terms[1];

        Vertex destination = MapAgent.getInstance().getNextUnsurveyedVertex(position);
        if (position == destination) {
            return false;
        }

        return unifier.unifies(destinationTerm, JasonHelper.getTerm(destination.getIdentifier()));
    }
}
