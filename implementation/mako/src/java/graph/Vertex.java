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
    private boolean isVisited = false;
    private boolean isProbed = false;
    private HashSet<Agent> teamAgents;
    private HashSet<Agent> enemyAgents;
    private HashMap<Identifier, Numeral> edges;
    private TeamEnum zoneTeam;

    Vertex(Identifier id) {
        this.id = id;
        this.value = new Numeral(1);
    }

    public Identifier getId() {
        return id;
    }

    public void setId(Identifier id) {
        this.id = id;
    }

    public Numeral getValue() {
        return value;
    }

    public TeamEnum getZoneTeam() {
        return zoneTeam;
    }

    public void setValue(Numeral value) {
        this.value = value;
    }

    public boolean isVisited() {
        return isVisited;
    }

    public void setVisited(boolean isVisited) {
        this.isVisited = isVisited;
    }

    public boolean isProbed() {
        return isProbed;
    }

    public void setProbed(boolean isProbed) {
        this.isProbed = isProbed;
    }

    public HashSet<Agent> getTeamAgents() {
        return teamAgents;
    }

    public void setTeamAgents(HashSet<Agent> teamAgents) {
        this.teamAgents = teamAgents;
    }

    public HashSet<Agent> getEnemyAgents() {
        return enemyAgents;
    }

    public void setEnemyAgents(HashSet<Agent> enemyAgents) {
        this.enemyAgents = enemyAgents;
    }

    public void setZoneTeam(TeamEnum zoneTeam) {
        this.zoneTeam = zoneTeam;
    }

    public TeamEnum getOccupyingTeam() {
        return null;
    }

    public HashMap<Identifier, Numeral> getEdges() {
        return edges;
    }

    public void setEdges(HashMap<Identifier, Numeral> edges) {
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
    public void addEdge(Identifier vertexDID, Numeral weight) {
        if (!(vertexDID.equals(this.id))) {
            if (weight == null) {
                weight = new Numeral(1000);
            }
            edges.put(vertexDID, weight);
        }
    }

    public Numeral getEdgeWeight(Identifier vertexDID) {
        return edges.get(vertexDID);
    }
}
