package mas.agentsHempelsSofa;

import java.util.LinkedList;
import mas.agentsHempelsSofa.data.AgentToken;
import mas.agentsHempelsSofa.data.graph.Vertex;
import eis.iilang.Action;
import eis.iilang.Identifier;

public class StrategyBasedRepairerAgent extends StrategyBasedAgent {
    /**
     * Holds all the damaged agents
     */
    private LinkedList<AgentToken> damagedAgents;
    /**
     * Represents the tactic for building a Zone
     */
    private static final int TACTIC_BuildZone = 0;
    /**
     * Represents the tactic for destroying an opponent's Zone
     */
    private static final int TACTIC_DestroyZone = 1;
    /**
     * Represents the maximum amount of steps, which can be taken to follow a
     * certain tactic
     */
    private static final int NUMBER_OF_STEPS = 5;
    /**
     * Holds all the Steps
     */
    private int Number_of_Steps = NUMBER_OF_STEPS;
    /**
     * The Position of the other reprairer
     */
    private AgentToken repairer;

    /**
     * Default Constructor
     * @param name the name of the agent
     * @param team the team of the agent
     */
    public StrategyBasedRepairerAgent ( String name, String team ) {
        super(name, team);
        damagedAgents = new LinkedList<AgentToken>();
        tactics = -1;
    }

    /**
     * Generates an action, which is meant to contribute to the achievements The
     * possible tributes from a repairer agent are surveying, parrying or
     * improving the zoneValue
     */
    @Override
    public Action generateAchievementAction ( String focus ) {
        myToken.setStrategyType(AgentToken.STRATEGY_UNKNOWN);
        if ( focus == "achievementsSurveyedEdges" ) {
            if ( unsurveyedEdgesNearby() )
                return new Action("survey");
            else
                return generateZoneAction("stability");
        }
        else if ( focus == "achievementsSuccessfulParries" ) {
            return new Action("parry");
        }
        else {
            if ( getZoneManager().getValue(getGraph().getPosition()) < 20 )
                return generateZoneAction("expand");
            else
                return generateZoneAction("stability");
        }
    }

    /**
     * Generates a buy-Action, which purchases things fr the agents according to
     * the focus
     */
    @Override
    public Action generateBuyAction ( String focus ) {
        myToken.setStrategyType(AgentToken.STRATEGY_UNKNOWN);
        return new Action("buy", new Identifier(
                Character.toLowerCase(focus.charAt(3)) + focus.substring(4)));
    }

    /**
     * Generates an Action, which contributes to building a Zone If the focus is
     * received, it will be pursued for NUMBER_OF_STEPS_BuildZone Steps
     */
    @Override
    public Action generateZoneAction ( String focus ) {
        myToken.setStrategyType(AgentToken.STRATEGY_ZONE_BUILDER);
        tactics = TACTIC_BuildZone;
        reviseTactics();
        Action move = ag.buildZone();
        if ( move.getName().equals("goto") )
            myToken.setNextPosition(getGraph().getVertex(
                    move.getParameters().getLast()));
        return move;
    }

    /**
     * Generates an offensive oriented move. If the focus is DestryoZones, the
     * agent will move towards an enemy zone and seek to destroy it or, if the
     * focus is Drawback, he will retreat towards the own main zone
     */
    @Override
    public Action generateOffensiveAction ( String focus ) {
        myToken.setStrategyType(AgentToken.STRATEGY_ATTACKER);
        if ( focus == "offensiveDestroyZone" ) {
            tactics = TACTIC_DestroyZone;
            reviseTactics();
            if ( getZoneManager().getBiggestEnemyZone() != null ) {
                Action move = ag.moveTowardsNearest(getZoneManager().getMostPreciousEnemyZone().getCriticalFrontier());
                if ( move.getName().equals("goto") )
                    myToken.setNextPosition(getGraph().getVertex(
                            move.getParameters().getLast()));
                return move;
            }
        }
        return drawback();
    }

    /**
     * Generates an defensive oriented action. The agent will move towards the
     * other repairer, if he is damaged Then he will seek to repair other
     * damaged agents and move towards them
     */
    @Override
    public Action generateDefensiveAction ( String focus ) {
        myToken.setStrategyType(AgentToken.STRATEGY_UNKNOWN);
        if ( repairer == null )
            return new Action("recharge");
        // if damaged agent on my position, repair him
        if ( focus.equals("defensiveRepair") ) {
            // if damaged agent on my position, repair him
            if ( damagedAgentNearby() ) {
                return new Action("repair", new Identifier(finddamagedAgent()));
            }
            // If disabled, move towards other repairer
            if ( myToken.getHealth() == 0 && repairer.getPosition() != null
                    && (repairer.getPosition() != myToken.getPosition()) ) {
                Action go = ag.moveTowards(repairer.getPosition());
                if ( repairer.getNextPosition() != null ) {
                    if ( repairer.getNextPosition() == myToken.getPosition() )
                        return new Action("recharge");
                    else
                        return go;
                }
            }
            // or move towards another damaged agent
            LinkedList<Vertex> positions = new LinkedList<Vertex>();
            for ( AgentToken a : damagedAgents )
                positions.add(a.getPosition());
            if ( repairer.getNextPosition() != null )
                positions.remove(repairer.getNextPosition());
            if ( repairer.getTargetVertex() != null )
                positions.remove(repairer.getTargetVertex());
            Action move = ag.moveTowardsNearest(positions);
            if ( move.getName().equals("goto") )
                myToken.setNextPosition(getGraph().getVertex(
                        move.getParameters().getLast()));
            return move;
        }
        else if ( focus == "defensiveParry" ) {
            return new Action("parry");
        }
        return drawback();
    }

