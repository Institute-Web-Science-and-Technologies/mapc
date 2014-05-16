package mas.agentsHempelsSofa.data.graph;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import apltk.interpreter.data.LogicBelief;
import mas.agentsHempelsSofa.algorithms.GraphAlgorithms;
import mas.agentsHempelsSofa.data.AgentToken;
import eis.iilang.Parameter;

/**
 * An implementation for our graph. It shall be used for all tokens to have an
 * overview of the playing-graph. A graph knows
 * <ul>
 * <li>it's vertices,</li>
 * <li>it's edges.</li>
 * <li>it's agent tokens.</li>
 * </ul>
 * @author Hempels-Sofa
 */
public class Graph {

    /* ------------------------ Fields ---------------------------- */

    /**
     * The vertices of the graph.
     */
    private LinkedList<Vertex> vertices;
    /**
     * An array of all possible vertices in the graph.
     */
    private Vertex[] vertexArray;
    /**
     * The edges of the graph.
     */
    private LinkedList<Edge> edges;
    /**
     * The position node of the agent.
     */
    private Vertex position;
    /**
     * The agent tokens on the graph.
     */
    private LinkedList<AgentToken> tokens;
    /**
     * the number of probed vertices.
     */
    private int numberOfProbedVertices;
    /**
     * the number of surveyed edges.
     */
    private int numberOfSurveyedEdges;
    /**
     * the total number of edges in the graph.
     */
    private int totalNumberOfEdges;

    /* --------------------- Constructors ------------------------- */

    /**
     * Creates a new empty graph.
     */
    public Graph () {
        vertices = new LinkedList<Vertex>();
        vertexArray = null;
        edges = new LinkedList<Edge>();
        tokens = new LinkedList<AgentToken>();
    }

    /* ------------------- Provided Methods ----------------------- */

    /**
     * Adds a new node to the graph with an {@code identifier}. If an equal node
     * is already contained, {@code false} is returned.
     * @param identifier The identifier of the new node.
     * @return <ul>
     *         <li>the already contained vertex,</li>
     *         <li>the newly added vertex.</li>
     *         </ul>
     */
    public Vertex add ( Parameter identifier ) {
        return add(new Vertex(identifier));
    }

    /**
     * Adds a node to the graph, if there is no equal vertex in the graph.
     * @return <ul>
     *         <li>the already contained vertex,</li>
     *         <li>the newly added vertex.</li>
     *         </ul>
     */
    public Vertex add ( Vertex vertex ) {
        if ( getVertex(vertex) != null )
            return getVertex(vertex);
        vertexArray[vertex.getNumber()] = vertex;
        // add to list
        int i = 0;
        for ( Vertex v : vertices ) {
            if ( v.compareTo(vertex) > 0 )
                break;
            i++;
        }
        vertices.add(i, vertex);
        return vertex;
    }

    /**
     * Adds a new edge to the graph, whereas the edge is built by two new
     * vertices which are created by {@code param1} and {@code param2}. This
     * also adds the vertices, if not contained already contained.
     * @param param1 The identifier of the first vertex.
     * @param param2 The identifier of the second vertex.
     * @return <ul>
     *         <li>the already contained edge,</li>
     *         <li>the newly added edge.</li>
     *         </ul>
     */
    public Edge add ( Parameter param1, Parameter param2 ) {
        return add(new Edge(new Vertex(param1), new Vertex(param2)));
    }

    /**
     * Adds an {@code edge} to the graph. This also adds the vertices, if not
     * already contained.
     * @param edge The edge to add.
     * @return <ul>
     *         <li>the already contained edge,</li>
     *         <li>the newly added edge.</li>
     *         </ul>
     */
    public Edge add ( Edge edge ) {
        Vertex v1 = getVertex(edge.getVertices()[0]);
        Vertex v2 = getVertex(edge.getVertices()[1]);
        if ( v1 == null ) {
            v1 = edge.getVertices()[0];
            add(v1);
        }
        if ( v2 == null ) {
            v2 = edge.getVertices()[1];
            add(v2);
        }
        if ( contains(edge) )
            return getEdge(edge);
        edge = new Edge(v1, v2, edge.getWeight());
        edge.getVertices()[0].addIncidentEdge(edge);
        edge.getVertices()[1].addIncidentEdge(edge);
        edges.add(edge);
        return edge;
    }

    @Override
    public String toString () {
        String s = "\n";
        s += new AdjacencyMatrix(this).toString();
        s += "\nval:  ";
        for ( Vertex v : vertices ) {
            if ( !v.isProbed() )
                s += "X  ";
            else
                s += v.getValue() + "  ";
        }
        return s + "\n";
    }

