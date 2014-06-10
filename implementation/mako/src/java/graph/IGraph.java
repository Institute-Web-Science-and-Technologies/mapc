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
     * Set global parameters of the graph - amount of Vertices and amount of
     * Edges
     * 
     * @param amountVertices
     *            amount of Vertices
     * @param amountEdges
     *            amount of Edges
     */
    public void setGlobalAmounts(Numeral amountVertices, Numeral amountEdges);

    public Identifier getOurTeam();

    public void setOurTeam(Identifier ourTeam);

    public void addVertex(Identifier vertexID);

    public void addVertex(Identifier vertexID, Identifier teamID);

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

    public List<Identifier> getNeighborhood(Identifier vertexV, int depth);

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

    public void addEnemyPosition(Identifier vertexID, Agent a);

    public void removeEnemyPosition(Agent a);

    public void updateEnemyPosition(Identifier newVertexID, Agent a);

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
    public HashMap<Agent, Vertex>
            getCloseEnemies(Identifier vertexID, int depth);

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
}