    /**
     * Handles the agent specific messages. Not used so far.
     */
    @Override
    public void handleAgentSpecificMessages () {
        // not needed so far
    }

    /**
     * Revises the preferences of the agent.
     */
    @Override
    public void preferencesRevision () {
        checkDamagedAgents();
        for ( AgentToken t : graph.getTokens() ) {
            if ( t.getRole() != null && t.getTeam().equals(myToken.getTeam())
                    && t.getRole().equals("Repairer") && !(t.equals(myToken)) )
                repairer = t;
        }
        // setting the general preferences right
        if ( hasDamagedAgents() || (myToken.getHealth() == 0) ) {
            getStrategy().defensivePref = 1.0;
            getStrategy().defensiveRepairPref = 1.0;
            return;
        }
        else {
            if ( tactics == TACTIC_BuildZone ) {
                getStrategy().zonePref = 1.0;
                getStrategy().zoneMainZonePref = 1.0;
                return;
            }
            else if ( tactics == TACTIC_DestroyZone ) {
                getStrategy().offensivePref = 1.0;
                getStrategy().offensiveDestroyZonesPref = 1.0;
                return;
            }
            else {
                if ( enemyNearby() ) {
                    // There are enemies on my position, so create a probability
                    // for defensive actions
                    // C: Repairer kann zu Anfang null sein, da alle ihre role
                    // noch nicht kennen, deshalb
                    // duerfen die defensive actions nicht durchgefuehrt werden.
                    if ( repairer != null )
                        getStrategy().defensivePref = 0.55;
                    else
                        getStrategy().defensivePref = 0.0;
                    getStrategy().defensiveParryPref = 0.8;
                    getStrategy().defensiveRunAwayPref = 0.2;
                    getStrategy().zonePref = 0.35;
                    getStrategy().zoneMainZonePref = 1.0;
                    getStrategy().offensivePref = 0.1;
                    getStrategy().offensiveDestroyZonesPref = 0.5;
                    getStrategy().offensiveDrawbackPref = 0.5;
                }
                else {
                    // no enemies, so no need for defense actions
                    getStrategy().defensivePref = 0.0;
                    getStrategy().zonePref = 0.7;
                    getStrategy().zoneMainZonePref = 1.0;
                    getStrategy().offensivePref = 0.3;
                    getStrategy().offensiveDestroyZonesPref = 0.5;
                    getStrategy().offensiveDrawbackPref = 0.5;
                }
            }
        }

    }

    // ---------------------------------------------------------------------------------------------------------------------------------
    // //
    /**
     * @return True, if I have an agent I have to repair
     */
    private boolean hasDamagedAgents () {
        if ( damagedAgents.isEmpty() )
            return false;
        return true;
    }

    /**
     * Compute a runaway action: Goto a random vertex
     * @return
     */
    private Action drawback () {
        // drawback
        return ag.moveTowardsNearest(getZoneManager().getMostPreciousZone().getCriticalFrontier());
    }

    /**
     * Checks, if there are any agents which need to be repaired
     */
    private void checkDamagedAgents () {
        damagedAgents = new LinkedList<AgentToken>();
        for ( AgentToken a : getGraph().getTokens() )
            if ( a.getTeam().equals(myToken.getTeam()) && !a.equals(myToken) )
                if ( a.getHealth() < a.getMaxHealth() || a.getHealth() == 0
                        || a.isDisabled() ) {
                    damagedAgents.add(a);
                }
    }

    /**
     * @return True, if there is a damaged agent on my position
     */
    private boolean damagedAgentNearby () {
        for ( AgentToken a : damagedAgents )
            if ( a.getPosition().getName().equals(
                    myToken.getPosition().getName()) )
                return true;
        return false;
    }

    /**
     * @return The name of the agent, which needs to be repaired
     */
    private String finddamagedAgent () {
        AgentToken target = null;
        for ( AgentToken a : damagedAgents ) {
            if ( a.getPosition().getName().equals(
                    myToken.getPosition().getName()) ) {
                target = a;
            }
            if ( a.getRole() != null && a.getRole() == "Repairer" ) {
                return a.getName();
            }
        }
        return target.getName();
    }

    /**
     * revises the tactics
     */
    private void reviseTactics () {
        if ( Number_of_Steps > 0 ) {
            Number_of_Steps--;
        }
        else {
            Number_of_Steps = NUMBER_OF_STEPS;
            tactics = -1;
        }
    }

}
