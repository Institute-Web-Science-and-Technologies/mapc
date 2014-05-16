package mas.agentsHempelsSofa.data;

import java.util.LinkedList;

import apltk.interpreter.data.LogicBelief;
import mas.agentsHempelsSofa.data.graph.Vertex;

/**
 * An implementation of an agent token which includes the following information
 * about an agent:
 * <ul>
 * <li>the name</li>
 * <li>the team</li>
 * <li>the position</li>
 * <li>the role</li>
 * <li>the strategy</li>
 * <li>the energy</li>
 * <li>the maximum energy</li>
 * <li>the maximum energy if disabled</li>
 * <li>the health</li>
 * <li>the maximum health</li>
 * <li>the strength</li>
 * <li>the visibility range</li>
 * <li>the target vertex</li>
 * <li>the target agent</li>
 * <li>the step it was lastly inspected</li>
 * </ul>
 * It can be placed and moved on the
 * {@link mas.agentsHempelsSofa.data.graph.Graph}.
 * @author Hempels-Sofa
 */
public class AgentToken implements Believable, Comparable<AgentToken> {

    /**
     * Says that the agent takes part in building a zone.
     */
    public static final int STRATEGY_ZONE_BUILDER = 0;
    /**
     * Says that the agent takes part in an attack.
     */
    public static final int STRATEGY_ATTACKER = 1;
    /**
     * Says that the agent takes part in an exploration.
     */
    public static final int STRATEGY_EXPLORER = 2;
    /**
     * The state is unknown (enemy tokens).
     */
    public static final int STRATEGY_UNKNOWN = 3;
    /**
     * A String representing the name of the agent
     */
    private String name;
    /**
     * A String representing the Team of the agent, which is represented by the
     * token
     */
    private String team;
    /**
     * The current position of the token
     */
    private Vertex position;
    /**
     * the state of the agent, which is represented by the token
     */
    private String state;
    /**
     * the last state of the agent, which is represented by the token
     */
    private String laststate;
    /**
     * The Role of the agent, which is represented by the token. If it is not
     * known, it should be set to {@code null}.
     */
    private String role;
    /**
     * Tells which strategy type the agent uses. Possible types are
     * <ul>
     * <li>{@link #STRATEGY_ZONE_BUILDER},</li>
     * <li>{@link #STRATEGY_ATTACKER},</li>
     * <li>{@link #STRATEGY_EXPLORER}.</li>
     * <li>{@link #STRATEGY_UNKNOWN}
     * </ul>
     */
    private int strategyType;
    /**
     * The energy of the agent, which is represented by the token
     */
    private int energy;
    /**
     * The maximum energy of the agent, which is represented by the token
     */
    private int maxEnergy;
    /**
     * The maximum energy of the agent, which is represented by the token, if it
     * is disabled
     */
    private int maxEnergyDisabled;
    /**
     * The health of the agent, which is represented by the token
     */
    private int health;
    /**
     * The maximum health of the agent, which is represented by the token
     */
    private int maxHealth;
    /**
     * The strength of the agent, which is represented by the token
     */
    private int strength;
    /**
     * The visibilityRange of the agent, which is represented by the token
     */
    private int visibilityRange;
    /**
     * The target vertex.
     */
    private Vertex targetVertex;
    /**
     * The Vertex the agent is about to move to
     */
    private Vertex nextPosition;
    /**
     * The target agent.
     */
    private AgentToken targetAgent;
    /**
     * The step of the last inspection.
     */
    private int lastInspection;
    /**
     * The step of the last update.
     */
    private int lastUpdate;

    /**
     * The constructor for an inspection.
     * @param name the name of the agent
     * @param team the team of the agent
     * @param position the position of the agent
     * @param state the current state of the agent
     * @param role the role of the agent
     * @param energy the current energy of the agent
     * @param health the current health of the agent
     * @param strength the strength of the agent
     * @param visibilityRange the visibility range of the agent
     * @param step the number of the actually executed step
     */
    public AgentToken ( String name, String team, Vertex position,
            String state, String role, int energy, int health, int strength,
            int visibilityRange, int step ) {
        this(name, team, position, null, state, role, energy, -1, -1, health,
                -1, strength, visibilityRange, step);
    }

