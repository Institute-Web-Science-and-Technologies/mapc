package mas.agentsHempelsSofa;

import java.util.LinkedList;

import apltk.interpreter.data.LogicBelief;
import apltk.interpreter.data.Message;
import mas.agentsHempelsSofa.data.AgentToken;
import mas.agentsHempelsSofa.data.Strategy;
import mas.agentsHempelsSofa.data.graph.Vertex;
import mas.agentsHempelsSofa.data.zone.Zone;
import eis.iilang.Action;
import eis.iilang.Identifier;

public class StrategyBasedSaboteurAgent extends StrategyBasedAgent {
    /**
     * Holds all enemy tokens, which are operative
     */
    private LinkedList<AgentToken> enemies;
    /**
     * Holds the positions of all operative enemy tokens
     */
    private LinkedList<Vertex> enemiesPosition;
    /**
     * Holds all the enemy agents, which are on the position of the saboteur
     */
    LinkedList<AgentToken> reachableEnemies;
    /**
     * Represents the tactic for building a Zone
     */
    private static final int TACTIC_BuildZone = 0;
    /**
     * Represents the tactic for destroying an opponent's Zone
     */
    private static final int TACTIC_DestroyZone = 1;
    /**
     * Represents the tactics for destroying an opponent
     */
    private static final int TACTIC_DestroyAgent = 2;
    /**
     * Represents the maximum amount of steps, which can be taken to follow a
     * strategy
     */
    private static final int NUMBER_OF_STEPS = 5;
    /**
     * Holds all the Steps
     */
    private int Number_of_Steps = NUMBER_OF_STEPS;
    /**
     * Counts the number of successful attacks for all saboteur agents
     */
    private int Number_of_Successfull_Attacks;
    /**
     * A strategy, that represents the behavior of an attacking saboteur agent
     */
    private Strategy attacker_Strategy = new Strategy();
    /**
     * A strategy, that represents the behavior of a defending saboteur agent
     */
    private Strategy defender_Strategy = new Strategy();
    /**
     * A strategy, that represents the behavior of a zone building saboteur
     * agent
     */
    private Strategy zone_Strategy = new Strategy();
    /**
     * A strategy, that represents the behavior of an offensive moving saboteur
     * agent
     */
    private Strategy offensive_Strategy = new Strategy();
    /**
     * Tells whether the agent is the defending saboteur or not
     */
    private boolean defender;
    /**
     * Tells whether the agent is the attacking saboteur or not
     */
    private boolean attacker;
    /**
     * Holds all enemies, that entered the zone - the defending sabotuer uses
     * this
     */
    private LinkedList<String> enemiesInZone = new LinkedList<String>();

    /**
     * Default Constructor
     * @param name - The Name of the Agent
     * @param team - The Team of the Agent
     */
    public StrategyBasedSaboteurAgent ( String name, String team ) {
        super(name, team);
        enemies = new LinkedList<AgentToken>();
        enemiesPosition = new LinkedList<Vertex>();
        Number_of_Successfull_Attacks = 0;
        createStrategy();
    }

    /**
     * Generates an action, which is meant to contribute to the achievements
     * Besides attacking, which is done in generateOffensiveAction(), the
     * Saboteur Agent can Survey or contribute to the zone-value
     */
    @Override
    public Action generateAchievementAction ( String focus ) {
        myToken.setStrategyType(AgentToken.STRATEGY_UNKNOWN);
        // Check, if there are unsurveyed edges
        if ( focus.equals("achievementsSurveyedEdges") ) {
            if ( getGraph().getPosition().hasUnsurveyedEdges() )
                return new Action("survey");
            else
                return generateZoneAction("expand");
        }
        else {
            if ( getZoneManager().getValue(getGraph().getPosition()) < getStrategy().achievementsAreaValueGoal ) {
                return generateZoneAction("expand");
            }
            else {
                return generateZoneAction("stability");
            }
        }
    }

