package eis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;

import eis.iilang.Numeral;
import eis.iilang.Percept;

public class MapAgent {

    private static MapAgent mapAgent;

    private final int maxEdgeWeight = 11;
    private final int resetStep = 10;

    private int edges = 1;
    private int vertices = 1;
    private int step = 0;
    private AgentLogger logger = new AgentLogger("MapAgent");

    private HashMap<String, Vertex> vertexMap = new HashMap<String, Vertex>();
    private HashMap<String, Agent> agents = new HashMap<String, Agent>();

    private HashSet<String> visibleVertices = new HashSet<String>();
    private HashSet<String> probedVertices = new HashSet<String>();
    private HashSet<String> visibleEdges = new HashSet<String>();
    private HashSet<String> surveyedEdges = new HashSet<String>();

    private HashSet<Vertex> currentZoneVertices = new HashSet<Vertex>();
    private HashSet<Vertex> reservedUnsurveyedVertices = new HashSet<Vertex>();
    private HashSet<Vertex> reservedProbedVertices = new HashSet<Vertex>();

    public static MapAgent getInstance() {
        if (mapAgent == null) {
            mapAgent = new MapAgent();
        }
        return mapAgent;
    }

    /**
     * Adds an Agent object to the list of agents.
     * 
     * @param serverName
     *            the serverName of the agent, e.g. "a1"
     * @param agent
     *            the Agent object to add
     */
    public void addAgent(String serverName, Agent agent) {
        agents.put(serverName, agent);
    }

    private HashSet<Agent> getEnemyAgents() {
        HashSet<Agent> enemies = new HashSet<Agent>();
        for (Agent agent : agents.values()) {
            if (!agent.isInOurTeam()) {
                enemies.add(agent);
            }
        }
        return enemies;
    }

    private HashSet<Agent> getFriendlyAgents() {
        HashSet<Agent> result = new HashSet<Agent>();
        for (Agent agent : agents.values()) {
            if (agent.isInOurTeam()) {
                result.add(agent);
            }
        }
        return result;
    }

    /**
     * @param vertex
     *            the vertex to return the list of enemy-free neighbour vertices
     *            for
     * @return the neighbour vertices of the given vertex that have no enemy
     *         agent standing on them (possibly empty)
     */
    public ArrayList<Vertex> getSafeNeighbours(Vertex vertex) {
        ArrayList<Vertex> neighbours = vertex.getNeighbours();
        for (Agent enemy : getEnemyAgents()) {
            neighbours.remove(enemy.getPosition());
        }
        return neighbours;
    }

    public synchronized void addPercept(Percept percept) {
        switch (percept.getName()) {
        case "inspectedEntity":
            handleInspectedEntity(percept);
            break;
        case "visibleVertex":
            handleVisibleVertex(percept);
            visibleVertices.add(percept.getParameters().get(0).toString());
            break;
        case "probedVertex":
            handleProbedVertex(percept);
            probedVertices.add(percept.toProlog());
            break;
        case "visibleEntity":
            handleVisibleEntity(percept);
            break;
        case "visibleEdge":
            handleVisibleEdge(percept);
            visibleEdges.add(percept.toProlog());
            break;
        case "surveyedEdge":
            handleSurveyedEdge(percept);
            surveyedEdges.add(percept.toProlog());
            break;
        case "edges":
            edges = ((Numeral) percept.getParameters().get(0)).getValue().intValue();
            break;
        case "vertices":
            vertices = ((Numeral) percept.getParameters().get(0)).getValue().intValue();
            break;
        case "step":
            handleStep(percept);
            break;
        }
    }