    /**
     * The complete constructor (for agents of same team).
     * @param name the name of the agent
     * @param team the team of the agent
     * @param position the position of the agent
     * @param state the current state of the agent
     * @param role the role of the agent
     * @param energy the current energy of the agent
     * @param maxEnergy the maximum energy of the agent
     * @param maxEnergyDisabled the maximum energy of the agent if it is
     *        disabled
     * @param health the current health of the agent
     * @param maxHealth the maximum health of the agent
     * @param strength the strength of the agent
     * @param visibilityRange the visibility range of the agent
     * @param step the number of the actually executed step
     */
    public AgentToken ( String name, String team, Vertex position,
            Vertex targetVertex, String state, String role, int energy,
            int maxEnergy, int maxEnergyDisabled, int health, int maxHealth,
            int strength, int visibilityRange, int step ) {
        this(name, team, position, targetVertex, state, role, energy,
                maxEnergy, maxEnergyDisabled, health, maxHealth, strength,
                visibilityRange, step, -1);
        if ( energy != -1 )
            this.setLastInspection(step);
    }

    /**
     * The complete constructor (for agents of same team).
     * @param name the name of the agent
     * @param team the team of the agent
     * @param position the position of the agent
     * @param state the current state of the agent
     * @param role the role of the agent
     * @param energy the current energy of the agent
     * @param maxEnergy the maximum energy of the agent
     * @param maxEnergyDisabled the maximum energy of the agent if it is
     *        disabled
     * @param health the current health of the agent
     * @param maxHealth the maximum health of the agent
     * @param strength the strength of the agent
     * @param visibilityRange the visibility range of the agent
     * @param lastUpdate the step of the last update
     * @param lastInspection the step of the last inspection
     */
    public AgentToken ( String name, String team, Vertex position,
            Vertex targetVertex, String state, String role, int energy,
            int maxEnergy, int maxEnergyDisabled, int health, int maxHealth,
            int strength, int visibilityRange, int lastUpdate,
            int lastInspection ) {
        this.setName(name);
        this.setTeam(team);
        this.setPosition(position);
        this.setTargetVertex(targetVertex);
        this.setState(state);
        this.setRole(role);
        this.setEnergy(energy);
        this.setMaxEnergy(maxEnergy);
        this.setMaxEnergyDisabled(maxEnergyDisabled);
        this.setHealth(health);
        this.setMaxHealth(maxHealth);
        this.setStrength(strength);
        this.setVisibilityRange(visibilityRange);
        this.setLastUpdate(lastUpdate);
        this.setLastInspection(lastInspection);
    }

    /**
     * Sets the name of an agent token
     * @param name The new name of the agent
     */
    public void setName ( String name ) {
        this.name = name;
    }

    /**
     * Constructor which sets {@code name}, {@code team}, {@code position} and
     * {@code state}. The other values are set to {@code null} or {@code -1}.
     * @param name the name of the agent
     * @param team the team of the agent
     * @param position the Position of the agent
     * @param state the state of the agent
     * @param step the number of the actually executed step
     */
    public AgentToken ( String name, String team, Vertex position,
            String state, int step ) {
        this(name, team, position, state, null, -1, -1, -1, -1, step);
    }

    public void update ( Vertex position, String state, int step ) {
        this.position = position;
        this.state = state;
        this.setLastUpdate(step);
    }

    public void inspection ( String role, int energy, int health, int strength,
            int visibilityRange, int step ) {
        this.role = role;
        this.energy = energy;
        this.health = health;
        this.strength = strength;
        this.visibilityRange = visibilityRange;
        this.lastInspection = step;
    }

    @Override
    public boolean equals ( Object obj ) {
        if ( obj instanceof AgentToken ) {
            AgentToken a = (AgentToken) obj;
            if ( this.name.equals(a.name) && this.team.equals(a.team) )
                return true;
        }
        return false;
    }

