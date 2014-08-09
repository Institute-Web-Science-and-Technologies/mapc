package eis;

import java.util.HashMap;
import java.util.HashSet;

public class Vertex {

    private final int hop1 = 1;
    private final int hop2 = 2;

    private String name = "";
    private String team = "none";
    private int value = 0;

    private HashSet<Vertex> neighbours = new HashSet<Vertex>();
    private HashSet<Vertex> twoHopNeighbours = new HashSet<Vertex>();
    private HashMap<Vertex, Path> pathMap = new HashMap<Vertex, Path>();

    public Vertex(String vertexName) {
        this.name = vertexName;
    }

    public String getName() {
        return this.name;
    }

    public void setTeam(String team) {
        if (!this.team.equalsIgnoreCase(team)) {
            this.team = team;
        }
    }

    public int getValue() {
        if (this.value == 0) {
            return 1;
        }
        return this.value;
    }

    public void setValue(int value) {
        if (this.value == 0) {
            this.value = value;
            calculateZoneValue();
        }
    }

    private void calculateZoneValue() {
        // TODO Auto-generated method stub

    }

    public boolean isProbed() {
        return value != 0;
    }

    public void setNeighbour(Vertex neighbour, int edgeWeight) {
        neighbours.add(neighbour);
        Path path = handlePath(neighbour);
        path.setPathHops(hop1, neighbour);
        path.setPathCosts(edgeWeight, neighbour);
        informNeighboursAboutPath(path);
    }

    private void informNeighboursAboutPath(Path path) {
        for (Vertex neighbour : neighbours) {
            neighbour.setPath(path, this);
        }
    }

    public void setPath(Path path, Vertex sender) {
        Path newPath = handlePath(path.getDestination());
        int newCosts = path.getPathCosts() + pathMap.get(sender).getPathCosts();
        int newHops = path.getPathHops() + pathMap.get(sender).getPathHops();
        boolean changed = newPath.setPathCosts(newCosts, sender);
        changed |= newPath.setPathHops(newHops, sender);
        if (changed) {
            informNeighboursAboutPath(newPath);
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
