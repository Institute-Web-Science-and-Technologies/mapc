package eis;

import java.util.ArrayList;
import java.util.HashMap;

public class PathMap {

    private AgentLogger logger;
    private Vertex position;
    private HashMap<Vertex, Path> knownPaths = new HashMap<Vertex, Path>();
    private HashMap<Vertex, Integer> hopMapping = new HashMap<Vertex, Integer>();
    private HashMap<Integer, ArrayList<Vertex>> hopPaths = new HashMap<Integer, ArrayList<Vertex>>();

    public PathMap(Vertex vertex) {
        this.setPosition(vertex);
        logger = new AgentLogger("PathMap " + vertex);
    }

    public boolean handlePath(Path newPath) {
        Vertex destination = newPath.getDestination();
        // don't store paths that lead to ourselves
        if (destination == getPosition())
            return false;
        if (knownPaths.containsKey(destination)) {
            Path currentPath = knownPaths.get(destination);
            String oldPathInfo = "" + this.position + currentPath;
            boolean hasHopsChanged = currentPath.setPathHops(newPath.getPathHops(), newPath.getNextHopVertex());
            boolean hasCostsChanged = currentPath.setPathCosts(newPath.getPathCosts(), newPath.getNextBestCostVertex());
            if (hasHopsChanged) {
                updateHopPaths(newPath);
            }

            if (hasHopsChanged || hasCostsChanged) {
                logger.info("Updated path: " + this.position + currentPath + " (was: " + oldPathInfo + ")");
            }

            // if (hasCostsChanged) {
            // updateCostPaths(path);
            // }
            return hasHopsChanged || hasCostsChanged;

        } else {
            knownPaths.put(destination, newPath);
            updateHopPaths(newPath);
            logger.info("New path: " + this.position + newPath);
            return true;
        }
    }

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
            hopPaths.put(path.getPathHops(), new ArrayList<Vertex>());
        }
        hopPaths.get(path.getPathHops()).add(path.getDestination());
    }

    public ArrayList<Vertex> getNeighbours() {
        return getVerticesWithHop(1);
    }

    /**
     * @param vertex
     *            the vertex to return the path to
     * @return the path to the given vertex
     */
    public Path getPath(Vertex vertex) {
        return knownPaths.get(vertex);
    }

    public boolean containsPathsWithHop(int hop) {
        return hopPaths.get(hop) != null && hopPaths.get(hop).size() > 0;
    }

    public ArrayList<Vertex> getVerticesWithHop(int hop) {
        ArrayList<Vertex> vertices = new ArrayList<Vertex>();
        if (this.containsPathsWithHop(hop)) {
            vertices.addAll(hopPaths.get(hop));
        }
        logger.info("Asked for vertices(" + hop + "). Result is: " + vertices);
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

}