    private void handleInspectedEntity(Percept percept) {
        // inspectedEntity(Name, Team, Role, Vertex, Energy, MaxEnergy, Health,
        // MaxHealth, Strength, VisRange)
        String name = percept.getParameters().get(0).toString();
        String team = percept.getParameters().get(1).toString();
        String role = percept.getParameters().get(2).toString();
        Vertex position = getVertex(percept.getParameters().get(3).toString());
        int energy = Integer.parseInt(percept.getParameters().get(4).toString());
        int maxEnergy = Integer.parseInt(percept.getParameters().get(5).toString());
        int health = Integer.parseInt(percept.getParameters().get(6).toString());
        int maxHealth = Integer.parseInt(percept.getParameters().get(7).toString());
        int strength = Integer.parseInt(percept.getParameters().get(8).toString());
        int visRange = Integer.parseInt(percept.getParameters().get(9).toString());

        Agent enemy = getAgent(name);
        enemy.setServerName(name);
        enemy.setTeam(team);
        enemy.setRole(role);
        enemy.setPosition(position);
        enemy.setEnergy(energy);
        enemy.setMaxEnergy(maxEnergy);
        enemy.setHealth(health);
        enemy.setMaxHealth(maxHealth);
        enemy.setStrength(strength);
        enemy.setVisRange(visRange);
        enemy.setInspectionStep(getStep());
    }

    private void handleVisibleEntity(Percept percept) {
        String vehicle = percept.getParameters().get(0).toString();
        Vertex position = getVertex(percept.getParameters().get(1).toString());
        boolean disabled = percept.getParameters().get(3).toString().equalsIgnoreCase("disabled");

        Agent agent = getAgent(vehicle);
        agent.setPosition(position);
        agent.setDisabled(disabled);
    }

    // @SuppressWarnings("unchecked")
    private synchronized void handleStep(Percept percept) {
        int newStep = ((Numeral) percept.getParameters().get(0)).getValue().intValue();
        if (newStep > getStep()) {
            for (Vertex vertex : reservedProbedVertices) {
                vertex.setReservedForProbing(false);
            }
            for (Vertex vertex : reservedUnsurveyedVertices) {
                vertex.setReservedForSurveying(false);
            }
            reservedProbedVertices.clear();
            reservedUnsurveyedVertices.clear();
            setStep(newStep);
            // Reset every zone periodically
            if (newStep % resetStep == 0) {
                currentZoneVertices.clear();
                for (Agent agent : getFriendlyAgents()) {
                    agent.setBuildingZone(false);
                }

            }

            // Debug output.
            int numSeenVertices = vertexMap.size();
            int probedVertices = 0;
            for (Vertex vertex : vertexMap.values()) {
                if (vertex.isProbed()) {
                    probedVertices++;
                }
            }

            logger.info("[Step " + getStep() + "] Total Vertices: " + vertices + ". Seen: " + numSeenVertices + "(" + numSeenVertices * 100.0 / vertices + "%) Probed: " + probedVertices + "(" + probedVertices * 100.0 / vertices + "%)");
            // logger.info("[Step " + getStep() + "] TotalEdges: " + edges +
            // ". Visible: " + visibleEdges.size() + "(" + visibleEdges.size() *
            // 100.0 / edges + "%) Surveyed: " + surveyedEdges.size() + "(" +
            // surveyedEdges.size() * 100.0 / edges + "%)");
            // HashSet<String> unsurveyedEdges = (HashSet<String>)
            // visibleEdges.clone();
            // unsurveyedEdges.removeAll(surveyedEdges);
            // logger.info("[Step" + getStep() +
            // "] Remaining unsurveyed edges: " + unsurveyedEdges);
            HashSet<Vertex> unsurveyedVertices = new HashSet<Vertex>();
            for (Vertex vertex : vertexMap.values()) {
                if (!vertex.isSurveyed()) {
                    unsurveyedVertices.add(vertex);
                }
            }
            logger.info("[Step " + getStep() + "] Remaining unsurveyed vertices: " + unsurveyedVertices);
        }
    }

    private void handleSurveyedEdge(Percept percept) {
        String vertexNameA = percept.getParameters().get(0).toString();
        String vertexNameB = percept.getParameters().get(1).toString();
        int edgeWeight = ((Numeral) percept.getParameters().get(2)).getValue().intValue();

        Vertex vertexA = getVertex(vertexNameA);
        Vertex vertexB = getVertex(vertexNameB);

        vertexA.setNeighbour(vertexB, edgeWeight);
        vertexB.setNeighbour(vertexA, edgeWeight);
    }

