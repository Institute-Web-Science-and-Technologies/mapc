package eis;

import java.util.ArrayList;
import java.util.TreeMap;

/**
 * Handles values and methods related to nodes.
 * 
 * @author Artur Daudrich
 * @author Michael Sewell
 * 
 */
public class Vertex {
    private AgentLogger logger;
    // The 'name' of the vertex, e.g. 'v81'
    private String identifier = "";
    // the team currently occupying the vertex, e.g. 'teamA'
    private String team = "none";
    // the node value, which is 1 to 10 after probing
    // '0' in this case is a bit of a hack to designate that the node has not
    // been probed
    private int value = 0;

    private PathMap knownPaths;
    private ZoneMap zoneMap;

    private boolean reservedForProbing = false;
    private boolean reservedForSurveying = false;

    /**
     * @param identifier
     *            The name of the vertex, e.g. 'v31'.
     */
    public Vertex(String identifier) {
        this.identifier = identifier;
        this.knownPaths = new PathMap(this);
        this.zoneMap = new ZoneMap(knownPaths);
        logger = new AgentLogger(identifier);
        logger.setVisible(true);
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
            // logger.info("Now occupied by " + team);
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
            zoneMap.calculateZoneValue();
            informZoneAboutNodeValue();
        }
    }

    private void informZoneAboutNodeValue() {
        ArrayList<Vertex> zoneVertices = zoneMap.getZoneVertices();
        logger.info("Informing the nodes in my zone (" + zoneVertices + ") about my node value (" + this.value + ")");
        for (Vertex vertex : zoneVertices) {
            vertex.calculateZone();
        }
    }

    private void calculateZone() {
        this.zoneMap.calculateZoneValue();
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
        for (Vertex neighbour : knownPaths.getNeighbours()) {
            if (knownPaths.getPath(neighbour).getPathCosts() > 10) {
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
    public TreeMap<Integer, Vertex> getNextUnsurveyedVertices(int hop) {
        TreeMap<Integer, Vertex> unsurveyedVertices = new TreeMap<Integer, Vertex>();
        if (!knownPaths.containsPathsWithHop(hop)) {
            return unsurveyedVertices;
        }
        for (Vertex vertex : knownPaths.getVerticesWithHop(hop)) {
            if (!vertex.isSurveyed() & !vertex.isReservedForSurveying()) {
                int pathCosts = knownPaths.getPath(vertex).getPathCosts();
                unsurveyedVertices.put(pathCosts, vertex);
            }
        }
        if (unsurveyedVertices.size() > 0) {
            return unsurveyedVertices;
        } else {
            return getNextUnsurveyedVertices(hop + 1);
        }
    }

    /**
     * Returns the nearest unprobed vertex. If there is more than one unprobed
     * vertex with minimal hop distance, returns from those vertices the vertex
     * with the highest number of connected probed vertices. If there is still a
     * tie, return the vertex with the lowest path costs.
     * 
     * @param int the hop distance to start the search for unprobed vertices
     *        from. This gets incremented recursively if no unprobed vertices
     *        are found.
     * @return a nearby unprobed vertex
     */
    public TreeMap<Integer, Vertex> getNextUnprobedVertices(int hop) {
        TreeMap<Integer, Vertex> unprobedVertices = new TreeMap<Integer, Vertex>();
        ArrayList<Vertex> vertices = new ArrayList<Vertex>();

        // if there are no paths return an empty list
        if (!knownPaths.containsPathsWithHop(hop)) {
            return unprobedVertices;
        }
        // find all unprobed and unreserved vertices
        for (Vertex vertex : knownPaths.getVerticesWithHop(hop)) {
            if (!vertex.isProbed() & !vertex.isReservedForProbing()) {
                vertices.add(vertex);
            }
        }

        // select only the vertex with most probed neighbours
        int probedNeighbours = 0;
        ArrayList<Vertex> verticesWithProbedNeighbours = new ArrayList<Vertex>();
        for (Vertex vertex : vertices) {
            // count the probed neighbours of the vertex
            int count = 0;
            for (Vertex neighbour : vertex.getNeighbours()) {
                if (neighbour.isProbed()) {
                    count++;
                }
            }
            if (probedNeighbours == count) {
                verticesWithProbedNeighbours.add(vertex);
            }
            if (probedNeighbours < count) {
                probedNeighbours = count;
                verticesWithProbedNeighbours.clear();
                verticesWithProbedNeighbours.add(vertex);
            }
        }

        // insert the nearest unprobed vertices with most already probed
        // neighbours and sort them by path costs
        for (Vertex vertex : verticesWithProbedNeighbours) {
            int pathCosts = knownPaths.getPath(vertex).getPathCosts();
            unprobedVertices.put(pathCosts, vertex);
        }

        if (unprobedVertices.size() > 0) {
            return unprobedVertices;
        } else {
            return getNextUnprobedVertices(hop + 1);
        }
    }

    /**
     * Tells the vertex about the existence of a neighbour node. If the vertex
     * already knows about this neighbour, it will only update the information
     * about the edge weight to its neighbour if the edgeWeight parameter is
     * smaller than the stored edge weight.
     * 
     * @param neighbour
     *            the vertex to be added to the list of neighbours
     * @param edgeWeight
     *            the weight of the edge to the node (1-10, or 11 if unsurveyed)
     */
    public void setNeighbour(Vertex neighbour, int edgeWeight) {
        Path newPath = new Path(neighbour);
        newPath.setPathHops(1, neighbour);
        newPath.setPathCosts(edgeWeight, neighbour);
        boolean changed = knownPaths.handlePath(newPath);
        if (changed) {
            // logger.info("New or better path (setNeighbour): " + this +
            // newPath);
            informNeighboursAboutPath(newPath);
            informNeighbourAboutPaths(neighbour);
        }
    }

    private void informNeighbourAboutPaths(Vertex neighbour) {
        for (Path path : knownPaths.getAllPaths()) {
            if (path.getDestination() != neighbour) {
                neighbour.setPath(path, this);
            }
        }
    }

    private void informNeighboursAboutPath(Path path) {
        ArrayList<Vertex> neighbours = this.getNeighbours();
        for (Vertex neighbour : neighbours) {
            neighbour.setPath(path, this);
        }
    }

    /**
     * Force the vertex to recalculate a path if told to do so by one of its
     * neighbours.
     * 
     * @param senderToDestination
     *            the path to check for improved hops/costs
     * @param sender
     *            the node telling this node about the new path
     */
    public void setPath(Path senderToDestination, Vertex sender) {
        // logger.info("Received path " + sender + senderToDestination);
        Vertex destination = senderToDestination.getDestination();
        if (destination == this) {
            return;
        }
        // call setNeighbour to guarantee that a path to the sender exists
        setNeighbour(sender, 11);
        Path hereToSender = knownPaths.getPath(sender);
        int costs = senderToDestination.getPathCosts() + hereToSender.getPathCosts();
        int hops = senderToDestination.getPathHops() + hereToSender.getPathHops();
        Path newPath = new Path(destination, costs, sender, hops, sender);
        boolean changed = knownPaths.handlePath(newPath);
        if (changed) {
            Path realNewPath = knownPaths.getPath(destination);
            // logger.info("SenderToDestinationHops: " +
            // senderToDestination.getPathHops() + " - hereToSenderHops: " +
            // hereToSender.getPathHops());
            // logger.info("New or better path: (setPath)" + this +
            // realNewPath);
            informNeighboursAboutPath(realNewPath);
        }
    }

    public String toString() {
        return this.identifier;
    }

    public ArrayList<Vertex> getNeighbours() {
        return this.knownPaths.getNeighbours();
    }

    public Path getPath(Vertex destination) {
        return knownPaths.getPath(destination);
    }

    public ArrayList<Vertex> getNeighbourhood(int range) {
        return knownPaths.getVerticesUpToDistance(range);
    }

    public Zone getBestMinimalZone() {
        return zoneMap.getBestMinimalZone();
    }

    public ZoneMap getZoneMap() {
        return zoneMap;
    }

    public boolean isReservedForProbing() {
        return reservedForProbing;
    }

    public void setReservedForProbing(boolean reservedForProbing) {
        this.reservedForProbing = reservedForProbing;
    }

    public boolean isReservedForSurveying() {
        return reservedForSurveying;
    }

    public void setReservedForSurveying(boolean reservedForSurveying) {
        this.reservedForSurveying = reservedForSurveying;
    }

}