    /**
     * Generates a buy-Action, which purchases things for the agents according
     * to the focus
     */
    @Override
    public Action generateBuyAction ( String focus ) {
        myToken.setStrategyType(AgentToken.STRATEGY_UNKNOWN);
        return new Action("buy", new Identifier(Character.toLowerCase(focus
                .charAt(3))
                + focus.substring(4)));
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
     * Generates an offensive oriented move. The agent will check, if there are
     * agents on his actual position which he can attack. If there are no
     * agents, he will seek to destroy an enemy zone or an enemy agent,
     * according to the focus that was received
     */
    @Override
    public Action generateOffensiveAction ( String focus ) {
        // Act according to focus
        myToken.setStrategyType(AgentToken.STRATEGY_ATTACKER);
        if ( focus.equals("offensiveDestroyZone") ) {
            tactics = TACTIC_DestroyZone;
            reviseTactics();
            if ( getZoneManager().getMostPreciousEnemyZone() != null )
                return ag.moveTowardsNearest(getZoneManager()
                        .getMostPreciousEnemyZone().getVertices());
            else
                return generateZoneAction("expand");
        }
        else if ( focus.equals("offensiveDestroyAgents") ) {
            // Both agents need to hit enemies, which are on their positions
            if ( enemiesOnMyPosition() ) {
                for ( AgentToken a : reachableEnemies ) {
                    // If there is a Repairer, strike him first
                    if ( a.getRole() != null && a.getRole().equals("Repairer") ) {
                        tactics = -1;
                        return new Action("attack", new Identifier(a.getName()));
                    }
                }
                for ( AgentToken a : reachableEnemies ) {
                    tactics = -1;
                    return new Action("attack", new Identifier(a.getName()));
                }
            }
            // The attacker needs to move forwards to a random enemy position
            if ( attacker ) {
                tactics = TACTIC_DestroyAgent;
                reviseTactics();
                // Move towards repairers first
                if ( !repairerPositions().isEmpty() )
                    return ag.moveTowardsNearest(repairerPositions());
                else
                    return ag.moveTowardsNearest(enemiesPosition);
            }
            else {
                // The defender only needs to move towards an agent, if there is
                // one in the zone
                if ( !enemiesInZone.isEmpty() )
                    tactics = TACTIC_DestroyAgent;
                LinkedList<Vertex> positions = new LinkedList<Vertex>();
                for ( AgentToken a : getGraph().getEnemyTokens(
                        myToken.getTeam()) ) {
                    if ( enemiesInZone.contains(a.getName()) )
                        positions.add(a.getPosition());
                }
                return ag.moveTowardsNearest(positions);
            }
        }
        else {
            // Retreat
            Zone zone = getZoneManager().getMostPreciousZone();
            if ( zone == null )
                return ag.expandOwnComponent();
            return ag.moveTowardsNearest(zone.getCriticalFrontier());
        }
    }

    /**
     * Generates an defensive oriented action. The only time this is used, is
     * when the agent itself is disabled
     */
    @Override
    public Action generateDefensiveAction ( String focus ) {
        myToken.setStrategyType(AgentToken.STRATEGY_UNKNOWN);
        if ( focus.equals("defensiveRepair") ) {
            return ag.moveTowardsAgent("Repairer");
        }
        return new Action("parry");
    }

    /**
     * Handles the agent specific messages, which includes the messages from the
     * other saboteur about a successful attack or a message for the defending
     * saboteur agent in the zone
     */
    @Override
    public void handleAgentSpecificMessages () {
        for ( Message msg : messages ) {
            LogicBelief belief = (LogicBelief) msg.value;
            if ( belief.getPredicate().equals("newSuccessfullAttack")
                    && !msg.sender.equals(myToken.getName()) ) {
                this.Number_of_Successfull_Attacks++;
                // System.out.println("The other attacker landed a successful attack");
            }
            if ( belief.getPredicate().equals("iAmAttacker") ) {
                this.attacker = false;
                this.defender = true;
            }
            if ( belief.getPredicate().equals("zoneAlert") ) {
                enemiesInZone.add(belief.getParameters().get(0));
            }
        }
    }

    /**
     * Revises the preferences of the agent. First checks, if he has been
     * attacked, then if he can attack and then, if there are tactics to follow
     */
    @Override
    public void preferencesRevision () {

        if ( !attacker && !defender ) {
            this.attacker = true;
            this.broadcastBelief(new LogicBelief("iAmAttacker"));
            myToken.setStrategyType(AgentToken.STRATEGY_ATTACKER); // C: richtig
            // gesetzt
            // hier?
        }
        // Revise the number of successful attacks
        if ( getEnvironment().isLastActionSuccessful()
                && getEnvironment().getLastAction().equals("attack") ) {
            Number_of_Successfull_Attacks++;
            this.broadcastBelief(new LogicBelief("newSuccessfullAttack"));
            // System.out.println("A new successful attack has occured "+getEnvironment().getLastAction());
        }
        // check, if there are enemies on my position
        findEnemies();
        // If the agent himself is hurt, move towards a Repairer Agent
        if ( myToken.isDisabled() ) {
            getStrategy().defensivePref = 2.0;
            getStrategy().defensiveRepairPref = 2.0;
            return;
        }
        if ( attacker ) {
            if ( enemiesOnMyPosition()
                    && (getStrategy().achievementsSuccessfulAttacksGoal > Number_of_Successfull_Attacks) ) {
                loadStrategy(offensive_Strategy);
                return;
            }
            else if ( tactics == TACTIC_BuildZone && !enemiesOnMyPosition() ) {
                loadStrategy(zone_Strategy);
            }
            else if ( getZoneManager().getBiggestEnemyZone() != null ) {
                if ( tactics == TACTIC_DestroyZone && !enemiesOnMyPosition() ) {
                    getStrategy().offensivePref = 1.0;
                    getStrategy().offensiveDestroyZonesPref = 1.0;
                }
            }
            else {
                loadStrategy(attacker_Strategy);
            }
            return;

        }
        if ( defender ) {
            if ( !enemiesInZone.isEmpty() ) {
                loadStrategy(offensive_Strategy);
                return;
            }
            else if ( tactics == TACTIC_BuildZone && !enemiesOnMyPosition() ) {
                loadStrategy(zone_Strategy);
            }
            else if ( getZoneManager().getBiggestEnemyZone() != null ) {
                if ( tactics == TACTIC_DestroyZone && !enemiesOnMyPosition() ) {
                    getStrategy().offensivePref = 1.0;
                    getStrategy().offensiveDestroyZonesPref = 1.0;
                }
            }
            else {
                loadStrategy(defender_Strategy);
            }
            return;
        }

    }

    /**
     * Finds enemies all over the map and puts them in a List, if they are not
     * already disabled
     */
    private void findEnemies () {
        enemies = new LinkedList<AgentToken>();
        enemiesPosition = new LinkedList<Vertex>();
        // Find agents all over the graph
        for ( AgentToken a : getGraph().getTokens() )
            if ( (!a.getTeam().equals(myToken.getTeam())) && (!a.isDisabled()) ) {
                enemies.add(a);
                enemiesPosition.add(a.getPosition());
            }
        reachableEnemies = new LinkedList<AgentToken>();
        // find enemies on my position
        for ( AgentToken a : getGraph().getPosition().getTokens() )
            if ( (!a.getTeam().equals(myToken.getTeam())) && (!a.isDisabled()) )
                reachableEnemies.add(a);
    }

    /**
     * @return True, of there are enemies on the agents position
     */
    private boolean enemiesOnMyPosition () {
        return !reachableEnemies.isEmpty();
    }

    /**
     * Finds the positions of the enemy repairer agents to destroy those eben
     * faster
     * @return A LinkedList holding the positions of the enemy repairer agents
     */
    private LinkedList<Vertex> repairerPositions () {
        LinkedList<Vertex> repPos = new LinkedList<Vertex>();
        for ( AgentToken a : enemies )
            if ( a.getRole() != null && a.getRole().equals("Repairer")
                    && !a.isDisabled() )
                repPos.add(a.getPosition());
        return repPos;
    }

    /**
     * Creates the strategies
     */
    private void createStrategy () {
        // Strategy for an attacking saboteur
        attacker_Strategy.offensivePref = 0.7;
        attacker_Strategy.offensiveDrawbackPref = 0.2;
        attacker_Strategy.offensiveDestroyAgentsPref = 0.5;
        attacker_Strategy.offensiveDestroyZonesPref = 0.3;
        attacker_Strategy.zonePref = 0.1;
        attacker_Strategy.achievementsPref = 0.1;
        attacker_Strategy.achievementsSuccessfulAttacksPref = 0.8;
        attacker_Strategy.achievementsSuccessfulParriesPref = 0.1;
        attacker_Strategy.achievementsSurveyedEdgesPref = 0.1;
        attacker_Strategy.buyPref = 0.1;
        attacker_Strategy.buyBatteryPref = 0.1;
        attacker_Strategy.buySabotageDevicePref = 0.7;
        attacker_Strategy.buySensorPref = 0.1;
        attacker_Strategy.buyShieldPref = 0.1;
        // Strategy for an defending saboteur agent
        defender_Strategy.offensivePref = 0.0;
        defender_Strategy.offensiveDrawbackPref = 0.0;
        defender_Strategy.offensiveDestroyAgentsPref = 1.0;
        defender_Strategy.offensiveDestroyZonesPref = 0.0;
        defender_Strategy.zonePref = 0.8;
        defender_Strategy.zoneExpandPref = 0.2;
        defender_Strategy.zoneMainZonePref = 0.5;
        defender_Strategy.zoneStabilityPref = 0.3;
        defender_Strategy.achievementsPref = 0.1;
        defender_Strategy.achievementsAreaValuePref = 0.6;
        defender_Strategy.achievementsSuccessfulAttacksPref = 0.2;
        defender_Strategy.achievementsSuccessfulParriesPref = 0.1;
        defender_Strategy.achievementsSurveyedEdgesPref = 0.1;
        defender_Strategy.buyPref = 0.1;
        defender_Strategy.buyBatteryPref = 0.1;
        defender_Strategy.buySabotageDevicePref = 0.5;
        defender_Strategy.buySensorPref = 0.1;
        defender_Strategy.buyShieldPref = 0.3;
        // Strategy for building a zone
        zone_Strategy.zonePref = 1.0;
        zone_Strategy.zoneExpandPref = 0.4;
        zone_Strategy.zoneStabilityPref = 0.1;
        zone_Strategy.zoneMainZonePref = 0.5;
        // Default offensive strategy for attacking
        offensive_Strategy.offensivePref = 1.0;
        offensive_Strategy.offensiveDestroyAgentsPref = 0.8;
        offensive_Strategy.offensiveDestroyZonesPref = 0.0;
        offensive_Strategy.offensiveDrawbackPref = 0.2;
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