    private void handleVisibleEdge(Percept percept) {
        String vertexNameA = percept.getParameters().get(0).toString();
        String vertexNameB = percept.getParameters().get(1).toString();

        Vertex vertexA = getVertex(vertexNameA);
        Vertex vertexB = getVertex(vertexNameB);

        vertexA.setNeighbour(vertexB, maxEdgeWeight);
        vertexB.setNeighbour(vertexA, maxEdgeWeight);
    }

    private void handleProbedVertex(Percept percept) {
        String vertexName = percept.getParameters().get(0).toString();
        int vertexValue = ((Numeral) percept.getParameters().get(1)).getValue().intValue();

        Vertex vertex = getVertex(vertexName);
        vertex.setValue(vertexValue);
    }

    private void handleVisibleVertex(Percept percept) {
        String vertexName = percept.getParameters().get(0).toString();
        String team = percept.getParameters().get(1).toString();

        Vertex vertex = getVertex(vertexName);
        vertex.setTeam(team);
    }

    public Vertex getVertex(String name) {
        Vertex vertex;
        if (!vertexMap.containsKey(name)) {
            vertex = new Vertex(name);
            vertexMap.put(name, vertex);
        } else {
            vertex = vertexMap.get(name);
        }
        return vertex;
    }

    /**
     * Returns the closest unsurveyed vertex near the given vertex. "Closest"
     * meaning the least number of hops, and cheapest costs if multiple vertices
     * with the same hop distance are available.
     * 
     * @param position
     *            the vertex to find the next unsurveyed vertex for
     * @return the closest unsurveyed vertex
     */
    public Vertex getNextUnsurveyedVertex(Vertex position) {
        Vertex vertex = position;
        TreeMap<Integer, Vertex> unsurveyedVertices = position.getNextUnsurveyedVertices(1);
        if (!unsurveyedVertices.isEmpty()) {
            Integer key = unsurveyedVertices.firstKey();
            vertex = unsurveyedVertices.get(key);
            vertex.setReservedForSurveying(true);
            reservedUnsurveyedVertices.add(vertex);
        } else {
            logger.info("No known unsurveyed vertices connected to " + position);
        }
        return vertex;
    }

    /**
     * Returns the nearest unprobed vertex. If there is more than one unprobed
     * vertex with minimal hop distance, returns from those vertices the vertex
     * with the highest number of connected probed vertices. If there is still a
     * tie, return the vertex with the lowest path costs.
     * 
     * @param position
     *            the vertex to start the search from
     * @return a nearby unprobed vertex
     */
    public Vertex getNextUnprobedVertex(Vertex position) {
        Vertex vertex = position;
        TreeMap<Integer, Vertex> unprobedVertices = position.getNextUnprobedVertices(1);
        if (!unprobedVertices.isEmpty()) {
            Integer key = unprobedVertices.firstKey();
            vertex = unprobedVertices.get(key);
            reservedProbedVertices.add(vertex);
            vertex.setReservedForProbing(true);
        }
        logger.info("returning vertex" + vertex);
        return vertex;
    }

    /**
     * @param vertex
     *            the vertex to find nearby zones for
     * @param range
     *            the search distance from the given vertex
     * @return the list of zones within range around the vertex
     */
    public ArrayList<Zone> getZonesInRange(Vertex vertex, int range) {
        ArrayList<Vertex> neighbourhood = vertex.getNeighbourhood(range);
        ArrayList<Zone> zones = new ArrayList<Zone>();
        for (Vertex neighbour : neighbourhood) {
            Zone bestMinimalZone = neighbour.getBestMinimalZone();
            if (bestMinimalZone != null) {
                zones.add(neighbour.getBestMinimalZone());
            }
        }
        return zones;
    }

