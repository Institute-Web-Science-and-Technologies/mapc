package graph;

import java.util.HashMap;
import java.util.List;

import eis.Agent;

/**
 * This interface models a Graph structure. Vertices from the outside are
 * identified via Strings and mapped to Vertex-classes.
 * 
 * @author Sergey Dedukh
 * @author Miriam Koelle
 * @author Michael Ruster
 */
public interface IGraph {

    public void addVertex(String vertexID);

    public void updateVertexValue(String vertexID, int value);

    public void addEdge(String vertexS, String vertexD);

    public void updateEdgeWeight(String vertexID, int weight);

    public List<String> getShortestPath(String vertexS, String vertexD);

    public List<String> getNeighborhood(String vertexV, int depth);

    public int getVertexValue(String vertexID);

    public int getEdgeWeight(String vertexS, String vertexD);

    public boolean existsPath(String vertexS, String vertexD);

    public void addEnemyPosition(String vertexID, Agent a);

    public void removeEnemyPosition(Agent a);

    public void updateEnemyPosition(String newVertexID, Agent a);

    /**
     * Get distances of close enemies within the "depth" range from the vertex
     * "vertexID"
     * 
     * @return
     */
    public HashMap<Agent, Integer> getCloseEnemiesDistance(String vertexID,
            int depth);

    /**
     * Get vertices of close enemies within the "depth" range from the vertex
     * "vertexID"
     * 
     * @return
     */
    public HashMap<Agent, Vertex> getCloseEnemies(String vertexID, int depth);

    /**
     * Get vertices of close enemy Saboteurs within the "depth" range from the
     * vertex "vertexID"
     * 
     * @return
     */
    public HashMap<Agent, Vertex> getCloseEnemySaboteurs(String vertexID,
            int depth);

    /**
     * Get vertices of close enemy Repairers within the "depth" range from the
     * vertex "vertexID"
     * 
     * @return
     */
    public HashMap<Agent, Vertex> getCloseEnemyRepairers(String vertexID,
            int depth);
}
