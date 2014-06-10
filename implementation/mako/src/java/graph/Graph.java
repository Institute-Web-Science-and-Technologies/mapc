package graph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import eis.Agent;
import eis.iilang.Identifier;
import eis.iilang.Numeral;

/**
 * This class represents global graph accessible by all agents. Implements
 * {@code IGraph} interface.
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
    private HashMap<Identifier, Vertex> vertices = new HashMap<>();

    private int globalVerticesAmount;
    private int globalEdgesAmount;
    private Identifier ourTeam = new Identifier("none");
    private HashMap<Identifier, Agent> enemyAgents = new HashMap<>();

    /**
     * Helper function, which casts {@code Numeral} to {@code int}.
     * 
     * @param val
     *            value in {@code Numeral} format
     * @return value of the {@code Numeral} or {@code -1} if casting was not
     *         possible.
     */
    private int Numeral2Int(Numeral val) {
        try {
            return Integer.valueOf(val.toString());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    @Override
    public synchronized void setGlobalVerticesAmount(Numeral verticesAmount) {
        this.globalVerticesAmount = this.Numeral2Int(verticesAmount);
    }

    @Override
    public synchronized void setGlobalEdgesAmount(Numeral edgesAmount) {
        this.globalEdgesAmount = this.Numeral2Int(edgesAmount);
    }

    @Override
    public Identifier getOurTeam() {
        return ourTeam;
    }

    @Override
    public void setOurTeam(Identifier ourTeam) {
        this.ourTeam = ourTeam;
    }

    @Override
    public synchronized void addVertex(Identifier vertexID) {
        Vertex vertex = getVertexOrCreateIt(vertexID);
        vertices.put(vertexID, vertex);
    }

    @Override
    public void addVertex(Identifier vertexID, Identifier teamID) {
        Vertex vertex = getVertexOrCreateIt(vertexID);
        vertex.setZoneTeam(teamID);
        vertices.put(vertexID, vertex);
    }

    @Override
    public synchronized void updateVertexValue(Identifier vertexID,
            Numeral value) {
        this.getVertexOrCreateIt(vertexID).setValue(value);
    }

    /**
     * This method simply calls {@code addEdge} with {@code null} weight.
     * 
     * @see #addEdge(Identifier, Identifier, Numeral)
     */
    @Override
    public synchronized void addEdge(Identifier vertexAID, Identifier vertexBID) {
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
    @Override
    public HashMap<Identifier, Integer> getNeighborhood(Identifier vertexV,
            int depth) {
        // Check depth
        if (depth < 0)
            return null;
        // Check if the vertex exists
        if (this.vertices.get(vertexV) == null)
            return null;

        HashMap<Identifier, Integer> result = new HashMap<>();
        result.put(vertexV, 0);

        HashSet<Identifier> currentVertices = new HashSet<>();
        currentVertices.add(vertexV);
        for (int i = 1; i <= depth; i++) {
            HashSet<Identifier> prevVertices = currentVertices;
            currentVertices = new HashSet<>();
            for (Identifier curVertex : prevVertices)
                for (Identifier neigh : this.vertices.get(curVertex).getEdges().keySet())
                    if (!result.containsKey(neigh)) {
                        currentVertices.add(neigh);
                        result.put(neigh, i);
                    }
        }
        return result;
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
    public synchronized void updateEnemyPosition(Identifier newVertexID, Agent a) {
        // TODO Auto-generated method stub

    }

    @Override
    public HashMap<Agent, Integer> getCloseEnemiesDistance(Identifier vertexID,
            int depth) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public HashMap<Agent, Vertex> getCloseEnemies(Identifier vertexID, int depth) {
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
