// Internal action code for project mako

package ia;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ListTerm;
import jason.asSyntax.Term;

import java.util.ArrayList;
import java.util.HashMap;

import eis.JasonHelper;
import eis.MapAgent;
import eis.Vertex;

// Call from AgentSpeak: placeAgentsOnZone(Vertex, List[Agent*], List[[Agent|Position]*]
public class placeAgentsOnZone extends DefaultInternalAction {

    private static final long serialVersionUID = -2434013129946995372L;

    /**
     * @return {@code false} if not all nodes could be filled with agents as
     *         some could not reach the nodes.
     */
    @Override
    public Object execute(TransitionSystem ts, Unifier unifier, Term[] terms)
            throws Exception {
        Vertex vertex = MapAgent.getInstance().getVertex(terms[0].toString());
        ListTerm list = ((ListTerm) terms[1]);
        Term agentPositionsTerm = terms[2];
        ArrayList<String> agents = new ArrayList<String>();
        for (Term term : list) {
            agents.add(term.toString());
        }
        HashMap<String, Vertex> agentPositions = MapAgent.getInstance().getAgentZonePositions(vertex, agents);

        if (agentPositions.isEmpty()) {
            return false;
        } else {
            return unifier.unifies(agentPositionsTerm, JasonHelper.getTerm(agentPositions));
        }
    }
}
