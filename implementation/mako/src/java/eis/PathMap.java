package eis;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;

public class PathMap {

    // private AgentLogger logger;
    private Vertex position;
    private Hashtable<Vertex, Path> knownPaths = new Hashtable<Vertex, Path>();
    // saves the edge costs to the direct neighbours
    private Hashtable<Vertex, Integer> edgeCosts = new Hashtable<Vertex, Integer>();
    private Hashtable<Vertex, Integer> hopMapping = new Hashtable<Vertex, Integer>();
    private Hashtable<Integer, HashSet<Vertex>> hopPaths = new Hashtable<Integer, HashSet<Vertex>>();

    public PathMap(Vertex vertex) {
        this.setPosition(vertex);
        // logger = new AgentLogger(vertex + " PathMap");
    }

    /**
     * The handlePath method stores new paths in the hash table of the PathMap.
     * If the destination of a path is already known, the method updates the
     * weights and the costs of the path.
     */
    public boolean handlePath(Path newPath) {
        Vertex destination = newPath.getDestination();
        // don't store paths that lead to ourselves
        if (destination == getPosition())
            return false;
        if (knownPaths.containsKey(destination)) {
            Path currentPath = knownPaths.get(destination);
            boolean hasHopsChanged = currentPath.setPathHops(newPath.getPathHops(), newPath.getNextHopVertex());
            boolean hasCostsChanged = currentPath.setPathCosts(newPath.getPathCosts(), newPath.getNextBestCostVertex());
            if (hasHopsChanged) {
                updateHopPaths(newPath);
            }
            return hasHopsChanged || hasCostsChanged;

        } else {
            knownPaths.put(destination, newPath);
            updateHopPaths(newPath);
            return true;
        }
    }

    /**
     * The updateHopPaths method updates the mappings of hops to paths. In this
     * method we store how far in hops every reachable destination is and which
     * path has to be chosen.
     */
    private void updateHopPaths(Path path) {
        int currentHop = 0;
        if (hopMapping.containsKey(path.getDestination())) {
            currentHop = hopMapping.get(path.getDestination());
        }
        hopMapping.put(path.getDestination(), path.getPathHops());

        if (currentHop != 0 && hopPaths.containsKey(currentHop)) {
            hopPaths.get(currentHop).remove(path.getDestination());
        }
        if (!hopPaths.containsKey(path.getPathHops())) {
            hopPaths.put(path.getPathHops(), new HashSet<Vertex>());
        }
        hopPaths.get(path.getPathHops()).add(path.getDestination());
    }

    public ArrayList<Vertex> getNeighbours() {
        ArrayList<Vertex> neighbours = getVerticesWithHop(1);
        return neighbours;
    }

    /**
     * @param vertex
     *            the vertex to return the path to
     * @return the path to the given vertex (null if not known)
     */
    public Path getPath(Vertex vertex) {
        return knownPaths.get(vertex);
    }

    /**
     * @param hop
     * @return true if we know about a connected node that is hop steps away
     */
    public boolean containsPathsWithHop(int hop) {
        return hopPaths.get(hop) != null && hopPaths.get(hop).size() > 0;
    }

    public ArrayList<Vertex> getVerticesWithHop(int hop) {
        ArrayList<Vertex> vertices = new ArrayList<Vertex>();
        if (this.containsPathsWithHop(hop)) {
            vertices.addAll(hopPaths.get(hop));
        }
        return vertices;
    }

    /**
     * Like getVerticesWithHop, but returns all vertices within the given hop
     * distance. E.g. if distance == 3, returns all vertices reachable within 1,
     * 2 or 3 steps.
     * 
     * @param distance
     *            the maximum search range
     * @return a list of vertices reachable within the given distance from the
     *         given vertex
     */
    public ArrayList<Vertex> getVerticesUpToDistance(int distance) {
        ArrayList<Vertex> vertices = new ArrayList<Vertex>();
        for (int i = 1; i <= distance; i++) {
            vertices.addAll(getVerticesWithHop(i));
        }
        return vertices;
    }

    public Vertex getPosition() {
        return position;
    }

    public void setPosition(Vertex position) {
        this.position = position;
    }

    public ArrayList<Path> getAllPaths() {
        return new ArrayList<Path>(knownPaths.values());
    }

    /**
     * Adds an edge to a direct neighbour of this node with its associated
     * weight.
     * 
     * @param neighbour
     *            the neighbour node
     * @param edgeWeight
     *            the weight of the edge to the neighbour node
     */
    public void addEdgeCost(Vertex neighbour, int edgeWeight) {
        // If we already know the edge weight, don't overwrite it. We need this
        // check because otherwise it will get overwritten with the maximum edge
        // value by Vertex.setNeighbour() (there's probably a better way to do
        // this).
        if (edgeCosts.containsKey(neighbour)) {
            Integer previousCosts = edgeCosts.get(neighbour);
            if (previousCosts <= edgeWeight) {
                return;
            }
        }
        edgeCosts.put(neighbour, edgeWeight);
    }

    public int getEdgeCosts(Vertex nextHop) {
        if (edgeCosts.containsKey(nextHop)) {
            return edgeCosts.get(nextHop);
        } else {
            return -1;
        }
    }

}
