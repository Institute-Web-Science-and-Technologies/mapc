package eis;

import java.util.HashSet;

public class Zone {
    private double zoneValue = 1.0;
    private HashSet<Vertex> positions = new HashSet<Vertex>();
    private HashSet<Vertex> zoneNodes = new HashSet<Vertex>();
    private Vertex center;

    public Zone(Vertex center) {
        this.center = center;
        positions.add(center);
    }

    public double getZoneValue() {
        return zoneValue;
    }

    public void setZoneValue(double zoneValue) {
        this.zoneValue = zoneValue;
    }

    @SuppressWarnings("unchecked")
    public HashSet<Vertex> getPositions() {
        return (HashSet<Vertex>) positions.clone();
    }

    public void setPositions(HashSet<Vertex> positions) {
        this.positions = positions;
    }

    public double getZoneValuePerAgent() {
        if (positions.size() <= 1) {
            return 0;
        } else {
            return zoneValue / positions.size();
        }
    }

    public Vertex getCenter() {
        return center;
    }

    @SuppressWarnings("unchecked")
    public HashSet<Vertex> getZoneNodes() {
        return (HashSet<Vertex>) zoneNodes.clone();
    }

    public void setZoneNodes(HashSet<Vertex> zoneNodes) {
        this.zoneNodes = zoneNodes;
    }
}
