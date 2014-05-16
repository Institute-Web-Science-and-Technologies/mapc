package mas.agentsHempelsSofa.data.zone;

import java.util.LinkedList;

import apltk.interpreter.data.LogicBelief;

import mas.agentsHempelsSofa.algorithms.GeneralAlgorithms;
import mas.agentsHempelsSofa.data.AgentToken;
import mas.agentsHempelsSofa.data.Believable;
import mas.agentsHempelsSofa.data.graph.Vertex;

/**
 * An implementation of a zone. It provides the following information:
 * <ul>
 * <li>the vertices in the zone</li>
 * <li>the critical frontier vertices</li>
 * <li>the agent tokens in the zone</li>
 * <li>the agent tokens on critical frontier vertices</li>
 * <li>a percepted value</li>
 * <li>the stability value</li>
 * @author Hempels-Sofa
 */
public class Zone implements Believable {

    /* -------------------------- Fields ------------------------- */

    /**
     * determines the significance of inner tokens in the stability value.
     */
    public static double stability_inner = 0.0;
    /**
     * determines the significance of critical frontier tokens in the stability
     * value.
     */
    public static double stability_front = 1.0;
    /**
     * the vertices of the zone.
     */
    private LinkedList<Vertex> vertices;
    /**
     * the tokens in the zone.
     */
    private LinkedList<AgentToken> tokens;
    /**
     * the team tokens in the zone.
     */
    private LinkedList<AgentToken> teamTokens;
    /**
     * the enemy tokens in the zone. These can only be IN the zone, if they are
     * on a vertex which is dominated by the {@code team}.
     */
    private LinkedList<AgentToken> enemyTokens;
    /**
     * the agent tokens on the critical frontier.
     */
    private LinkedList<AgentToken> criticalFrontierTokens;
    /**
     * the vertices of the critical frontier.
     */
    private LinkedList<Vertex> criticalFrontierVertices;
    /**
     * the isolated vertices of the zone.
     */
    private LinkedList<Vertex> isolatedVertices;
    /**
     * the name of the team which possesses this zone.
     */
    private String team;
    /**
     * the value of this zone which was percepted.
     */
    private int perceptedValue = 0;
    /**
     * the calculated value of the zone.
     */
    private int value;
    /**
     * the stability value.
     */
    private double stabilityValue;

    /* ---------------------- Constructors ----------------------- */

    /**
     * creates an empty zone
     */
    public Zone () {
        vertices = new LinkedList<Vertex>();
        tokens = new LinkedList<AgentToken>();
        teamTokens = new LinkedList<AgentToken>();
        enemyTokens = new LinkedList<AgentToken>();
        criticalFrontierTokens = new LinkedList<AgentToken>();
        criticalFrontierVertices = new LinkedList<Vertex>();
    }

    /* --------------------- Provided Methods -------------------- */

    /**
     * updates the fields values.
     */
    public void update () {
        // clear all
        tokens.clear();
        teamTokens.clear();
        enemyTokens.clear();
        criticalFrontierTokens.clear();
        criticalFrontierVertices.clear();
        value = 0;

        // set all
        for ( Vertex v : vertices ) {
            for ( Vertex w : v.getAdjacentVertices() )
                if ( !vertices.contains(w) ) {
                    criticalFrontierVertices.add(v);
                    break;
                }
            for ( AgentToken t : v.getTokens() ) {
                tokens.add(t);
                if ( t.isOfTeam(team) )
                    teamTokens.add(t);
                else
                    enemyTokens.add(t);
                if ( criticalFrontierVertices.contains(t.getPosition()) )
                    criticalFrontierTokens.add(t);
            }
            value += v.getValue();
        }

        // calculate stabilityValue
        int numberOfInnerAgents = teamTokens.size()
                - criticalFrontierTokens.size();
        int numberOfFrontAgents = criticalFrontierTokens.size();
        stabilityValue = (numberOfInnerAgents * stability_inner + numberOfFrontAgents
                * stability_front)
                / criticalFrontierVertices.size();
    }

    /**
     * A zone is adjacent to another zone if one vertex out of the one zone is
     * adjacent to a vertex out of the other zone.
     * @param z the zone to test.
     * @return <ul>
     *         <li>{@code true} - if the zones are adjacent to each other,</li>
     *         <li>{@code false} - otherwise.</li>
     *         </ul>
     */
    public boolean isAdjacentTo ( Zone z ) {
        for ( Vertex v : z.getVertices() )
            for ( Vertex w : vertices )
                if ( v.isAdjacentTo(w) )
                    return true;
        return false;
    }

