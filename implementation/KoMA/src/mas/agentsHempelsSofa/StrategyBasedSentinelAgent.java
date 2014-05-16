package mas.agentsHempelsSofa;

import mas.agentsHempelsSofa.data.AgentToken;
import mas.agentsHempelsSofa.data.Strategy;
import mas.agentsHempelsSofa.data.zone.Zone;
import eis.iilang.Action;
import eis.iilang.Identifier;

public class StrategyBasedSentinelAgent extends StrategyBasedAgent {

    private Zone myZone;
    private Strategy disabledStrategy = new Strategy();
    private Strategy surveyStrategy = new Strategy();
    private Strategy parryStrategy = new Strategy();
    private Strategy stabilizeZoneStrategy = new Strategy();
    private Strategy recoverZoneOrParryStrategy = new Strategy();
    private Strategy inMainZoneStrategy = new Strategy();
    private Strategy gotoMainZoneStrategy = new Strategy();
    private Zone mostPreciousZone;

    public StrategyBasedSentinelAgent ( String name, String team ) {
        super(name, team);
        defineStrategies();
    }

    private void defineStrategies () {
        disabledStrategy.defensivePref = 1;
        disabledStrategy.defensiveRunAwayPref = 1;

        surveyStrategy.achievementsPref = 1;
        surveyStrategy.achievementsSurveyedEdgesPref = 1;

        parryStrategy.defensivePref = 1;
        parryStrategy.defensiveParryPref = 1;

        stabilizeZoneStrategy.zonePref = 1;
        stabilizeZoneStrategy.zoneStabilityPref = 1;

        recoverZoneOrParryStrategy.defensivePref = 1.3;
        recoverZoneOrParryStrategy.defensiveParryPref = 1;
        recoverZoneOrParryStrategy.zonePref = 1.7;
        recoverZoneOrParryStrategy.zoneStabilityPref = 1;

        inMainZoneStrategy.zonePref = 1;
        inMainZoneStrategy.zoneExpandPref = 2 - 1. / 20;

        gotoMainZoneStrategy.zonePref = 1;
        gotoMainZoneStrategy.zoneMainZonePref = 1;
    }

    @Override
    public void handleAgentSpecificMessages () {

    }

    @Override
    public void preferencesRevision () {
        myToken.setStrategyType(AgentToken.STRATEGY_ZONE_BUILDER);
        // if disabled, go to repairer
        if ( myToken.isDisabled() )
            loadStrategy(disabledStrategy);
        myZone = getZoneManager().getZone(graph.getPosition());
        mostPreciousZone = getZoneManager().getMostPreciousZone();
        // if there are unsurveyed edges
        if ( graph.getPosition().hasUnsurveyedEdges() )
            loadStrategy(surveyStrategy);
        // if there is an enemy on my position
        if ( graph.getPosition().hasEnemyTokens(myToken.getTeam()) )
            // if my position is dominated by my team
            if ( myZone != null && myToken.getTeam().equals(myZone.getTeam()) )
                loadStrategy(parryStrategy);
            else
                loadStrategy(recoverZoneOrParryStrategy);
        // if I am in the most precious zone of my team
        if ( getZoneManager().isInMostPreciousZone(graph.getPosition()) )
            // if There is an enemy on a neighboring vertex
            if ( graph.isAdjacentToEnemyAgent(graph.getPosition(),
                    myToken.getTeam()) )
                loadStrategy(stabilizeZoneStrategy);
            else {
                loadStrategy(inMainZoneStrategy);
                // scale stability value
                inMainZoneStrategy.zoneStabilityPref = 2 - myZone.getStabilityValue() / 10;
            }
        else
            loadStrategy(gotoMainZoneStrategy);
    }

    @Override
    public Action generateAchievementAction ( String focus ) {
        if ( focus.equals("achievementsSurveyedEdges") ) {
            if ( graph.getPosition().hasUnsurveyedEdges() )
                return new Action("survey");
        }
        else if ( focus.equals("achievementsSuccessfulParries") ) {
            return new Action("parry");
        }
        else if ( focus.equals("achievementsAreaValue") ) {
            if ( mostPreciousZone == myZone )
                return ag.expandZoneByValue(myZone);
        }
        return null;
    }

    @Override
    public Action generateBuyAction ( String focus ) {
        return new Action("buy", new Identifier(
                Character.toLowerCase(focus.charAt(3)) + focus.substring(4)));
    }

    @Override
    public Action generateZoneAction ( String focus ) {
        if ( focus.equals("zoneExpand") )
            return ag.expandZoneByValue(myZone);
        else if ( focus.equals("zoneStability") )
            return ag.stabilizeZone(myZone);
        else if ( focus.equals("zoneDrawback") )
            return ag.stabilizeZone(myZone);
        else if ( focus.equals("zoneMainZone") ) {
            if ( mostPreciousZone != null && myZone != mostPreciousZone )
                return ag.moveTowardsNearest(mostPreciousZone.getCriticalFrontier());
            else
                return ag.buildZone();
        }
        return null;
    }

    @Override
    public Action generateOffensiveAction ( String focus ) {
        if ( focus.equals("offensiveDestroyZones") )
            return ag.destroyZone();
        else if ( focus.equals("offensiveDrawback") )
            return null;
        return null;
    }

    @Override
    public Action generateDefensiveAction ( String focus ) {
        if ( focus.equals("defensiveParry") )
            return new Action("parry");
        else if ( focus.equals("defensiveRunaway") )
            return ag.moveTowardsAgent("Repairer");
        ;
        return null;
    }

}
