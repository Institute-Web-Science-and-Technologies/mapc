package mas.agentsHempelsSofa;

import java.util.LinkedList;

import mas.agentsHempelsSofa.data.AgentToken;
import mas.agentsHempelsSofa.data.graph.Vertex;
import mas.agentsHempelsSofa.data.zone.Zone;
import eis.iilang.Action;
import eis.iilang.Identifier;

public class StrategyBasedInspectorAgent extends StrategyBasedAgent {

    /**
     * the current position of this agent
     */
    private Vertex myPosition;
    /**
     * the team of this agent
     */
    private String myTeam;
    /**
     * the current step
     */
    private int currentStep;
    /**
     * the number of agents, which have been inspected by the whole team until
     * know
     */
    private int numberOfInspectedAgents;
    /**
     * the threshold, to consider an inspection as "long ago"
     */
    private final int LAST_INSPECTION_THRESHOLD = 13;

    // constants for tactics
    /**
     * the number of steps that have been made with the current tactics
     */
    private int tacticStep;
    private static final int NUMBER_OF_TACTICS = 5;
    /**
     * the number of steps that are suitable for a tactic
     */
    private static int[] numberOfSteps = new int[NUMBER_OF_TACTICS];

    // assign tactic indices
    private static final int TACTIC_none = 0;
    private static final int TACTIC_destroyZone = 1;
    private static final int TACTIC_buildZone = 2;
    private static final int TACTIC_goToRepairer = 3;
    private static final int TACTIC_followLongNotInspected = 4;

    /**
     * constructor for a new agent of the role Inspector
     * @param name the name of the agent
     * @param team the name of the team
     */
    public StrategyBasedInspectorAgent ( String name, String team ) {
        super(name, team);

        // initialize number of inspected agents
        numberOfInspectedAgents = 0;

        // setting the suitable number of steps per tactics
        numberOfSteps[TACTIC_none] = 0;
        numberOfSteps[TACTIC_destroyZone] = 15;
        numberOfSteps[TACTIC_buildZone] = 10;
        numberOfSteps[TACTIC_goToRepairer] = 12345;
        numberOfSteps[TACTIC_followLongNotInspected] = 18;
    }

    @Override
    public Action generateAchievementAction ( String focus ) {
        myToken.setStrategyType(AgentToken.STRATEGY_ZONE_BUILDER);
        // successfulParries is covered by defaultAction
        if ( focus == "achievementsSurveyedEdges" ) {
            if ( myPosition.getNumberOfUnsurveyedEdges() > 0 ) {
                Action ret = new Action("survey", myPosition.getIdentifier());
                return ret;
            }
        }
        if ( focus == "achievementsInspectedAgents" ) {
            if ( longNotInspectedEnemyNear(myPosition) )
                return new Action("inspect", myPosition.getIdentifier());
        }

        if ( focus == "achievementsAreaValue" ) {
            Action ret = ag.buildZone();
            return ret;
        }
        return ag.expandOwnComponent();
    }

    @Override
    public Action generateBuyAction ( String focus ) {
        myToken.setStrategyType(AgentToken.STRATEGY_UNKNOWN);
        Action ret = new Action("buy", new Identifier(Character
                .toLowerCase(focus.charAt(3))
                + focus.substring(4)));
        return ret;
    }

    @Override
    public Action generateZoneAction ( String focus ) {
        tactics = TACTIC_buildZone;

        myToken.setStrategyType(AgentToken.STRATEGY_ZONE_BUILDER);

        if ( focus == "zoneExpand" ) {
            Action ret = ag.buildZone();
            return ret;
        }
        if ( focus == "zoneStability" ) {
            Action ret = ag.buildZone();
            return ret;
        }
        if ( focus == "zoneDrawback" ) {
            Action ret = ag.buildZone();
            return ret;
        }
        Action ret = ag.expandOwnComponent();
        return ret;
    }

    @Override
    public Action generateOffensiveAction ( String focus ) {
        // focus offensiveDestroyAgent not considered

        if ( tactics == TACTIC_followLongNotInspected ) {
        	myToken.setStrategyType(AgentToken.STRATEGY_ATTACKER);
            if ( longNotInspectedEnemyNear(myPosition) )
                return new Action("inspect", myPosition.getIdentifier());

            LinkedList<Vertex> possibleTargets = new LinkedList<Vertex>();
            for ( AgentToken t : graph.getTokens() ) {
                if ( !t.getTeam().equals(myTeam) && notRecentlyInspected(t) )
                    possibleTargets.add(t.getPosition());
            }
            if ( !possibleTargets.isEmpty() )
                return ag.moveTowardsNearest(possibleTargets);
        }

        if ( focus == "offensiveDestroyZone" ) {
        	myToken.setStrategyType(AgentToken.STRATEGY_ATTACKER);
            tactics = TACTIC_destroyZone;
            Action ret = ag.destroyZone();
            return ret;
        }
        if ( focus == "offensiveDrawback" ) {
        	myToken.setStrategyType(AgentToken.STRATEGY_UNKNOWN);
            Zone zone = getZoneManager().getMostPreciousZone();
            if ( zone == null )
                return ag.expandOwnComponent();
            Action ret = ag.moveTowardsNearest(zone.getCriticalFrontier());
            return ret;
        }
        Action ret = ag.expandOwnComponent();
        return ret;
    }