    /**
     * unions two zones.
     * @param z the zone to add to this zone.
     */
    public void union ( Zone z ) {
        this.vertices.addAll(z.getVertices());
        z.getVertices().clear();
    }

    @Override
    public boolean equals ( Object obj ) {
        if ( !(obj instanceof Zone) )
            return false;
        Zone z = (Zone) obj;
        return z.vertices.contains(vertices.getFirst());
    }

    @Override
    public String toString () {
        String circ = "non ";
        if ( isCircular() )
            circ = "";
        String s = "zone(t=" + team + ", v=" + getValue() + ", sv="
                + this.getStabilityValue() + ", " + vertices + " " + circ
                + "circular)";
        return s;
    }

    /**
     * Looks up a vertex in this zone.
     * @param vertex the vertex of an agent which shall be looked up.
     * @return <ul>
     *         <li>{@code true} - if the vertex is in this zone,</li>
     *         <li>{@code false} - otherwise.</li>
     *         </ul>
     */
    public boolean contains ( Vertex vertex ) {
        if ( vertex == null )
            return false;
        boolean contains = vertices.contains(vertex);
        if ( !contains )
            for ( Vertex v : vertices )
                if ( v.equals(vertex) )
                    return true;
        return contains;
    }

    /**
     * Looks up an agent token in this zone.
     * @param token the agent token of an agent which shall be looked up.
     * @return <ul>
     *         <li>{@code true} - if the agent is in this zone,</li>
     *         <li>{@code false} - otherwise.</li>
     *         </ul>
     */
    public boolean contains ( AgentToken token ) {
        return tokens.contains(token);
    }

    /**
     * Checks whether a vertex is on the critical frontier of this zone.
     * @param vertex the vertex which shall be checked.
     * @return <ul>
     *         <li>{@code true} - if the agent is on the frontier,</li>
     *         <li>{@code false} - otherwise.</li>
     *         </ul>
     */
    public boolean isOnCriticalFrontier ( Vertex vertex ) {
        return criticalFrontierVertices.contains(vertex);
    }

    /**
     * Checks whether an agent is on the critical frontier of this zone.
     * @param token the agent token which shall be checked.
     * @return <ul>
     *         <li>{@code true} - if the agent is on the frontier,</li>
     *         <li>{@code false} - otherwise.</li>
     *         </ul>
     */
    public boolean isOnCriticalFrontier ( AgentToken token ) {
        return criticalFrontierTokens.contains(token);
    }

    /* -------------------- Getter, Setter ----------------------- */

    /**
     * @return the calculated value of this zone.
     */
    public int getValue () {
        return value;
    }

    /**
     * @return all vertices of this zone.
     */
    public LinkedList<Vertex> getVertices () {
        return vertices;
    }

    /**
     * @return the size of the zone (the number of vertices in this zone).
     */
    public int size () {
        return vertices.size();
    }

    /**
     * @return <ul>
     *         <li>{@code true}, if the zone has no vertices,</li>
     *         <li>{@code false} otherwise.</li>
     *         </ul>
     */
    public boolean isEmpty () {
        return vertices.isEmpty();
    }

    /**
     * @return all agent tokens in this zone.
     */
    public LinkedList<AgentToken> getTokens () {
        return tokens;
    }

    /**
     * @return all agent tokens in this zone of own team
     */
    public LinkedList<AgentToken> getTeamTokens () {
        return teamTokens;
    }

    /**
     * @return the number of agent tokens in this zone.
     */
    public int getNumberOfTokens () {
        return tokens.size();
    }

    /**
     * @return the number of team agent tokens in this zone.
     */
    public int getNumberOfTeamAgents () {
        return teamTokens.size();
    }

    /**
     * @return the agent tokens placed on the critical frontier.
     */
    public LinkedList<AgentToken> getCriticalFrontierTokens () {
        return criticalFrontierTokens;
    }

    /**
     * @param vertex the vertex to add.
     */
    public void addVertex ( Vertex vertex ) {
        vertices.add(vertex);
    }

    /**
     * @return the stability value. It is calculated by <br> {@code ( |inner tokens|
     *         * }{@link #stability_inner}<br> {@code + |critical frontier tokens| *
     *         }{@link #stability_front}{@code )}<br> {@code / |vertices|}
     */
    public double getStabilityValue () {
        return stabilityValue;
    }

    /**
     * gets the number of vertices building the critical frontier.
     * @return the frontier length.
     */
    public int getCriticalFrontierLength () {
        return getCriticalFrontier().size();
    }

    /**
     * gets the frontier vertices.
     * @return the frontier vertices.
     */
    public LinkedList<Vertex> getCriticalFrontier () {
        LinkedList<Vertex> criticalFrontierVertices = new LinkedList<Vertex>();
        for ( Vertex v : getVertices() )
            if ( isOnCriticalFrontier(v) )
                criticalFrontierVertices.add(v);
        return criticalFrontierVertices;

    }

