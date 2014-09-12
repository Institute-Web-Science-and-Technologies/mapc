package eis;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.TreeMap;

public class ZoneMap {
    private AgentLogger logger;
    private PathMap pathMap;
    private ArrayList<Vertex> agentPositions;
    private ArrayList<Vertex> optionalAgentPositions;

    // the number of agents required to build a zone, and the zones themselves
    private TreeMap<Integer, Zone> zones = new TreeMap<Integer, Zone>();

    public ZoneMap(PathMap pathMap) {
        this.pathMap = pathMap;
        logger = new AgentLogger(pathMap.getPosition() + " ZoneMap");
        logger.setVisible(false); // set to true for debug output
    }

    public void calculateZoneValue() {
        logger.info("Entering zone calculation");
        // calculate agent positions
        calculateAgentPositions();
        // calculate zones
        calculateZones();
    }

    private void calculateAgentPositions() {
        agentPositions = new ArrayList<Vertex>();

        // Find all potential members for building a zone from this center
        // vertex.
        ArrayList<Vertex> neighbours = pathMap.getNeighbours();
        ArrayList<Vertex> twoHops = pathMap.getVerticesWithHop(2);
        logger.info("Neighbours: " + neighbours + ". Initial Two-Hops: " + twoHops);
        if (twoHops.size() == 0 && neighbours.size() == 0) {
            logger.info("I don't know about any neighbours, aborting zone calculation");
            return;
        }

        // Find all vertices which are connected to at least 2 one-hop
        // neighbours. Remove them from the two-hop list and add them to the
        // agent position list.
        for (int i = twoHops.size() - 1; i >= 0; i--) {
            Vertex vertex = twoHops.get(i);
            if (connectedWithVerticesInOneHop(vertex, neighbours) >= 2) {
                twoHops.remove(vertex);
                agentPositions.add(vertex);
            }
        }
        logger.info("1) Agent Positions: " + agentPositions);
        logger.info("1) Remaining Two-Hops: " + twoHops);

        // Remove all vertices from the two-hop list, which are connected
        // directly or indirectly (via a one-hop) to the center vertex.
        for (int i = twoHops.size() - 1; i >= 0; i--) {
            Vertex vertex = twoHops.get(i);
            // directly connected
            if (connectedWithVerticesInOneHop(vertex, agentPositions) >= 1) {
                twoHops.remove(vertex);
                continue;
            }
            // indirectly connected (via one-hop)
            if (connectedWithVerticesInTwoHops(vertex, neighbours, agentPositions)) {
                twoHops.remove(vertex);
            }
        }
        logger.info("2) Remaining Two-Hops: " + twoHops);

        // Add all two-hops to the agent position list, which are forming a
        // bridge over an other two-hop.
        // Remove the vertex which is under the bridge from the two-hop list.
        for (int i = twoHops.size() - 1; i >= 0; i--) {
            Vertex vertex = twoHops.get(i);
            ArrayList<Vertex> bridge = buildBridge(vertex, twoHops);
            if (bridge.size() == 2) {
                twoHops.remove(vertex);
                agentPositions.add(bridge.get(0));
                agentPositions.add(bridge.get(1));
            }
        }

        // Add center vertex to the list of agent positions
        agentPositions.add(pathMap.getPosition());
        logger.info("3) Agent Positions: " + agentPositions);
        logger.info("3) Remaining Two-Hops: " + twoHops);

        // Check every one-hop neighbour if it is connected to at least 1 agent
        // positions and the center vertex. If not add the two-hop with highest
        // value, which connects the one-hop to the zone, to the agent position
        // list. If there exist no two-hop mark the one-hop as optional agent
        // position.
        optionalAgentPositions = new ArrayList<Vertex>();
        for (Vertex oneHop : neighbours) {
            ArrayList<Vertex> oneHopNeighbours = oneHop.getNeighbours();
            oneHopNeighbours.retainAll(agentPositions);
            if (oneHopNeighbours.size() == 1) {
                oneHopNeighbours = oneHop.getNeighbours();
                oneHopNeighbours.retainAll(pathMap.getVerticesWithHop(2));
                TreeMap<Integer, Vertex> oneHopNeighboursWithNodeValues = new TreeMap<Integer, Vertex>();
                for (Vertex oneHopNeighbour : oneHopNeighbours) {
                    if (oneHopNeighbour != pathMap.getPosition()) {
                        oneHopNeighboursWithNodeValues.put(oneHopNeighbour.getValue(), oneHopNeighbour);
                    }
                }
                if (oneHopNeighboursWithNodeValues.size() > 0) {
                    Vertex possibleDisconnectedNode = oneHopNeighboursWithNodeValues.lastEntry().getValue();
                    agentPositions.add(possibleDisconnectedNode);
                    logger.info("4) new agent position: " + possibleDisconnectedNode + " connects: " + oneHop);
                } else {
                    optionalAgentPositions.add(oneHop);
                    logger.info("4) new optional agent position: " + oneHop);
                }
            }
        }
        logger.info("4) Agent Positions: " + agentPositions);

        // All two-hops which are not in the agent position list, become
        // optional positions.
        optionalAgentPositions.addAll(pathMap.getVerticesWithHop(2));
        optionalAgentPositions.removeAll(agentPositions);
        logger.info("5) Optional Agent Positions: " + optionalAgentPositions);
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
        logger.info("Minimal Zone Value per agent is: " + zoneValue);

        // create zone
        Zone zone = new Zone(pathMap.getPosition());
        zone.setZoneValue(zoneValue);
        zone.setPositions(agentPositions);
        zone.setZonePointVertices(zonePointVertices);
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
                // add next best optimal position to agent positions of new zone
                ArrayList<Vertex> positions = zone.getPositions();
                positions.add(bestEntry.getValue());

                // add next best optimal position to zone point vertices of new
                // zone
                ArrayList<Vertex> newZonePointVertices = zone.getZonePointVertices();
                newZonePointVertices.add(bestEntry.getValue());

                // calculate new zone value of new zone
                double newZoneValue = ((zone.getZoneValue() * (positions.size() - 1)) + bestEntry.getKey()) / positions.size();

                // create new zone and assign values
                zone = new Zone(pathMap.getPosition());
                zone.setPositions(positions);
                zone.setZoneValue(newZoneValue);
                zone.setZonePointVertices(newZonePointVertices);
                this.zones.put(positions.size(), zone);
                logger.info("Optional Zone Value:" + newZoneValue + ". Positions: " + positions);
            }
        }
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

    /**
     * Retrieves the first (sorted by agent count) howMany zones for this
     * vertex. If less zones are available than requested, return the entire
     * zone list.
     * 
     * @param howMany
     *            up to how many zones to return
     * @return the list of zones, or an empty list if there are no zones
     */
    public TreeMap<Integer, Zone> getZones(int howMany) {
        TreeMap<Integer, Zone> zoneList = new TreeMap<Integer, Zone>();
        if (!zones.isEmpty()) {
            int count = 0;
            int currentKey = zones.firstKey();
            while (count < howMany && count < zoneList.size() - 1) {
                zoneList.put(currentKey, zones.get(currentKey));
                currentKey = zones.higherKey(currentKey);
                count++;
            }
        }
        return zoneList;
    }

    /**
     * @return best minimal zone; can be {@code null}.
     */
    public Zone getBestMinimalZone() {
        if (zones.size() == 0) {
            return null;
        } else {
            return zones.firstEntry().getValue();
        }
    }

    /**
     * @param currentSize
     *            the size (number of agents used) of the current zone
     * @return the vertex that an agent should be placed on to optimally
     *         increase the size of the zone by one agent
     */
    public Vertex getNextAgentPosition(int currentSize) {
        if (!zones.containsKey(currentSize + 1)) {
            return null;
        } else {
            Zone biggerZone = zones.get(currentSize + 1);
            // this only works if the agent position we're looking for is
            // actually the last vertex stored in the list of agent positions
            // for the zone
            Vertex agentPosition = biggerZone.getPositions().remove(biggerZone.getPositions().size() - 1);
            return agentPosition;
        }
    }

}
