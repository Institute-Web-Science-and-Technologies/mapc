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

    public void addVertex(Identifier vertexID);

    public void updateVertexValue(Identifier vertexID, Numeral value);

    public void addEdge(Identifier vertexA, Identifier vertexB);

    public void updateEdgeWeight(Identifier vertexID, Numeral weight);

    public List<Identifier> getShortestPath(Identifier vertexS,
            Identifier vertexD);

    public List<Identifier> getNeighborhood(Identifier vertexV, int depth);

    public int getVertexValue(Identifier vertexID);

    public int getEdgeWeight(Identifier vertexS, Identifier vertexD);

    public boolean existsPath(Identifier vertexS, Identifier vertexD);

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
}
