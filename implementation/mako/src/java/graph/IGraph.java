package graph;

import java.util.HashMap;
import java.util.List;

import eis.Agent;
import eis.iilang.Identifier;
import eis.iilang.Numeral;

/**
 * This interface models a Graph structure. Vertices from the outside are
 * identified via Strings and mapped to Vertex-classes.
 * 
 * @author Sergey Dedukh
 * @author Miriam Koelle
 * @author Michael Ruster
 */
public interface IGraph {

    /**
     * Set the amount of vertices for the current simulation.
     * 
     * @param amountVertices
     *            amount of Vertices
     */
    public void setGlobalVerticesAmount(Numeral verticesAmount);

    /**
     * Set the amount of edges for the current simulation.
     * 
     * @param amountEdges
     *            amount of Edges
     */
    public void setGlobalEdgesAmount(Numeral edgesAmount);

    public Identifier getOurTeam();

    public void setOurTeam(Identifier ourTeam);

    public void addVertex(Identifier vertexID);

    public void addVertex(Identifier vertexID, Identifier teamID);

    public void setVertexVisited(Identifier vertexID);

    public boolean isVertexVisited(Identifier vertexID);

    public boolean isVertexProbed(Identifier vertexID);

    /**
     * This method returns whether all adjacent edge weights to {@code vertexID} are known.
     * @param vertexID the Vertex identifier to which other vertices are connected through weighted edges.
     * @return {@code true} if the survey action has been executed on this node.
     */
    public boolean isVertexSurveyed(Identifier vertexID);

    public boolean isEdgeSurveyed(Identifier vertexAID, Identifier vertexBID);

    public void updateVertexValue(Identifier vertexID, Numeral value);

    /**
     * This method adds an edge iff there is not such an edge already while
     * disregarding weights. Hence, a predefined weight of high value will be
     * assigned to edge. This should make travelling unknown edges too expensive
     * for agents to prefer them travelling known edges. If a Vertex identified
     * by {@code vertexAID} or {@code vertexBID} does not exist yet, then they
     * will be created first.
     * 
     * @param vertexA
     *            identifier for first Vertex.
     * @param vertexB
     *            identifier for second Vertex.
     */
    public void addEdge(Identifier vertexAID, Identifier vertexBID);

    /**
     * This method adds an edge iff there is not such an edge already with given
     * {@code weight}. If a Vertex identified by {@code vertexAID} or
     * {@code vertexBID} does not exist yet, then they will be created first.
     * 
     * @param vertexA
     *            identifier for first Vertex.
     * @param vertexB
     *            identifier for second Vertex.while disregarding weights
     * @param weight
     *            weight for the edge.
     */
    public void addEdge(Identifier vertexAID, Identifier vertexBID,
            Numeral weight);

    public List<Identifier> getShortestPath(Identifier vertexS,
            Identifier vertexD);

    /**
     * Function returns neighborhood of a current vertex with a given depth.
     * Uses Breadth First Search. Adds initial value to the result with depth =
     * 0.
     * 
     * @param vertexV
     *            Starting vertex.
     * @param depth
     *            Depth of search.
     * @return if vertexV exists in the graph and depth > 0 returns a HashMap<
     *         {@link Identifier} and {@link Integer}> of pairs (VertexID,
     *         depth). Otherwise returns null.
     */
    public HashMap<Identifier, Integer> getNeighborhood(Identifier vertexV,
            int depth);

    /**
     * 
     * @param vertexID
     *            Vertex identifier for a vertex that must be known.
     * @return value of Vertex or {@code null} if the ID is not associated with
     *         a known Vertex.
     */
    public Numeral getVertexValue(Identifier vertexID);

    public Numeral getEdgeWeight(Identifier vertexAID, Identifier vertexBID);

    public boolean existsPath(Identifier vertexSID, Identifier vertexDID);

    /**
     * Updates enemy agent's position on the graph. If the agent does not exist
     * on the map, it is created.
     * 
     * @param agent
     *            {@link Identifier} of an agent to add.
     * @param vertexID
     *            {@link Identifier} of a vertex where the agent is situated.
     */
    public void updateEnemyPosition(Identifier agent, Identifier vertexID);

    /**
     * Remove enemy agent's position from the map.
     * 
     * @param agent
     *            {@link Identifier} of the agent to remove.
     */
    public void removeEnemyPosition(Identifier agent);

    /**
     * Updates out team agent's position on the graph. If the agent does not
     * exist on the map, it is created.
     * 
     * @param agent
     *            {@link Identifier} of an agent to add.
     * @param vertexID
     *            {@link Identifier} of a vertex where the agent is situated.
     */
    public void updateTeamAgentPosition(Identifier agent, Identifier vertexID);

    /**
     * Query for one enemy agent's position on the map.
     * 
     * @param agent
     *            {@link Identifier} of an agent.
     * @return {@link Identifier} of a vertex where the agent is situated.
     *         Returns null if agent doesn't exist on the map.
     */
    public Identifier getEnemyPosition(Identifier agent);

    /**
     * Query for all enemy agents' positions on the map.
     * 
     * @return {@link HashMap} of pairs ({@link Identifier} of an agent,
     *         {@link Identifier} of a vertex).
     */
    public HashMap<Identifier, Identifier> getEnemyPositions();

    /**
     * Query for one our team agent's position on the map.
     * 
     * @param agent
     *            {@link Identifier} of an agent.
     * @return {@link Identifier} of a vertex where the agent is situated.
     *         Returns null if agent doesn't exist on the map.
     */
    public Identifier getTeamAgentPosition(Identifier agent);

    /**
     * Get distances of close enemies within the "depth" range from the vertex
     * "vertexID"
     * 
     * @return
     */
    public HashMap<Agent, Integer> getCloseEnemiesDistance(Identifier vertexID,
            int depth);

    /**
     * Get vertices of close enemies within the "depth" range from the vertex
     * "vertexID"
     * 
     * @return
     */
    public HashMap<Agent, Vertex> getCloseEnemies(Identifier vertexID, int depth);

    /**
     * Get vertices of close enemy Saboteurs within the "depth" range from the
     * vertex "vertexID"
     * 
     * @return
     */
    public HashMap<Agent, Vertex> getCloseEnemySaboteurs(Identifier vertexID,
            int depth);

    /**
     * Get vertices of close enemy Repairers within the "depth" range from the
     * vertex "vertexID"
     * 
     * @return
     */
    public HashMap<Agent, Vertex> getCloseEnemyRepairers(Identifier vertexID,
            int depth);

    /**
     * Returns a vertex corresponding to unvisited vertex with lowest edge
     * weight. If there are no unvisited vertices in the neighborhood sets
     * NextVertex = InitialVertex.
     * 
     * @param vertexID
     * @return
     */
    public Identifier getBestUnexploredVertex(Identifier vertexID);

}