    /**
     * gets all relevant beliefs of the graph (edges, vertices and tokens).
     * @return a linked list of all relevant beliefs
     */
    public LinkedList<LogicBelief> toBeliefs () {
        LinkedList<LogicBelief> beliefs = new LinkedList<LogicBelief>();
        for ( Vertex v : vertices )
            beliefs.add(v.toBelief());
        for ( Edge e : edges )
            beliefs.add(e.toBelief());
        for ( AgentToken t : tokens )
            beliefs.add(t.toBelief());
        return beliefs;
    }

    /**
     * @param edge an edge
     * @return <ul>
     *         <li>{@code true}, if an equal edge is already on the Graph</li>
     *         <li>{@code false}, otherwise.</li>
     *         </ul>
     */
    public boolean contains ( Edge edge ) {
        return getEdge(edge) != null;
    }

    /**
     * @param vertex a vertex
     * @return <ul>
     *         <li>{@code true}, if an equal vertex is already on the Graph</li>
     *         <li>{@code false}, otherwise.</li>
     *         </ul>
     */
    public boolean contains ( Vertex vertex ) {
        return vertices.contains(this.getVertex(vertex));
    }

    /**
     * @param token an agent token
     * @return <ul>
     *         <li>{@code true}, if an equal token is already on the Graph</li>
     *         <li>{@code false}, otherwise.</li>
     *         </ul>
     */
    public boolean contains ( AgentToken token ) {
        for ( AgentToken a : tokens )
            if ( a.equals(token) )
                return true;
        return false;
    }

    /**
     * @param edges some edges
     * @return <ul>
     *         <li>{@code true}, if an equal edge for all {@code edges} is
     *         already on the Graph</li>
     *         <li>{@code false}, otherwise.</li>
     *         </ul>
     */
    public boolean containsAllEdges ( Collection<Edge> edges ) {
        for ( Edge e : edges )
            if ( !this.contains(e) )
                return false;
        return true;
    }

    /**
     * @param vertices some vertices
     * @return <ul>
     *         <li>{@code true}, if an equal vertex for all {@code vertices} is
     *         already on the Graph</li>
     *         <li>{@code false}, otherwise.</li>
     *         </ul>
     */
    public boolean containsAllVertices ( Collection<Vertex> vertices ) {
        for ( Vertex v : vertices )
            if ( !this.contains(v) )
                return false;
        return true;
    }

    /**
     * @param tokens some agent tokens
     * @return <ul>
     *         <li>{@code true}, if an equal agent for all {@code tokens} is
     *         already on the Graph</li>
     *         <li>{@code false}, otherwise.</li>
     *         </ul>
     */
    public boolean containsAllTokens ( Collection<AgentToken> tokens ) {
        for ( AgentToken a : tokens )
            if ( !this.contains(a) )
                return false;
        return true;
    }

    /**
     * adds an agent to the list of tokens and to the vertex, if not already
     * contained.
     * @param token the token to add.
     */
    public void add ( AgentToken token ) {
        tokens.add(token);
        Collections.sort(tokens);
        Vertex pos = token.getPosition();
        if ( !contains(pos) )
            vertices.add(pos);
        getVertex(pos).addToken(token);
    }

    /**
     * moves a token to another {@code vertex}. If there is no equal token in
     * the graph, a new token is added at that position.
     * @param token the agent token which shall be moved.
     * @param targetVertex the target vertex.
     */
    public void moveToken ( AgentToken token, Vertex targetVertex ) {
        Vertex target = getVertex(targetVertex);
        AgentToken save = null;
        if ( !contains(token) ) {
            token.setPosition(target);
            add(token);
        }
        else {
            save = getVertex(getToken(token).getPosition()).removeToken(token);
            getToken(token).setPosition(target);
            target.addToken(save);
        }
    }

