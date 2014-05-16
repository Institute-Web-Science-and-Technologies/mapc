package mas.agentsHempelsSofa.data.graph;

import java.util.LinkedList;

import mas.agentsHempelsSofa.data.AgentToken;
import mas.agentsHempelsSofa.data.Believable;

import apltk.interpreter.data.LogicBelief;

import eis.iilang.Identifier;
import eis.iilang.Parameter;

/**
 * An implementation of the vertices for our graph. A node knows
 * <ul>
 * <li>it's incident edges,</li>
 * <li>it's adjacent vertices,</li>
 * <li>it's value,</li>
 * <li>the number of incident unsurveyed edges.</li>
 * <li>the graph it belongs to</li>
 * </ul>
 * @author Hempels-Sofa
 */
public class Vertex implements Comparable<Vertex>, Believable {

    /* ------------------------ Fields ---------------------------- */

    /**
     * The identifier of the node.
     */
    private Parameter identifier;
    /**
     * the number of this vertex
     */
    private int number;
    /**
     * The value of the node. If 0, the node is not probed yet.
     */
    private int value;
    /**
     * The not-surveyed incident edges.
     */
    private int numberOfUnsurveyedIncidentEdges;
    /**
     * The list of incident edges.
     */
    private LinkedList<Edge> incidentEdges;
    /**
     * The list of agent tokens, which are currently located on the vertex
     */
    private LinkedList<AgentToken> tokens;
    /**
     * Used for graph algorithms. Indicates weighted distance to the tokens
     * position.
     */
    private double weightedDistance;

    public static final int UNPROBED_VALUE = 0;

    /* --------------------- Constructors ------------------------- */

    /**
     * Creates a new node with a specified {@code identifier}, a certain {@code
     * value}, a boolean {@code probed} which says whether the real value is
     * known and a {@code graph} it thinks it belongs to.
     * @param identifier The identifier of the node.
     * @param value The value of the node.
     */
    public Vertex ( Parameter identifier, int value ) {
        this.setIdentifier(identifier);
        this.number = Integer.parseInt(getName().replace("vertex", ""));
        this.setValue(value);
        this.incidentEdges = new LinkedList<Edge>();
        this.tokens = new LinkedList<AgentToken>();
    }

    /**
     * Creates a new unprobed node with a specified {@code identifier}, unknown
     * value and a {@code graph} it thinks it belongs to.
     * @param identifier The identifier of the node.
     */
    public Vertex ( Parameter identifier ) {
        this(identifier, UNPROBED_VALUE);
    }

    public Vertex ( String identifier ) {
        this(new Identifier(identifier));
    }

    /* ------------------- Provided Methods ----------------------- */

    @Override
    public final boolean equals ( Object obj ) {
        if ( obj != null )
            return ((Vertex) obj).number == number;
        return false;
    }

    @Override
    public final String toString () {
        return getName().replace("ertex", "");
    }

    @Override
    public final int compareTo ( Vertex v ) {
        if ( number < v.number )
            return -1;
        if ( number > v.number )
            return 1;
        return 0;
    }

    /**
     * generates a belief for this node.
     * @return a belief with
     *         <ul>
     *         <li>{@code predicate} - vertex,</li>
     *         <li>{@code parameter1} - identifier,</li>
     *         <li>{@code parameter2} - value,</li>
     *         <li>{@code parameter3} - probed</li>
     *         </ul>
     */
    @Override
    public LogicBelief toBelief () {
        LinkedList<String> parameters = new LinkedList<String>();
        parameters.add(getName());
        parameters.add(Integer.toString(value));
        parameters.add(Boolean.toString(isProbed()));
        return new LogicBelief("vertex", parameters);
    }

    /**
     * Immediately adds an incident edge to the vertex.
     * @param incidentEdge the incidentEdge.
     */
    public void addIncidentEdge ( Edge incidentEdge ) {
        incidentEdges.add(incidentEdge);
        if ( !incidentEdge.isSurveyed() )
            numberOfUnsurveyedIncidentEdges++;
    }

    /**
     * @param adjacentVertex The vertex which shall be tested.
     * @return <ul>
     *         <li>{@code true}, if {@code adjacentVertex} is adjacent to this
     *         vertex,</li>
     *         <li>{@code false}, otherwise.</li>
     *         </ul>
     */
    public boolean isAdjacentTo ( Vertex adjacentVertex ) {
        if ( adjacentVertex == null )
            return false;
        for ( Vertex vertex : getAdjacentVertices() )
            if ( vertex.equals(adjacentVertex) )
                return true;
        return false;
    }