    /**
     * Retrieves the best zone (the one with the highest zone value per agent)
     * from a list of zones.
     * 
     * @param zones
     *            the list of zones to choose the best zone from
     * @return the zone with the highest zone value per agent or {@code null} if
     *         {@code zones} does not contain any zone.
     */
    public Zone getBestZone(ArrayList<Zone> zones) {
        Zone bestZone = null;
        if (zones.size() > 0) {
            for (Zone zone : zones) {
                ArrayList<Vertex> zonePointVertices = zone.getZonePointVertices();
                zonePointVertices.retainAll(currentZoneVertices);
                if (zonePointVertices.size() == 0) {
                    if (bestZone == null) {
                        bestZone = zone;
                    } else if (bestZone.getZoneValuePerAgent() < zone.getZoneValuePerAgent()) {
                        bestZone = zone;
                    }
                }
            }
        }
        return bestZone;
    }

    /**
     * Retrieves the best zone (the one with the highest zone value per agent)
     * from a list of zones, but only those not exceeding the given maximum
     * amount of required agents.
     * 
     * @param zones
     *            the list of zones to choose the best zone from
     * 
     * @param maxAgents
     *            the maximum number of agents for the zone
     * @return the zone with the highest zone value per agent and at most
     *         maxAgents agent positions
     */
    public Zone getBestZoneWithMaxAgents(ArrayList<Zone> zones, int maxAgents) {
        for (int i = zones.size() - 1; i >= 0; i--) {
            Zone zone = zones.get(i);
            if (zone.getPositions().size() > maxAgents) {
                zones.remove(i);
            }
        }
        return getBestZone(zones);
    }

    public Vertex getBestHopToVertex(Vertex position, Vertex destination) {
        Path path = position.getPath(destination);
        return (path == null) ? null : path.getNextHopVertex();
    }

    public Vertex getCheapestHopToVertex(Vertex position, Vertex destination) {
        Path path = position.getPath(destination);
        return (path == null) ? null : path.getNextBestCostVertex();
    }

    public Integer getHopsToVertex(Vertex position, Vertex destination) {
        if (position == destination) {
            return 0;
        }
        Path path = position.getPath(destination);
        if (path == null) {
            return null;
        } else {
            return path.getPathHops();
        }
    }

    public Vertex getClosestVertex(Vertex position, ArrayList<Vertex> vertices) {
        if (vertices.size() > 0) {
            Vertex closest = vertices.get(0);
            for (Vertex destination : vertices) {
                if (position.getPath(destination).getPathHops() < position.getPath(closest).getPathHops()) {
                    closest = destination;
                }
            }
            return closest;
        } else {
            return position;
        }
    }