    @Override
    public String toString () {
        return "name:              " + name + "\nteam:              " + team
                + "\nposition:          " + position + "\nstate:             "
                + state + "\nrole:              " + role
                + "\nenergy:            " + energy + "\nmaxEnergy:         "
                + maxEnergy + "\nmaxEnergyDisabled: " + maxEnergyDisabled
                + "\nhealth:            " + health + "\nmaxHealth:         "
                + maxHealth + "\nstrength:          " + strength
                + "\nvisibilityRange:   " + visibilityRange
                + "\nlastInspected:	 " + lastInspection;
    }

    /**
     * @return the name of the agent
     */
    public String getName () {
        return this.name;
    }

    /**
     * @param role the role to set
     */
    public void setRole ( String role ) {
        this.role = role;
    }

    /**
     * @return the role
     */
    public String getRole () {
        return role;
    }

    /**
     * @param team the team to set
     */
    public void setTeam ( String team ) {
        this.team = team;
    }

    /**
     * @return the team
     */
    public String getTeam () {
        return team;
    }

    /**
     * @param position the position to set
     */
    public void setPosition ( Vertex position ) {
        this.position = position;
    }

    /**
     * @return the position
     */
    public Vertex getPosition () {
        return position;
    }

    /**
     * @param state the state to set
     */
    public void setState ( String state ) {
        this.state = state;
    }

    /**
     * @return the state
     */
    public String getState () {
        return state;
    }

    /**
     * @param energy the energy to set
     */
    public void setEnergy ( int energy ) {
        this.energy = energy;
    }

    /**
     * @return the energy
     */
    public int getEnergy () {
        return energy;
    }

    /**
     * @param maxEnergy the maxEnergy to set
     */
    public void setMaxEnergy ( int maxEnergy ) {
        this.maxEnergy = maxEnergy;
    }

    /**
     * @return the maxEnergy
     */
    public int getMaxEnergy () {
        return maxEnergy;
    }

    /**
     * @param maxEnergyDisabled the maxEnergyDisabled to set
     */
    public void setMaxEnergyDisabled ( int maxEnergyDisabled ) {
        this.maxEnergyDisabled = maxEnergyDisabled;
    }

    /**
     * @return the maxEnergyDisabled
     */
    public int getMaxEnergyDisabled () {
        return maxEnergyDisabled;
    }

    /**
     * @param health the health to set
     */
    public void setHealth ( int health ) {
        this.health = health;
    }

    /**
     * @return the health
     */
    public int getHealth () {
        return health;
    }

    /**
     * @param maxHealth the maxHealth to set
     */
    public void setMaxHealth ( int maxHealth ) {
        this.maxHealth = maxHealth;
    }

    /**
     * @return the maxHealth
     */
    public int getMaxHealth () {
        return maxHealth;
    }

    /**
     * @param strength the strength to set
     */
    public void setStrength ( int strength ) {
        this.strength = strength;
    }

    /**
     * @return the strength
     */
    public int getStrength () {
        return strength;
    }

    /**
     * @param visibilityRange the visibilityRange to set
     */
    public void setVisibilityRange ( int visibilityRange ) {
        this.visibilityRange = visibilityRange;
    }

    /**
     * @return the visibilityRange
     */
    public int getVisibilityRange () {
        return visibilityRange;
    }

    /**
     * @param lastInspected the lastInspected to set
     */
    public void setLastInspection ( int lastInspected ) {
        this.lastInspection = lastInspected;
    }

    /**
     * @return the lastInspected
     */
    public int getLastInspection () {
        return lastInspection;
    }

    /**
     * @param targetVertex the tar))getVertex to set
     */
    public void setTargetVertex ( Vertex targetVertex ) {
        this.targetVertex = targetVertex;
    }

    /**
     * @return the targetVertex
     */
    public Vertex getTargetVertex () {
        return targetVertex;
    }