    /**
     * updates an agent token. if the token is not already in the graph, it is
     * added. All values which are not set to null or (if it is an int value)
     * -1, are actualized (excluded: name and team). for different constructors
     * see {@link mas.agentsHempelsSofa.data.AgentToken}
     * @param token the agent token to update.
     */
    public void updateToken ( AgentToken token ) {
        AgentToken graphToken = getToken(token);
        // if not already contained, add token
        if ( graphToken == null ) {
            add(token);
            return;
        }
        // update attributes
        if ( getVertex(token.getPosition()) != getVertex(graphToken
                .getPosition()) )
            moveToken(graphToken, getVertex(token.getPosition()));
        if ( token.getTargetVertex() != null
                && !token.getTargetVertex()
                        .equals(graphToken.getTargetVertex()) )
            graphToken.setTargetVertex(getVertex(token.getTargetVertex()));
        if ( token.getState() != null && !token.getState().equals("") )
            graphToken.setState(token.getState());
        if ( (token.getRole() != null) && (!token.getRole().equals("")) )
            graphToken.setRole(token.getRole());
        if ( token.getEnergy() != -1 )
            graphToken.setEnergy(token.getEnergy());
        if ( token.getHealth() != -1 )
            graphToken.setHealth(token.getHealth());
        if ( token.getStrength() != -1 )
            graphToken.setStrength(token.getStrength());
        if ( token.getVisibilityRange() != -1 )
            graphToken.setVisibilityRange(token.getVisibilityRange());
        if ( token.getLastInspection() != -1 )
            graphToken.setLastInspection(token.getLastInspection());
        if ( token.getMaxHealth() != -1 )
            graphToken.setMaxHealth(token.getMaxHealth());
        if ( token.getMaxEnergy() != -1 )
            graphToken.setMaxEnergy(token.getMaxEnergy());
        if ( token.getMaxEnergyDisabled() != -1 )
            graphToken.setMaxEnergyDisabled(token.getMaxEnergyDisabled());
        if ( token.getLastUpdate() != -1
                && token.getLastUpdate() != graphToken.getLastUpdate() )
            graphToken.setLastUpdate(token.getLastUpdate());
        graphToken = getToken(token);
    }

    /* -------------------- Getter, Setter ------------------------ */

    /**
     * @return the adjacency matrix of this graph.
     */
    public AdjacencyMatrix getAdjacencyMatrix () {
        return new AdjacencyMatrix(this);
    }

    /**
     * @param param1 the first paramter.
     * @param param2 the second parameter.
     * @return the edge with the fitting vertex-paramteters.
     */
    public Edge getEdge ( Parameter param1, Parameter param2 ) {
        return getEdge(new Edge(param1, param2));
    }

    /**
     * @param vertex1 the first vertex.
     * @param vertex2 the second vertex.
     * @return the edge with the fitting vertices.
     */
    public Edge getEdge ( Vertex vertex1, Vertex vertex2 ) {
        return getEdge(new Edge(vertex1, vertex2));
    }

    /**
     * Returns the edge which equals {@code edge}.
     * @param edge the edge equal to the queried edge.
     * @return the edge fitting to the input edge.
     */
    public Edge getEdge ( Edge edge ) {
        Vertex v = getVertex(edge.getVertices()[0]);
        if ( v != null ) {
            for ( Edge e : v.getIncidentEdges() )
                if ( e.equals(edge) )
                    return e;
        }
        return null;
    }

    /**
     * @param param the parameter of the vertex.
     * @return the vertex with the fitting parameter.
     */
    public Vertex getVertex ( Parameter param ) {
        return getVertex(new Vertex(param));
    }

    /**
     * @param vertex a vertex.
     * @return the vertex which equals {@code vertex}.
     */
    public Vertex getVertex ( Vertex vertex ) {
        return vertexArray[vertex.getNumber()];
    }

    /**
     * @param vertexName the name of a vertex.
     * @return the vertex which equals the vertex with {@code vertexName}.
     */
    public Vertex getVertex ( String vertexName ) {
        return vertexArray[Integer
                .parseInt(vertexName.replaceAll("[^0-9]", ""))];
    }

    /**
     * @return A linked list of all vertices in the graph.
     */
    public LinkedList<Vertex> getVertices () {
        return vertices;
    }

    /**
     * @return A linked list of all edges in the graph.
     */
    public LinkedList<Edge> getEdges () {
        return edges;
    }

    /**
     * @param vertex the vertex to set as position.
     */
    public void setPosition ( Vertex vertex ) {
        Vertex pos = getVertex(vertex.getName());
        if ( pos == null ) {
            pos = new Vertex(vertex.getName());
            add(pos);
        }
        position = pos;
    }

    /**
     * @return the actual position of the agent.
     */
    public Vertex getPosition () {
        return position;
    }

    /**
     * @return the number of all explored vertices.
     */
    public int getNumberOfVertices () {
        return vertices.size();
    }

    /**
     * @return the number of explored edges
     */
    public int getNumberOfEdges () {
        return edges.size();
    }

    /**
     * gets the connected component which contains {@code vertex}.
     * @param vertex the vertex
     * @return the connected component.
     */
    public LinkedList<Vertex> getConnectedComponent ( Vertex vertex ) {
        return GraphAlgorithms.findConnectedComponent(this, vertex);
    }

    /**
     * gets the connected component which contains the actual position.
     * @return the connected component.
     */
    public LinkedList<Vertex> getConnectedComponent () {
        return getConnectedComponent(position);
    }