    /**
     * @return the agent positions in a {@link HashMap} or an empty one if there
     *         was no best minimal zone.
     */
    public HashMap<String, Vertex> getAgentZonePositions(
            Vertex zoneCenterVertex, ArrayList<String> agents) {
        // order agent by distance to center vertex of the zone
        TreeMap<Integer, String> distances = new TreeMap<Integer, String>();
        for (String agentName : agents) {
            Agent agent = getAgent(agentName);
            if (agent != null) {
                Vertex vertex = agent.getPosition();
                Path path = vertex.getPath(zoneCenterVertex);
                if (path != null) {
                    int pathHops = path.getPathHops();
                    distances.put(pathHops, agentName);
                    agent.setBuildingZone(true);
                }
            }
        }

        // map agents to zone positions
        HashMap<String, Vertex> map = new HashMap<String, Vertex>();
        Zone zone = zoneCenterVertex.getBestMinimalZone();
        if (zone != null || distances.isEmpty()) {
            ArrayList<Vertex> positions = zone.getPositions();
            Integer key = distances.lastKey();
            while (positions.size() > 0 && key != null) {
                String agentName = distances.get(key);
                Agent agent = getAgent(agentName);
                key = distances.lowerKey(key);
                Vertex position = agent.getPosition();
                Vertex closest = null;
                Path closestPath = null;
                for (Vertex destination : positions) {
                    Path destinationPath = position.getPath(destination);
                    // there is a closestPath
                    if (destinationPath != null) {
                        if (closest == null || closestPath.getPathHops() > destinationPath.getPathHops()) {
                            closest = destination;
                            closestPath = destinationPath;
                        }
                    }
                }
                if (closest != null) {
                    map.put(agentName, closest);
                    positions.remove(closest);
                }
            }
        }
        // save vertices which are now in a zone to prevent overlapping of zones
        if (map.size() == agents.size()) {
            ArrayList<Vertex> zonePointVertices = zone.getZonePointVertices();
            currentZoneVertices.addAll(zonePointVertices);
        } else {
            logger.info("Some agents have no known path to zone.");
            map = new HashMap<String, Vertex>();
        }
        // return the mapping of agents to positions
        return map;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public Agent getClosestEnemy(Vertex position) {
        Agent closestEnemy = null;
        Integer distanceToClosestEnemy = null;
        for (Agent enemy : getEnemyAgents()) {
            // Check if a path to the enemy even exists, and if the agent is not
            // disabled.
            Vertex enemyPosition = enemy.getPosition();
            if (enemyPosition == null || enemy.isDisabled()) {
                continue;
            }
            Path pathToEnemy = position.getPath(enemyPosition);
            if (pathToEnemy == null) {
                continue;
            }
            // If we haven't found a suitable enemy yet...
            if (closestEnemy == null) {
                closestEnemy = enemy;
                distanceToClosestEnemy = pathToEnemy.getPathHops();
                continue;
            }

            // Compare the distances for the agent we currently think is
            // closest and this one.
            int distanceToThisEnemy = pathToEnemy.getPathHops();
            if (distanceToClosestEnemy > distanceToThisEnemy) {
                closestEnemy = enemy;
                distanceToClosestEnemy = distanceToThisEnemy;
            }
        }
        return closestEnemy;
    }

    /**
     * @return closest agents to the zone identified by {@code center}; can be
     *         empty if there are none.
     */
    public List<String> getClosestAgentsToZone(Vertex center, int count) {
        ArrayList<String> closestAgents = new ArrayList<String>();

        // sort agents in regard of their distance to the center of the zone
        TreeMap<Integer, ArrayList<String>> distanceFromZone = new TreeMap<Integer, ArrayList<String>>();
        HashSet<Agent> availableZoners = new HashSet<Agent>();
        for (Agent agent : getFriendlyAgents()) {
            if (agent.isAvailableForZoning() & !agent.isBuildingZone()) {
                availableZoners.add(agent);
            }
        }
        for (Agent agent : availableZoners) {
            Vertex agentPosition = agent.getPosition();
            Path pathToAgent = agentPosition.getPath(center);
            if (pathToAgent != null) {
                int distance = pathToAgent.getPathHops();
                if (!distanceFromZone.containsKey(distance)) {
                    distanceFromZone.put(distance, new ArrayList<String>());
                }
                distanceFromZone.get(distance).add(agent.getJasonName());
            }
        }

        if (distanceFromZone.size() > 0) {
            // select closest agents to the center of the zone
            Integer key = distanceFromZone.firstKey();
            while (closestAgents.size() < count) {
                closestAgents.addAll(distanceFromZone.get(key));
                key = distanceFromZone.higherKey(key);
                if (key == null) {
                    break;
                }
            }
            if (closestAgents.size() > count) {
                return closestAgents.subList(0, count - 1);
            }
        }
        return closestAgents;
    }

    public void destroyZone(Vertex center, int size) {
        if (size == 1) {
            currentZoneVertices.remove(center);
        } else {
            Zone zone = center.getZone(size);
            currentZoneVertices.removeAll(zone.getZonePointVertices());
        }
    }

    public Agent getAgent(String name) {
        if (agents.containsKey(name)) {
            return agents.get(name);
        } else {
            for (Agent agent : agents.values()) {
                if (agent.isInOurTeam() && agent.getJasonName().equalsIgnoreCase(name)) {
                    return agent;
                }
            }
        }
        logger.info("Could not find agent with name: " + name);
        return null;
    }

    public Vertex getNextBestValueVertex(Vertex position, int range) {
        // TODO Auto-generated method stub
        return null;
    }
}