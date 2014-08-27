// Internal action code for project mako

package ia;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Term;

import java.util.List;
import java.util.logging.Logger;

import eis.JasonHelper;
import eis.MapAgent;
import eis.Vertex;
import eis.Zone;

//Call from AgentSpeak: getBestZone(Position, Range, ZoneValuePerAgent, CenterVertex, ListOfAgents)
public class getBestZone extends DefaultInternalAction {
    private static final long serialVersionUID = -6937681288781906625L;

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args)
            throws Exception {
        Logger logger = ts.getAg().getLogger();
        logger.info("executing internal action 'ia.getbestZone'");

        for (int i = 0; i < args.length; i++) {
            logger.info("terms[" + i + "]: " + args[i]);
        }

        Term zoneValuePerAgent = args[2];
        Term centerVertex = args[3];
        Term listOfAgents = args[4];

        MapAgent mapAgent = MapAgent.getInstance();
        int range = (int) ((NumberTerm) args[1]).solve();
        Vertex vertex = mapAgent.getVertex(args[0].toString());
        Zone zone = mapAgent.getBestZone(mapAgent.getZonesInRange(vertex, range));

        List<String> closestAgents = mapAgent.getClosestAgentsToZone(zone.getCenter(), zone.getPositions().size());
        if (closestAgents.size() > 0) {
            Double zoneValue = zone.getZoneValuePerAgent() / closestAgents.size();
            un.unifies(zoneValuePerAgent, JasonHelper.getTerm(zoneValue));
            un.unifies(centerVertex, JasonHelper.getTerm(zone.getCenter().getIdentifier()));
            un.unifies(listOfAgents, JasonHelper.getStringList(closestAgents));
        }
        return closestAgents.size() == zone.getPositions().size();
    }

}
