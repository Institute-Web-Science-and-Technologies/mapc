package graph;

import java.util.HashMap;
import java.util.List;

import eis.Agent;
import eis.iilang.Identifier;
import eis.iilang.Numeral;

/**
 * This class represents global graph accessible by all agents. Implements
 * IGraph interface.
 * 
 * @author Sergey Dedukh
 * @author Miriam Koelle
 * @author Michael Ruster
 */
public class Graph implements IGraph {

    private static Graph instance = null;

    private Graph() {
    }

    /**
     * Thread-safe singleton pattern.
     * 
     * @return existing instance or create an instance of {@code Graph}.
     */
    public synchronized static Graph getInstance() {
        if (instance == null) {
            instance = new Graph();
        }
        return instance;
    }

    /**
     * The Integer maps the vertex id for faster querying.
     */
    private HashMap<Identifier, Vertex> vertices;

    private int amountVertices;
    private int amountEdges;

    @Override
    public synchronized void addVertex(Identifier vertexID) {
        // TODO Auto-generated method stub

    }

    @Override
    public synchronized void updateVertexValue(Identifier vertexID,
            Numeral value) {
        // TODO Auto-generated method stub

    }

    /**
     * This method simply calls {@code addEdge} with {@code null} weight.
     * 
     * @see #addEdge(Identifier, Identifier, Numeral)
     */
    @Override
    public synchronized void
            addEdge(Identifier vertexAID, Identifier vertexBID) {
        this.addEdge(vertexAID, vertexBID, null);
    }

    @Override
    public synchronized void removeEnemyPosition(Agent a) {
        // TODO Auto-generated method stub

    }

    /**
     * This method adds an edge between {@code vertexAID} and {@code vertexBID}
     * with the given {@code weight}. It also creates said vertices if needed.
     * 
     * @see #getVertexOrCreateIt(Identifier)
     */
    @Override
    public synchronized void addEdge(Identifier vertexAID,
            Identifier vertexBID, Numeral weight) {
        if (!vertexAID.equals(vertexBID)) {
            Vertex vertexA = this.getVertexOrCreateIt(vertexAID);
            vertexA.addEdge(vertexBID, weight);

            Vertex vertexB = this.getVertexOrCreateIt(vertexBID);
            vertexB.addEdge(vertexAID, weight);
        }
    }

    /**
     * 
     * @param vertexID
     *            which identifies a Vertex in {@code vertices} or is the
     *            identifier for a newly created one.
     * @return Vertex that is identified by {@code vertexID} in {@code vertices}
     *         or creates a new one with {@code value} 1 in {@code vertices} and
     *         returns that one.
     */
    private Vertex getVertexOrCreateIt(Identifier vertexID) {
        Vertex vertex = vertices.get(vertexID);
        if (vertex == null) {
            vertex = new Vertex(vertexID);
            vertices.put(vertexID, vertex);
        }
        return vertex;
    }

    @Override
    public List<Identifier> getShortestPath(Identifier vertexSID,
            Identifier vertexDID) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Identifier> getNeighborhood(Identifier vertexV, int depth) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Numeral getVertexValue(Identifier vertexID) {
        Vertex vertex = vertices.get(vertexID);
        if (vertex == null) {
            return null;
        } else {
            return vertex.getValue();
        }
    }

    /**
     * @return {@code null} if such an edge, vertexS or vertexD does not exist.
     *         Else return the edge weight.
     */
    @Override
    public Numeral getEdgeWeight(Identifier vertexSID, Identifier vertexDID) {
        Vertex vertex = vertices.get(vertexSID);
        if (vertex == null) {
            return null;
        }
        return vertex.getEdgeWeight(vertexDID);
    }

    @Override
    public boolean existsPath(Identifier vertexSID, Identifier vertexDID) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public synchronized void addEnemyPosition(Identifier vertexID, Agent a) {
        // TODO Auto-generated method stub

    }

    @Override
    public synchronized void
            updateEnemyPosition(Identifier newVertexID, Agent a) {
        // TODO Auto-generated method stub

    }

    @Override
    public HashMap<Agent, Integer> getCloseEnemiesDistance(Identifier vertexID,
            int depth) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public HashMap<Agent, Vertex>
            getCloseEnemies(Identifier vertexID, int depth) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public HashMap<Agent, Vertex> getCloseEnemySaboteurs(Identifier vertexID,
            int depth) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public HashMap<Agent, Vertex> getCloseEnemyRepairers(Identifier vertexID,
            int depth) {
        // TODO Auto-generated method stub
        return null;
    }

}
