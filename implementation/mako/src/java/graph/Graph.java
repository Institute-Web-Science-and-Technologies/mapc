package graph;

import java.util.HashMap;
import java.util.HashSet;
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
    private HashMap<Numeral, Vertex> vertices;
    private HashSet<Edge> edges;

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

    @Override
    public synchronized void addEdge(Identifier vertexA, Identifier vertexB) {
        Edge edge = new Edge(vertexA, vertexB);
        // a test for duplicates is not needed as this is edges are a set:
        edges.add(edge);

        Vertex vertex = vertices.get(vertexA);
        if (vertex == null) {
            addVertex(vertexA);
        }
        vertex = vertices.get(vertexB);
        if (vertex == null) {
            addVertex(vertexB);
        }
    }

    @Override
    public synchronized void removeEnemyPosition(Agent a) {
        // TODO Auto-generated method stub

    }

    @Override
    public synchronized void updateEdgeWeight(Identifier vertexID,
            Numeral weight) {
        // TODO Auto-generated method stub

    }

    @Override
    public List<Identifier> getShortestPath(Identifier vertexS,
            Identifier vertexD) {
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

    @Override
    public Numeral getEdgeWeight(Identifier vertexSID, Identifier vertexDID) {
        // TODO Auto-generated method stub
        return null;
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
