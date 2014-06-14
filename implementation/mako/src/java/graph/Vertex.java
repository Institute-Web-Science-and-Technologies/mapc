package graph;

import java.util.HashMap;
import java.util.HashSet;

import eis.Agent;
import eis.iilang.Identifier;
import eis.iilang.Numeral;

/**
 * This class models a vertex of a graph.
 * 
 * @author Sergey Dedukh
 * @author Miriam Koelle
 * @author Michael Ruster
 */
public class Vertex {

    private Identifier id;
    private Numeral value;
    private boolean isVisited;
    private boolean isProbed;
    private HashSet<Agent> teamAgents;
    private HashSet<Agent> enemyAgents;
    private HashMap<Identifier, Numeral> edges;
    private final int edgeUnsurveyedWeight = 1000;
    /**
     * The returned values can be A, B or none.
     */
    private Identifier zoneTeam;

    Vertex(Identifier id) {
        this.id = id;
        this.value = new Numeral(1);
        this.edges = new HashMap<>();
        this.isVisited = false;
        this.isProbed = false;
    }

    public Identifier getId() {
        return id;
    }

    public Numeral getValue() {
        return value;
    }

    public Identifier getZoneTeam() {
        return zoneTeam;
    }

    public synchronized void setValue(Numeral value) {
        this.value = value;
        this.isProbed = true;
    }

    public boolean isVisited() {
        return isVisited;
    }

    public synchronized void setVisited(boolean isVisited) {
        this.isVisited = isVisited;
    }

    public boolean isProbed() {
        return isProbed;
    }

    public boolean isEdgeSurveyed(Identifier neighVertexID) {
        if (!edges.containsKey(neighVertexID))
            return false;
        Numeral weight = edges.get(neighVertexID);
        return (weight.getValue().intValue() != this.edgeUnsurveyedWeight);
    }

    public HashSet<Agent> getTeamAgents() {
        return teamAgents;
    }

    public synchronized void setTeamAgents(HashSet<Agent> teamAgents) {
        this.teamAgents = teamAgents;
    }

    public HashSet<Agent> getEnemyAgents() {
        return enemyAgents;
    }

    public synchronized void setEnemyAgents(HashSet<Agent> enemyAgents) {
        this.enemyAgents = enemyAgents;
    }

    public synchronized void setZoneTeam(Identifier zoneTeam) {
        this.zoneTeam = zoneTeam;
    }

    public HashMap<Identifier, Numeral> getEdges() {
        return edges;
    }

    public synchronized void setEdges(HashMap<Identifier, Numeral> edges) {
        this.edges = edges;
    }

    /**
     * Adds an edge if {@code vertexDID} is not this Vertex because edges may
     * not span from a Vertex to itself. If such an edge already existed, the
     * overwrites its value.
     * 
     * @param vertexDID
     *            destination ID of a Vertex to which an edge should be spanned.
     *            It may not be this Vertex's ID.
     * @param weight
     *            can be {@code null}. Then, the weight will be set high (1000)
     *            to simulate high costs for travelling unknown edges.
     */
    public synchronized void addEdge(Identifier vertexDID, Numeral weight) {
        if (!(vertexDID.equals(this.id))) {
            if (weight == null) {
                weight = new Numeral(this.edgeUnsurveyedWeight);
            }
            edges.put(vertexDID, weight);
        }
    }

    public Numeral getEdgeWeight(Identifier vertexDID) {
        return edges.get(vertexDID);
    }
}
