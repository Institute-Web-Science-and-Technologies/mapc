package eis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * Handles values and methods related to nodes.
 * 
 * @author Artur Daudrich
 * @author Michael Sewell
 * 
 */
public class Vertex {

    // The 'name' of the vertex, e.g. 'v81'
    private String identifier = "";
    // the team currently occupying the vertex, e.g. 'teamA'
    private String team = "none";
    // the node value, which is 1 to 10 after probing
    // '0' in this case is a bit of a hack to designate that the node has not
    // been probed
    private int value = 0;

    // destinations saves a list of all the other vertices we know a path to.
    // the integer value is the minimal hop distance to the vertex
    private HashMap<Integer, ArrayList<Vertex>> destinationMap = new HashMap<Integer, ArrayList<Vertex>>();
    // for every destination, pathMap saves the path to it
    private HashMap<Vertex, Path> pathMap = new HashMap<Vertex, Path>();
    // stores the node value of each node in our zone
    private HashMap<Vertex, Integer> zoneValueMap = new HashMap<Vertex, Integer>();
    // the number of agents required to build a zone, and the zones themselves
    private HashMap<Integer, Zone> zones = new HashMap<Integer, Zone>();

    /**
     * @param identifier
     *            The name of the vertex, e.g. 'v31'.
     */
    public Vertex(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    /**
     * @param team
     *            The identifier of the team currently occupying the node.
     */
    public void setTeam(String team) {
        if (!this.team.equalsIgnoreCase(team)) {
            this.team = team;
        }
    }

    /**
     * @return The vertex value (1-10).
     */
    public int getValue() {
        if (this.value == 0) {
            return 1;
        }
        return this.value;
    }

    /**
     * Sets the vertex value and triggers zone recalculation. Used after
     * probing.
     * 
     * @param value
     *            The node value (1-10).
     */
    public void setValue(int value) {
        if (this.value == 0) {
            this.value = value;
            calculateZoneValue();
            informZoneAboutNodeValue();
        }
    }

    private void informZoneAboutNodeValue() {
        ArrayList<Vertex> zoneVertices = new ArrayList<Vertex>();
        zoneVertices.addAll(destinationMap.get(1));
        zoneVertices.addAll(destinationMap.get(2));
        for (Vertex vertex : zoneVertices) {
            vertex.setZoneNodeValue(this, this.getValue());
        }
    }

    /**
     * Sets the node value of a node in the two-hop neighbourhood, which is used
     * for calculating the zone value of this node. Also triggers zone value
     * recalculation.
     * 
     * @param vertex
     *            the neighbour vertex
     * @param vertexValue
     *            the node value of the neighbour
     */
    public void setZoneNodeValue(Vertex vertex, int vertexValue) {
        // only update value if it was previously unknown or worse
        if (!zoneValueMap.containsKey(vertex) || zoneValueMap.get(vertex) < vertexValue) {
            zoneValueMap.put(vertex, vertexValue);
            calculateZoneValue();
        }
    }

    private void calculateZoneValue() {
        // calculate agent positions
        ArrayList<Vertex> agentPositions = new ArrayList<Vertex>();
        agentPositions.add(this);

        ArrayList<Vertex> oneHops = destinationMap.get(1);
        ArrayList<Vertex> twoHops = destinationMap.get(2);

        for (int i = twoHops.size(); i > 0; i--) {
            Vertex vertex = twoHops.get(i);
            if (vertex.connectedWithVerticesInOneHop(oneHops) >= 2) {
                twoHops.remove(vertex);
                agentPositions.add(vertex);
            }
        }
        for (int i = twoHops.size(); i > 0; i--) {
            Vertex vertex = twoHops.get(i);
            if (vertex.connectedWithVerticesInOneHop(agentPositions) >= 1) {
                twoHops.remove(vertex);
                continue;
            }
            if (vertex.connectedWithVerticesInTwoHops(oneHops, agentPositions)) {
                twoHops.remove(vertex);
            }
        }
        for (int i = twoHops.size(); i > 0; i--) {
            Vertex vertex = twoHops.get(i);
            ArrayList<Vertex> bridge = vertex.buildBridge(twoHops);
            if (bridge.size() == 2) {
                twoHops.remove(vertex);
                agentPositions.add(bridge.get(0));
                agentPositions.add(bridge.get(1));
            }
        }
        ArrayList<Vertex> optionalAgentPositions = new ArrayList<Vertex>();
        optionalAgentPositions.addAll(destinationMap.get(2));
        optionalAgentPositions.removeAll(agentPositions);

        // calculate zones
        double zoneValue = 0.0;
        ArrayList<Vertex> zoneVertices = new ArrayList<Vertex>();
        zoneVertices.addAll(oneHops);
        zoneVertices.addAll(agentPositions);
        // calculate initial zone value
        for (Vertex vertex : zoneVertices) {
            zoneValue += vertex.getValue();
        }
        zoneValue /= agentPositions.size();
        Zone zone = new Zone();
        zone.setZoneValue(zoneValue);
        zone.setPositions(agentPositions);
        this.zones.put(agentPositions.size(), zone);

        TreeMap<Integer, Vertex> optionalAgentsWithValues = new TreeMap<Integer, Vertex>();
        for (Vertex optionalAgentPosition : optionalAgentPositions) {
            optionalAgentsWithValues.put(zoneValueMap.get(optionalAgentPosition), optionalAgentPosition);
        }
        while (optionalAgentsWithValues.size() > 0) {
            Entry<Integer, Vertex> bestEntry = optionalAgentsWithValues.pollLastEntry();
            if (bestEntry.getKey() > zone.getZoneValue()) {
                ArrayList<Vertex> positions = zone.getPositions();
                positions.add(bestEntry.getValue());
                double newZoneValue = ((zone.getZoneValue() * (positions.size() - 1)) + bestEntry.getKey()) / positions.size();
                zone = new Zone();
                zone.setPositions(positions);
                zone.setZoneValue(newZoneValue);
                this.zones.put(positions.size(), zone);
            }
        }
    }

    public ArrayList<Vertex> buildBridge(ArrayList<Vertex> vertices) {
        ArrayList<Vertex> myOneHops = destinationMap.get(1);
        myOneHops.retainAll(vertices);
        ArrayList<Vertex> bridgeVertices = new ArrayList<Vertex>();
        if (myOneHops.size() >= 2) {
            bridgeVertices.add(myOneHops.get(0));
            bridgeVertices.add(myOneHops.get(1));
        }
        return bridgeVertices;
    }

    /**
     * Given a list of vertices, return the number of those vertices that this
     * node is connected to in two steps.
     * 
     * @param vertices
     *            the list of vertices to check for connectivity from this node
     * @return the number of neighbours in the given vertex list
     */
    public boolean connectedWithVerticesInTwoHops(ArrayList<Vertex> vertices,
            ArrayList<Vertex> agentPositions) {
        ArrayList<Vertex> myOneHops = destinationMap.get(1);
        myOneHops.retainAll(vertices); // blue to cyan
        for (Vertex vertex : myOneHops) {
            if (vertex.connectedWithVerticesInOneHop(agentPositions) >= 1) {
                return true;
            }
        }
        return false;
    }

    /**
     * Given a list of vertices, return the number of those vertices that this
     * node has direct edges to.
     * 
     * @param vertices
     *            the list of vertices to check for connectivity from this node
     * @return the number of neighbours in the given vertex list
     */
    public int connectedWithVerticesInOneHop(ArrayList<Vertex> vertices) {
        ArrayList<Vertex> myOneHops = destinationMap.get(1);
        myOneHops.retainAll(vertices);
        return myOneHops.size();
    }

    /**
     * @return True if the node has been probed before, false otherwise.
     */
    public boolean isProbed() {
        return value != 0;
    }

    /**
     * @return True if all adjacent edges of the node have been surveyed.
     */
    public boolean isSurveyed() {
        for (Vertex neighbour : this.destinationMap.get(1)) {
            if (pathMap.get(neighbour).getPathCosts() < 11) {
                return false;
            }
        }
        return true;
    }

    /**
     * Finds a list of unsurveyed vertices of distance {@code hop} or greater.
     * Used for exploring.
     * 
     * @param hop
     *            The hop distance to start looking for unsurveyed vertices.
     * @return A list of vertices (empty if no unsurveyed vertices can be
     *         reached from this node).
     */
    public HashMap<Vertex, Integer> getNextUnsurveyedVertices(int hop) {
        HashMap<Vertex, Integer> unsurveyedVertices = new HashMap<Vertex, Integer>();
        if (!this.destinationMap.containsKey(hop)) {
            return unsurveyedVertices;
        }
        for (Vertex vertex : this.destinationMap.get(hop)) {
            int weight = pathMap.get(vertex).getPathCosts();
            unsurveyedVertices.put(vertex, weight);
        }
        if (unsurveyedVertices.size() > 0) {
            return unsurveyedVertices;
        } else {
            return getNextUnsurveyedVertices(hop + 1);
        }
    }

    /**
     * Tells the vertex about the existence of a neighbour node.
     * 
     * @param neighbour
     *            the vertex to be added to the list of neighbours
     * @param edgeWeight
     *            the weight of the edge to the node (1-10, or 11 if unsurveyed)
     */
    public void setNeighbour(Vertex neighbour, int edgeWeight) {
        Path path = handlePath(neighbour);
        path.setPathHops(1, neighbour);
        path.setPathCosts(edgeWeight, neighbour);
        addDestination(1, neighbour);
        informNeighboursAboutPath(path);
    }

    private void informNeighboursAboutPath(Path path) {
        for (Vertex neighbour : this.destinationMap.get(1)) {
            neighbour.setPath(path, this);
        }
    }

    /**
     * Force the vertex to recalculate a path if told to do so by one of its
     * neighbours.
     * 
     * @param path
     *            the path to check for improved hops/costs
     * @param sender
     *            the node telling this node about the new path
     */
    public void setPath(Path path, Vertex sender) {
        Path newPath = handlePath(path.getDestination());
        int newCosts = path.getPathCosts() + pathMap.get(sender).getPathCosts();
        int newHops = path.getPathHops() + pathMap.get(sender).getPathHops();
        boolean hasBetterCosts = newPath.setPathCosts(newCosts, sender);
        boolean hasBetterHops = newPath.setPathHops(newHops, sender);
        if (hasBetterHops) {
            addDestination(newHops, sender);
        }
        if (hasBetterCosts || hasBetterHops) {
            informNeighboursAboutPath(newPath);
        }
    }

    private void addDestination(int hops, Vertex destination) {
        // first remove existing vertices
        for (int key : destinationMap.keySet()) {
            if (destinationMap.get(key).contains(destination)) {
                destinationMap.get(key).remove(destination);
                break;
            }
        }
        // then add vertex
        if (!destinationMap.containsKey(hops)) {
            destinationMap.put(hops, new ArrayList<Vertex>());
        }
        destinationMap.get(hops).add(destination);
        if (hops <= 2) {
            destination.setZoneNodeValue(this, this.getValue());
        }
    }

    private Path handlePath(Vertex destination) {
        Path path;
        if (!pathMap.containsKey(destination)) {
            path = new Path(destination);
            pathMap.put(destination, path);
        } else {
            path = pathMap.get(destination);
        }
        return path;
    }
}
