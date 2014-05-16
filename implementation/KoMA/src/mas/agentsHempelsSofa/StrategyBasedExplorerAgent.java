package mas.agentsHempelsSofa;

import java.util.LinkedList;

import mas.agentsHempelsSofa.data.AgentToken;
import mas.agentsHempelsSofa.data.graph.Vertex;
import mas.agentsHempelsSofa.data.zone.Zone;
import massim.javaagents.agents.MarsUtil;
import eis.iilang.Action;
import eis.iilang.Identifier;
import eis.iilang.Percept;

/**
 * A GoalOrientedAgent which's goal is to explore the environment through
 * probing, surveying and moving.
 * @author Hempels-Sofa
 */
public class StrategyBasedExplorerAgent extends StrategyBasedAgent {

    /**
     * Creates a new Strategy based Explorer Agent.
     * @param name the name of the agent.
     * @param team the team of the agent.
     */
    public StrategyBasedExplorerAgent ( String name, String team ) {
        super(name, team);
    }

    /**
     * this makes the explorer agent probe vertices inside of the main zone.
     * when every vertex inside of the zone is probed, he tries to expand the
     * zone
     */
    public Action probeMainZone () {
        myToken.setStrategyType(AgentToken.STRATEGY_ZONE_BUILDER);
        // when his last action was probe -> do survey
        Percept lastAction = MarsUtil.filterPercepts(getPercepts(),
                "lastAction").getFirst();
        if ( lastAction.getParameters().getFirst().toProlog().equals("probe")
                && graph.getPosition().hasUnsurveyedEdges() ) {

            return new Action("survey");
        }
        if ( MarsUtil.filterPercepts(getPercepts(), "lastAction").getFirst().getParameters().getFirst().toProlog().equals(
                "probe")
                && graph.getPosition().hasUnsurveyedEdges() ) {
            return new Action("survey");
        }
        if ( environment.getLastAction().equals("probe")
                && graph.getPosition().hasUnsurveyedEdges() )
            return new Action("survey");

        // >>>>>>>>>>probe the mainZones vertices<<<<<<<<<<<<<<<<<<<<
        Zone mainZone = getZoneManager().getMostPreciousZone();
        if ( mainZone == null
                || (!graph.getConnectedComponent(
                        mainZone.getVertices().getFirst()).contains(
                        graph.getPosition())) ) {
            // when there is no mainZone or he is not in the mainZones connected
            // component
            return ag.expandOwnComponent();
        }

        // if agent on unproved vertex inside of mainZone -> probe
        if ( (mainZone.contains(getGraph().getPosition()))
                && (!getGraph().getPosition().isProbed()) ) {
            return new Action("probe");
        }

        // when his vertex is probed, move towards a promising unproved vertex
        // (high-value-neighbors)
        LinkedList<Vertex> candidates = mainZone.getMostPreciousVertices();
        for ( Vertex v : candidates ) {
            for ( Vertex u : v.getAdjacentVertices() )
                if ( mainZone.contains(u) && !u.isProbed() )
                    return ag.moveTowards(u);
        }
        // all vertices in Zone are probed -> buildZone()
        return ag.expandZoneByValue(mainZone);
    }

    @Override
    public Action generateAchievementAction ( String focus ) {
        return probeMainZone();
    }

    @Override
    public Action generateBuyAction ( String focus ) {
        myToken.setStrategyType(AgentToken.STRATEGY_UNKNOWN);
        Action ret = new Action("buy", new Identifier(
                Character.toLowerCase(focus.charAt(3)) + focus.substring(4)));
        return ret;
    }

    @Override
    public Action generateZoneAction ( String focus ) {
        myToken.setStrategyType(AgentToken.STRATEGY_EXPLORER);
        Zone zone = getZoneManager().getMostPreciousZone();
        if ( zone == null )
            return ag.expandOwnComponent();
        return ag.expandZoneBySize(getZoneManager().getMostPreciousZone());
    }

    @Override
    public Action generateOffensiveAction ( String focus ) {

        return probeMainZone();
    }

    @Override
    public void handleAgentSpecificMessages () {

    }

    @Override
    public void preferencesRevision () {
    	if ( enemySaboteurAt(myToken.getPosition())){
    			getStrategy().defensivePref=2.0;
    			getStrategy().defensiveParryPref=0.7;
    			getStrategy().defensiveRunAwayPref=0.3;
    	}
    		
        if ( myToken.isDisabled() ) {
            getStrategy().defensivePref = 1.0;
            getStrategy().defensiveRepairPref = 1.0;
        }

    }

    @Override
    public Action generateDefensiveAction ( String focus ) {
    	if(focus == "defensiveParry")
    		return new Action("parry");
        if ( myToken.isDisabled() ) {
            return ag.moveTowardsAgent("Repairer");
        }
        Zone zone = getZoneManager().getBiggestZone();
        if ( zone != null )
            return ag.moveTowardsNearest(zone.getCriticalFrontier());
        return new Action("recharge");
    }

}