    /**
     * @return the weakest critical frontier vertices, sorted by descending
     *         weakness
     */
    public LinkedList<Vertex> getWeakestCriticalFrontierVertices () {
        LinkedList<String> teams = new LinkedList<String>();
        teams.add(team);
        // determine all teams on the critical frontier
        LinkedList<String> teamsToAdd = new LinkedList<String>();
        for ( AgentToken t : criticalFrontierTokens )
            for ( String team : teams )
                if ( !team.equals(t.getTeam()) )
                    teamsToAdd.add(t.getTeam());
        teams.addAll(teamsToAdd);
        int[][] teamCount = new int[teams.size()][criticalFrontierVertices
                .size()];
        for ( int i = 0; i < criticalFrontierVertices.size(); i++ ) {
            Vertex v = criticalFrontierVertices.get(i);
            // count the tokens of each team on each critical frontier vertex
            for ( AgentToken t : v.getTokens() ) {
                for ( int j = 0; j < teams.size(); j++ )
                    if ( teams.get(j).equals(t.getTeam()) )
                        teamCount[j][i]++;
            }
            // fetch the second maximum count
            int max = 0;
            for ( int j = 0; j < teams.size(); j++ )
                if ( teamCount[j][i] > max && !teams.get(j).equals(team) )
                    max = teamCount[j][i];
            // save the difference
            teamCount[0][i] = teamCount[0][i] - max;
        }
        GeneralAlgorithms.sort(criticalFrontierVertices, teamCount[0]);
        return criticalFrontierVertices;
    }

    /**
     * @return a linked list of the most precious vertices of the zone. sorted
     *         from highest to lowest value
     */
    public LinkedList<Vertex> getMostPreciousVertices () {
        LinkedList<Vertex> mostPrecious = new LinkedList<Vertex>();
        int[] values = new int[vertices.size()];
        for ( int i = 0; i < vertices.size(); i++ ) {
            mostPrecious.add(vertices.get(i));
            values[vertices.size() - i - 1] = vertices.get(i).getValue();
        }
        GeneralAlgorithms.sort(mostPrecious, values);
        return mostPrecious;
    }

    /**
     * @param team the team to set
     */
    protected void setTeam ( String team ) {
        this.team = team;
    }

    /**
     * @return the team
     */
    public String getTeam () {
        return team;
    }

    /**
     * @param team the team to check
     * @return <ul>
     *         <li>{@code true} if the zone is from {@code team}</li>
     *         <li>{@code false} otherwise</li>
     *         </ul>
     */
    public boolean isOfTeam ( String team ) {
        return this.team.equals(team);
    }

    /**
     * @param perceptedValue the perceptedValue to set
     */
    public void setPerceptedValue ( int perceptedValue ) {
        this.perceptedValue = perceptedValue;
    }

    /**
     * @return the perceptedValue
     */
    public int getPerceptedValue () {
        return perceptedValue;
    }

    @Override
    public LogicBelief toBelief () {
        LinkedList<String> attributes = new LinkedList<String>();
        attributes.add(Integer.toString(value));
        for ( Vertex v : this.vertices )
            attributes.add(Integer.toString(v.getNumber()));
        return new LogicBelief("zone", attributes);
    }

    /**
     * @return a list of all vertices in the zone without the critical frontier.
     */
    @SuppressWarnings("unchecked")
    public LinkedList<Vertex> getNonCriticalVertices () {
        LinkedList<Vertex> nonCritical = (LinkedList<Vertex>) vertices.clone();
        nonCritical.removeAll(criticalFrontierVertices);
        return nonCritical;
    }

    /**
     * @param isolatedVertices the isolated vertices to set
     */
    protected void setIsolatedVertices ( LinkedList<Vertex> isolatedVertices ) {
        this.isolatedVertices = isolatedVertices;
    }

    /**
     * @return <ul>
     *         <li>the list of isolated vertices, if the zone is circular</li>
     *         <li>{@code null}, if the zone is not circular</li>
     *         </ul>
     */
    public LinkedList<Vertex> getIsolatedVertices () {
        return isolatedVertices;
    }

    /**
     * @return the number of isolated vertices
     */
    public int getNumberOfIsolatedVertices () {
        return isolatedVertices.size();
    }

    /**
     * @return <ul>
     *         <li>{@code true}, if the zone is circular</li>
     *         <li>{@code false}, otherwise</li>
     *         </ul>
     */
    public boolean isCircular () {
        return isolatedVertices != null;
    }

}
