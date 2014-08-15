package eis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TreeMap;

public class ZoneMap {
    private AgentLogger logger;
    private PathMap pathMap;
    private ArrayList<Vertex> agentPositions;
    private ArrayList<Vertex> optionalAgentPositions;

    // the number of agents required to build a zone, and the zones themselves
    private HashMap<Integer, Zone> zones = new HashMap<Integer, Zone>();

    public ZoneMap(PathMap pathMap) {
        this.pathMap = pathMap;
        logger = new AgentLogger("PathMap - " + pathMap.getPosition().getIdentifier());
    }

    public void calculateZoneValue() {
        logger.info("Entering zone calculation");
        // calculate agent positions
        calculateAgentPositions();
        // calculate zones
        calculateZones();
    }

    private void calculateZones() {
        ArrayList<Vertex> zonePointVertices = new ArrayList<Vertex>();
        zonePointVertices.addAll(getZoneVertices());
        zonePointVertices.removeAll(optionalAgentPositions);
        logger.info("Zone Point Vertices: " + zonePointVertices);

        // calculate minimal zone value
        double zoneValue = 0.0;
        for (Vertex vertex : zonePointVertices) {
            zoneValue += vertex.getValue();
        }
        zoneValue /= agentPositions.size();
        logger.info("Minimal Zone Value is: " + zoneValue);

        // create zone
        Zone zone = new Zone();
        zone.setZoneValue(zoneValue);
        zone.setPositions(agentPositions);
        this.zones.put(agentPositions.size(), zone);

        // calculate additional zones
        TreeMap<Integer, Vertex> optionalAgentsWithValues = new TreeMap<Integer, Vertex>();
        for (Vertex optionalAgentPosition : optionalAgentPositions) {
            optionalAgentsWithValues.put(optionalAgentPosition.getValue(), optionalAgentPosition);
        }
        logger.info("Optional Agents with values: " + optionalAgentsWithValues);
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
                logger.info("Optional Zone Value:" + newZoneValue + ". Positions: " + positions);
            }
        }
    }

    private void calculateAgentPositions() {
        agentPositions = new ArrayList<Vertex>();
        agentPositions.add(pathMap.getPosition());

        ArrayList<Vertex> neighbours = pathMap.getNeighbours();
        ArrayList<Vertex> twoHops = pathMap.getVerticesWithHop(2);
        logger.info("Neighbours: " + neighbours + ". Initial Two-Hops: " + twoHops);

        if (twoHops.size() == 0 && neighbours.size() == 0) {
            logger.info("I don't know about any neighbours, aborting zone calculation");
            return;
        }

        for (int i = twoHops.size() - 1; i > 0; i--) {
            Vertex vertex = twoHops.get(i);
            if (connectedWithVerticesInOneHop(vertex, neighbours) >= 2) {
                twoHops.remove(vertex);
                agentPositions.add(vertex);
            }
        }
        logger.info("1) Agent Positions: " + agentPositions);
        logger.info("1) Remaining Two-Hops: " + twoHops);

        for (int i = twoHops.size() - 1; i > 0; i--) {
            Vertex vertex = twoHops.get(i);
            if (connectedWithVerticesInOneHop(vertex, agentPositions) >= 1) {
                twoHops.remove(vertex);
                continue;
            }
            if (connectedWithVerticesInTwoHops(vertex, neighbours, agentPositions)) {
                twoHops.remove(vertex);
            }
        }
        logger.info("2) Remaining Two-Hops: " + twoHops);

        for (int i = twoHops.size() - 1; i > 0; i--) {
            Vertex vertex = twoHops.get(i);
            ArrayList<Vertex> bridge = buildBridge(vertex, twoHops);
            if (bridge.size() == 2) {
                twoHops.remove(vertex);
                agentPositions.add(bridge.get(0));
                agentPositions.add(bridge.get(1));
            }
        }
        logger.info("3) Agent Positions: " + agentPositions);
        logger.info("3) Remaining Two-Hops: " + twoHops);

        optionalAgentPositions = new ArrayList<Vertex>();
        optionalAgentPositions.addAll(pathMap.getVerticesWithHop(2));
        optionalAgentPositions.removeAll(agentPositions);
        logger.info("4) Optional Agent Positions: " + optionalAgentPositions);
    }

    public ArrayList<Vertex> buildBridge(Vertex vertex,
            ArrayList<Vertex> vertices) {
        ArrayList<Vertex> myOneHops = vertex.getNeighbours();
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
    public boolean connectedWithVerticesInTwoHops(Vertex vertex,
            ArrayList<Vertex> vertices, ArrayList<Vertex> agentPositions) {
        ArrayList<Vertex> myOneHops = vertex.getNeighbours();
        myOneHops.retainAll(vertices); // blue to cyan
        for (Vertex v : myOneHops) {
            if (connectedWithVerticesInOneHop(v, agentPositions) >= 1) {
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
    public int connectedWithVerticesInOneHop(Vertex vertex,
            ArrayList<Vertex> vertices) {
        ArrayList<Vertex> myOneHops = vertex.getNeighbours();
        myOneHops.retainAll(vertices);
        return myOneHops.size();
    }

    public ArrayList<Vertex> getZoneVertices() {
        ArrayList<Vertex> zoneVertices = new ArrayList<Vertex>();
        zoneVertices.addAll(pathMap.getNeighbours());
        zoneVertices.addAll(pathMap.getVerticesWithHop(2));
        logger.info("ZoneVertices: " + zoneVertices);
        return zoneVertices;
    }
}
