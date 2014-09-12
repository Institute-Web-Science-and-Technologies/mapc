package eis;

import java.util.ArrayList;

public class Zone {
    private double zoneValue = 1.0;
    private ArrayList<Vertex> positions = new ArrayList<Vertex>();
    private ArrayList<Vertex> zonePointVertices = new ArrayList<Vertex>();
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
    public ArrayList<Vertex> getPositions() {
        return (ArrayList<Vertex>) positions.clone();
    }

    public void setPositions(ArrayList<Vertex> positions) {
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
    public ArrayList<Vertex> getZonePointVertices() {
        return (ArrayList<Vertex>) zonePointVertices.clone();
    }

    public void setZonePointVertices(ArrayList<Vertex> zonePointVertices) {
        this.zonePointVertices = zonePointVertices;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Zone [" + (getCenter() != null ? "center=" + getCenter() + ", " : "") + (getPositions() != null ? "positions=" + getPositions() + ", " : "") + "zone value=" + getZoneValue() + ", zone value per agent=" + getZoneValuePerAgent() + "]";
    }
}