    /**
     * gets the connected component which contains {@code vertex}.
     * @param vertex the vertex
     * @return the connected component.
     */
    public LinkedList<Vertex> getSurveyedConnectedComponent ( Vertex vertex ) {
        return GraphAlgorithms.findSurveyedConnectedComponent(this, vertex);
    }

    /**
     * gets the connected component which contains the actual position.
     * @return the connected component.
     */
    public LinkedList<Vertex> getSurveyedConnectedComponent () {
        return getSurveyedConnectedComponent(position);
    }

    /**
     * @param vertex the vertex
     * @param team the team
     * @return <ul>
     *         <li>{@code true}, if an enemy agent is on an adjacent token to
     *         {@code vertex}</li> </li>{@code false}, otherwise</li>
     *         </ul>
     */
    public boolean isAdjacentToEnemyAgent ( Vertex vertex, String team ) {
        for ( Vertex v : vertex.getAdjacentVertices() )
            if ( v.hasEnemyTokens(team) )
                return true;
        return false;
    }

    /**
     * @param ownTeam the own team
     * @return all agent tokens
     */
    public LinkedList<AgentToken> getEnemyTokens ( String ownTeam ) {
        LinkedList<AgentToken> enemyTokens = new LinkedList<AgentToken>();
        for ( AgentToken t : tokens )
            if ( !t.getTeam().equals(ownTeam) )
                enemyTokens.add(t);
        return enemyTokens;
    }

    /**
     * @return a list of all teams which are known by this agent.
     */
    public LinkedList<String> getAllTeams () {
        LinkedList<String> teams = new LinkedList<String>();
        for ( AgentToken t : tokens ) {
            boolean contained = false;
            for ( String team : teams )
                if ( t.getTeam().equals(team) )
                    contained = true;
            if ( !contained )
                teams.add(t.getTeam());
        }
        return teams;
    }

    public void setProbed ( Vertex vertex, int value ) {
        getVertex(vertex).setValue(value);
        numberOfProbedVertices++;
    }

    public void setSurveyed ( Edge e, int weight ) {
        e = getEdge(e);
        e.setWeight(weight);
        e.getVertices()[0].decreaseNumberOfUnsurveyedIncidentEdges(1);
        e.getVertices()[1].decreaseNumberOfUnsurveyedIncidentEdges(1);
        numberOfSurveyedEdges++;
    }

    /**
     * @param vertexArray the vertexArray to set
     */
    public void setVertexArray ( Vertex[] vertexArray ) {
        this.vertexArray = vertexArray;
    }

    /**
     * @return the vertexArray
     */
    public Vertex[] getVertexArray () {
        return vertexArray;
    }

    /**
     * @return the number of explored edges.
     */
    public int getNumberOfExploredVertices () {
        return vertices.size();
    }

    /**
     * @return the ratio of explored vertices.
     */
    public double getRatioOfExploredVertices () {
        return (double) vertices.size() / vertexArray.length;
    }

    /**
     * @return the number of probed vertices.
     */
    public int getNumberOfProbedVertices () {
        return numberOfProbedVertices;
    }

    /**
     * @return the ratio of probed vertices.
     */
    public double getRatioOfProbedVertices () {
        return (double) numberOfProbedVertices / vertexArray.length;
    }

    /**
     * @return the number of explored edges.
     */
    public int getNumberOfExploredEdges () {
        return edges.size();
    }

    /**
     * @return the ratio of explored edges.
     */
    public double getRatioOfExploredEdges () {
        return (double) edges.size() / totalNumberOfEdges;
    }

    /**
     * @return the number of surveyed edges.
     */
    public int getNumberOfSurveyedEdges () {
        return numberOfSurveyedEdges;
    }

    /**
     * @return the ratio of surveyed edges.
     */
    public double getRatioOfSurveyedEdges () {
        return (double) numberOfSurveyedEdges / totalNumberOfEdges;
    }

    /**
     * @return the total number of vertices in this simulation.
     */
    public int getTotalNumberOfVertices () {
        return vertexArray.length;
    }

    /**
     * @return the total number of edges in this simulation.
     */
    public int getTotalNumberOfEdges () {
        return totalNumberOfEdges;
    }

    /**
     * @param number the total number of edges in this simulation.
     */
    public void SetTotalNumberOfEdges ( int number ) {
        totalNumberOfEdges = number;
    }

    /**
     * @return All the agent Tokens on the graph
     */
    public LinkedList<AgentToken> getTokens () {
        return tokens;
    }

    /**
     * @param token an agent token
     * @return a reference to the token which is already in the graph. if there
     *         is no token yet, {@code null} is returned.
     */
    public AgentToken getToken ( AgentToken token ) {
        for ( AgentToken a : tokens )
            if ( a.equals(token) )
                return a;
        return null;
    }

}