    @Override
    public Action generateDefensiveAction ( String focus ) {
        myToken.setStrategyType(AgentToken.STRATEGY_UNKNOWN);
        if ( focus == "defensiveRepair" ) {
            LinkedList<Vertex> repairerPosition = new LinkedList<Vertex>();
            for ( AgentToken t : graph.getTokens() ) {

                if ( t.getRole() != null && t.getTeam().equals(myTeam)
                        && t.getRole().equals("Repairer") ) {
                    if ( t.getNextPosition() == null )
                        return new Action("recharge");
                    if ( t.getNextPosition().equals(myPosition) )
                        return new Action("recharge");
                    repairerPosition.add(t.getPosition());
                }
            }
            return ag.moveTowardsNearest(repairerPosition);
        }

        if ( focus == "defensiveParry" ) {
            return new Action("parry");
        }
        // standard action & focus runaway
        Zone zone = getZoneManager().getBiggestZone();
        if ( zone != null )
            return ag.moveTowardsNearest(zone.getCriticalFrontier());
        return new Action("recharge");
    }

    @Override
    public void handleAgentSpecificMessages () {
    }

    @Override
    public void preferencesRevision () {

        update();

        // countInspectedAgents();

        tacticsRevision();

        // check tactics
        switch ( tactics ) {
        case TACTIC_none:
            setStrategy(getDefaultStrategy());

            // check for enemys on own position
            if ( enemyUnknownAgentAt(myPosition) ) {
                getStrategy().achievementsPref = 1.0;
                getStrategy().achievementsInspectedAgentsPref = 1.0;
            }

            if ( enemySaboteurAt(myPosition) ) {
                getStrategy().buyPref = 0;
                getStrategy().defensivePref = 2.0;
                getStrategy().defensiveParryPref = 0.7;
                getStrategy().defensiveRunAwayPref = 0.3;
                break;

            }
            // no enemy at my position
            getStrategy().defensivePref = 0.0;

            if ( longNotInspectedEnemyNear(myPosition) ) {
                getStrategy().achievementsPref = 2;
                getStrategy().achievementsInspectedAgentsPref = 2;
            }

            break;
        case TACTIC_destroyZone:
            getStrategy().offensivePref = 1;
            getStrategy().offensiveDestroyZonesPref = 1;
            getStrategy().defensivePref = 0;
            break;
        case TACTIC_buildZone:
            getStrategy().achievementsPref = 1;
            getStrategy().achievementsAreaValuePref = 1;
            getStrategy().defensivePref = 0;
            getStrategy().offensivePref = 0;
            break;
        case TACTIC_goToRepairer:
            getStrategy().defensivePref = 2;
            getStrategy().defensiveRepairPref = 2;
            getStrategy().offensivePref = 0;
            break;
        case TACTIC_followLongNotInspected:
            getStrategy().defensivePref = 0;
            getStrategy().offensivePref = 1;
            getStrategy().zonePref = 0;
            break;
        default:
            setStrategy(getDefaultStrategy());
            break;
        }
    }

    /**
     * holds old tactics, if number of steps is not reached resets tactic if
     * threshold reached
     */
    private void tacticsRevision () {
        tacticStep++;

        // check for each tactics whether numberOfSteps is reached
        for ( int t = 0; t < NUMBER_OF_TACTICS; t++ ) {
            if ( tacticStep > numberOfSteps[t] && tactics == t ) {
                tacticStep = 0;
                tactics = TACTIC_none;
            }
        }

        // if disabled go to repairer
        if ( myToken.isDisabled() ) {
            tactics = TACTIC_goToRepairer;
            return;
        }

        // if repaired, stop tactic
        if ( tactics == TACTIC_goToRepairer && myToken.getHealth() > 0 )
            tactics = TACTIC_none;

        // check if goals reached
        if ( numberOfInspectedAgents < getStrategy().achievementsInspectedAgentsGoal ) {
            tactics = TACTIC_followLongNotInspected;
            return;
        }

        // search never inspected enemy
        if ( longNotInspectedEnemyNear(myPosition) ) {
            tactics = TACTIC_followLongNotInspected;
            return;
        }

    }

    /**
     * @param token related agent token
     * @return true, if token has not been inspected since
     *         LAST_INSPECTION_THRESHOLD steps, else false
     */
    private boolean notRecentlyInspected ( AgentToken token ) {
        return (currentStep - token.getLastInspection() > LAST_INSPECTION_THRESHOLD);
    }

    /**
     * updates locally used variables
     */
    private void update () {
        myPosition = graph.getPosition();
        myTeam = getMyToken().getTeam();
        // get current step count
        currentStep = environment.getStep();
        numberOfInspectedAgents = environment.getNumberOfInspectedAgents();
    }

    /**
     * checks for long not inspected enemy at a position and adjacent vertices
     * @param v the vertex to check
     * @return true, if there is at least one long not inspected enemy, else
     *         false
     * @see {@link #notRecentlyInspected(AgentToken)}
     */
    private boolean longNotInspectedEnemyNear ( Vertex v ) {
        // check for uninspected enemy on own position and adjacent vertices
        LinkedList<AgentToken> agentsToCheck = new LinkedList<AgentToken>();
        agentsToCheck.addAll(v.getTokens());
        for ( Vertex w : v.getAdjacentVertices() )
            agentsToCheck.addAll(w.getTokens());

        for ( AgentToken t : agentsToCheck ) {
            if ( !t.getTeam().equals(myTeam) && notRecentlyInspected(t) )
                return true;

        }
        return false;
    }
}