    /**
     * clears the list of incident unsurveyed edges.
     */
    public void surveyAllIncidentEdges () {
        numberOfUnsurveyedIncidentEdges = 0;
    }

    /* -------------------- Getter, Setter ------------------------ */

    /**
     * @return the incidentEdges
     */
    public LinkedList<Edge> getIncidentEdges () {
        return incidentEdges;
    }

    /**
     * @return the adjacentVertices
     */
    public LinkedList<Vertex> getAdjacentVertices () {
        LinkedList<Vertex> adjacentVertices = new LinkedList<Vertex>();
        for ( Edge e : incidentEdges )
            if ( e.getVertices()[0].equals(this) )
                adjacentVertices.add(e.getVertices()[1]);
            else
                adjacentVertices.add(e.getVertices()[0]);
        return adjacentVertices;
    }

    /**
     * @param identifier the identifier to set.
     */
    public void setIdentifier ( Parameter identifier ) {
        this.identifier = identifier;
    }

    /**
     * @return the name
     */
    public Parameter getIdentifier () {
        return identifier;
    }

    /**
     * @return the probed
     */
    public boolean isProbed () {
        return !(this.value == UNPROBED_VALUE);
    }

    /**
     * Sets the {@code value} of the vertex.
     * @param value The new value.
     */
    public void setValue ( int value ) {
        this.value = value;
    }

    /**
     * @return the value
     */
    public int getValue () {
        if ( value == UNPROBED_VALUE )
            return 1;
        return value;
    }

    /**
     * @return the numberOfUnsurveyedEdges
     */
    public int getNumberOfUnsurveyedEdges () {
        return numberOfUnsurveyedIncidentEdges;
    }

    /**
     * @param weightedDistance the weightedDistance to set
     */
    public void setWeightedDistance ( double weightedDistance ) {
        this.weightedDistance = weightedDistance;
    }

    /**
     * @return the weightedDistance
     */
    public double getWeightedDistance () {
        return weightedDistance;
    }

    /**
     * @return the number of incident edges
     */
    public int getNumberOfEdges () {
        return this.incidentEdges.size();
    }

    /**
     * @return the name of the vertex
     */
    public String getName () {
        return identifier.toProlog();
    }

    /**
     * @param token The agent token which has to be added to the vertex
     */
    public void addToken ( AgentToken token ) {
        if ( !tokens.contains(token) )
            this.tokens.add(token);
    }

    /**
     * @return A List of all the Tokens which are on the vertex
     */
    public LinkedList<AgentToken> getTokens () {
        return tokens;
    }

    /**
     * Determines, whether the agent token is on the vertex or not
     * @param token The agent token
     * @return True, if the agent token is on the vertex
     */
    public boolean contains ( AgentToken token ) {
        for ( AgentToken t : tokens )
            if ( t != null && t.equals(token) )
                return true;
        return false;
    }

    /**
     * removes a token from the vertex
     * @param token agent token to remove.
     */
    public AgentToken removeToken ( AgentToken token ) {
        if ( contains(token) ) {
            AgentToken save = null;
            for ( AgentToken t : tokens )
                if ( t.equals(token) )
                    save = t;
            if ( save != null )
                return tokens.remove(tokens.indexOf(save));
        }
        return null;
    }

    /**
     * @return <ul>
     *         <li>{@code true}, if the vertex s already target of a different
     *         agent,</li>
     *         <li>{@code false}, otherwise.</li>
     *         </ul>
     */
    public boolean isTarget ( String teamname ) {
        if ( tokens.size() > 0 )
            for ( AgentToken a : tokens )
                if ( a.getTeam() == teamname )
                    return true;
        return false;
    }

    /**
     * @return the names of all agents.
     */
    public String[] getAgentNames () {
        String[] names = new String[tokens.size()];
        for ( AgentToken a : tokens )
            names[tokens.indexOf(a)] = a.getName();
        return names;
    }

    public boolean hasEnemyTokens ( String team ) {
        for ( AgentToken t : tokens )
            if ( !t.getTeam().equals(team) )
                return true;
        return false;
    }

    public boolean hasUnsurveyedEdges () {
        return numberOfUnsurveyedIncidentEdges > 0;
    }

    public int getNumber () {
        return number;
    }

    public void decreaseNumberOfUnsurveyedIncidentEdges ( int n ) {
        numberOfUnsurveyedIncidentEdges -= n;
    }

}
