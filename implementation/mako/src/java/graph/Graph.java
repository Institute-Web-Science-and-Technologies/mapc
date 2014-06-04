package graph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import eis.Agent;

/**
 * This class represents global graph accessible by all agents. Implements
 * IGraph interface.
 * 
 * @author Sergey Dedukh
 * @author Miriam Koelle
 * @author Michael Ruster
 */
public class Graph implements IGraph {
    /**
     * The Integer maps the vertex id for faster querying.
     */
    private HashMap<Integer, Vertex> vertices;
    private HashSet<Edge> edges;

    private int amountVertices;
    private int amountEdges;

    @Override
    public synchronized void addVertex(String vertexID) {
        // TODO Auto-generated method stub

    }

    @Override
    public synchronized void updateVertexValue(String vertexID, int value) {
        // TODO Auto-generated method stub

    }

    @Override
    public synchronized void addEdge(String vertexS, String vertexD) {
        // TODO Auto-generated method stub

    }

    @Override
    public synchronized void updateEdgeWeight(String vertexID, int weight) {
        // TODO Auto-generated method stub

    }

    @Override
    public List<String> getShortestPath(String vertexS, String vertexD) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getNeighborhood(String vertexV, int depth) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getVertexValue(String vertexID) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getEdgeWeight(String vertexS, String vertexD) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean existsPath(String vertexS, String vertexD) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public synchronized void addEnemyPosition(String vertexID, Agent a) {
        // TODO Auto-generated method stub

    }

    @Override
    public synchronized void removeEnemyPosition(Agent a) {
        // TODO Auto-generated method stub

    }

    @Override
    public synchronized void updateEnemyPosition(String newVertexID, Agent a) {
        // TODO Auto-generated method stub

    }

    @Override
    public HashMap<Agent, Integer> getCloseEnemiesDistance(String vertexID,
            int depth) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public HashMap<Agent, Vertex> getCloseEnemies(String vertexID, int depth) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public HashMap<Agent, Vertex> getCloseEnemySaboteurs(String vertexID,
            int depth) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public HashMap<Agent, Vertex> getCloseEnemyRepairers(String vertexID,
            int depth) {
        // TODO Auto-generated method stub
        return null;
    }

}
