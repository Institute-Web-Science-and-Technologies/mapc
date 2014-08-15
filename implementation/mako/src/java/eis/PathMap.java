package eis;

import java.util.ArrayList;
import java.util.HashMap;

public class PathMap {

    private AgentLogger logger;
    private Vertex position;
    private HashMap<Vertex, Path> paths = new HashMap<Vertex, Path>();
    private HashMap<Vertex, Integer> hopMapping = new HashMap<Vertex, Integer>();
    private HashMap<Integer, ArrayList<Vertex>> hopPaths = new HashMap<Integer, ArrayList<Vertex>>();

    public PathMap(Vertex vertex) {
        this.setPosition(vertex);
        logger = new AgentLogger("PathMap - " + vertex.getIdentifier());
    }

    public boolean handlePath(Path path) {
        Vertex destination = path.getDestination();
        if (destination == getPosition())
            return false;
        if (paths.containsKey(destination)) {
            Path currentPath = paths.get(destination);
            boolean hasHopsChanged = currentPath.setPathHops(path.getPathHops(), path.getNextHopVertex());
            boolean hasCostsChanged = currentPath.setPathCosts(path.getPathCosts(), path.getNextBestCostVertex());
            if (hasHopsChanged) {
                updateHopPaths(path);
            }

            if (hasHopsChanged || hasCostsChanged) {
                logger.info("Updated path: " + this.position + path);
            }

            // if (hasCostsChanged) {
            // updateCostPaths(path);
            // }
            return hasHopsChanged || hasCostsChanged;

        } else {
            paths.put(destination, path);
            updateHopPaths(path);
            logger.info("New path: " + this.position + path);
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

    public Path getPathToVertex(Vertex vertex) {
        return paths.get(vertex);
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

    public Vertex getPosition() {
        return position;
    }

    public void setPosition(Vertex position) {
        this.position = position;
    }

}
