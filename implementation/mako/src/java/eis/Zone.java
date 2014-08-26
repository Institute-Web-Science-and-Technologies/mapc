package eis;

import java.util.ArrayList;

public class Zone {
    private double zoneValue = 1.0;
    private ArrayList<Vertex> positions = new ArrayList<Vertex>();

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

}