    /**
     * @param lastUpdate the lastUpdate to set
     */
    public void setLastUpdate ( int lastUpdate ) {
        this.lastUpdate = lastUpdate;
    }

    /**
     * @return the lastUpdate
     */
    public int getLastUpdate () {
        return lastUpdate;
    }

    /**
     * @param team the team which shall be checked
     * @return <ul>
     *         <li>{@code true} - if the agent is in the team,</li>
     *         <li>{@code false} - otherwise.</li>
     *         </ul>
     */
    public boolean isOfTeam ( String team ) {
        return team == this.team;
    }

    /**
     * @return a logic belief which contains all information of the agent token.
     */
    @Override
    public LogicBelief toBelief () {
        LinkedList<String> parameters = new LinkedList<String>();
        parameters.addLast(name);
        parameters.addLast(team);
        parameters.addLast(position.getName());
        if ( targetVertex != null )
            parameters.addLast(targetVertex.getName());
        else
            parameters.addLast("null");
        parameters.addLast(state);
        parameters.addLast(role);
        parameters.addLast(Integer.toString(strategyType));
        parameters.addLast(Integer.toString(energy));
        parameters.addLast(Integer.toString(maxEnergy));
        parameters.addLast(Integer.toString(maxEnergyDisabled));
        parameters.addLast(Integer.toString(health));
        parameters.addLast(Integer.toString(maxHealth));
        parameters.addLast(Integer.toString(strength));
        parameters.addLast(Integer.toString(visibilityRange));
        parameters.addLast(Integer.toString(lastInspection));
        parameters.addLast(Integer.toString(lastUpdate));
        return new LogicBelief("agent", parameters);
    }

    /**
     * @param targetAgent the targetAgent to set
     */
    public void setTargetAgent ( AgentToken targetAgent ) {
        this.targetAgent = targetAgent;
    }

    /**
     * @return the targetAgent
     */
    public AgentToken getTargetAgent () {
        return targetAgent;
    }

    /**
     * @param nextPosition the nextPosition to set
     */
    public void setNextPosition ( Vertex nextPosition ) {
        this.nextPosition = nextPosition;
    }

    /**
     * @return the nextPosition
     */
    public Vertex getNextPosition () {
        return nextPosition;
    }

    public boolean isDisabled () {
        return state.equals("disabled");
    }

    @Override
    public int compareTo ( AgentToken t ) {
        int id1 = Integer.parseInt(name.replaceAll("[^0-9]", ""));
        int id2 = Integer.parseInt(name.replaceAll("[^0-9]", ""));
        if ( id1 < id2 )
            return -1;
        if ( id2 < id1 )
            return 1;
        return 0;
    }

    /**
     * @param laststate the laststate to set
     */
    public void setLaststate ( String laststate ) {
        this.laststate = laststate;
    }

    /**
     * @return the laststate
     */
    public String getLaststate () {
        return laststate;
    }

    public boolean stateChanged () {
        if ( state.equals(this.laststate) )
            return false;
        else
            return true;
    }

    /**
     * @param strategyType the zoneBuildingState to set
     */
    public void setStrategyType ( int strategyType ) {
        this.strategyType = strategyType;
    }

    /**
     * @return the strategyType
     */
    public int getStrategyType () {
        return strategyType;
    }

    public boolean isZoneBuilder () {
        return strategyType == STRATEGY_ZONE_BUILDER;
    }

    public boolean isAttacker () {
        return strategyType == STRATEGY_ATTACKER;
    }

    public boolean isStrategyKnown () {
        return strategyType != STRATEGY_UNKNOWN;
    }

    /**
     * @return the String representation of the strategy type.
     */
    public String getStrategyTypeString () {
        switch ( getStrategyType() ) {
        case AgentToken.STRATEGY_ATTACKER:
            return "Attacker";
        case AgentToken.STRATEGY_EXPLORER:
            return "Explorer";
        case AgentToken.STRATEGY_UNKNOWN:
            return "Unknown";
        case AgentToken.STRATEGY_ZONE_BUILDER:
            return "Zone-Builder";
        }
        return null;
    }

}
