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
    private int edges = 1;
    private int vertices = 1;
    private int step = 0;
    private HashMap<String, Vertex> vertexMap = new HashMap<String, Vertex>();
    private HashMap<String, Vertex> agentPositions = new HashMap<String, Vertex>();
    private HashSet<String> availableZoners = new HashSet<String>();
    // access enemy agent position by the agent name
    private HashMap<String, Vertex> enemyPositions = new HashMap<String, Vertex>();
    private HashMap<String, Agent> enemyInfos = new HashMap<String, Agent>();
    private AgentLogger logger = new AgentLogger("MapAgent");

    private HashSet<String> visibleVertices = new HashSet<String>();
    private HashSet<String> probedVertices = new HashSet<String>();
    private HashSet<String> visibleEdges = new HashSet<String>();
    private HashSet<String> surveyedEdges = new HashSet<String>();

    private HashSet<Vertex> reservedUnsurveyedVertices = new HashSet<Vertex>();
    private HashSet<Vertex> reservedProbedVertices = new HashSet<Vertex>();

    public static MapAgent getInstance() {
        if (mapAgent == null) {
            mapAgent = new MapAgent();
        }
        return mapAgent;
    }

    /**
     * @param vertex
     *            the vertex to return the list of enemy-free neighbour vertices
     *            for
     * @return the neighbour vertices of the given vertex that have no enemy
     *         agent standing on them
     */
    public ArrayList<Vertex> getSafeNeighbours(Vertex vertex) {
        ArrayList<Vertex> neighbours = vertex.getNeighbours();
        neighbours.removeAll(enemyPositions.values());
        return neighbours;
    }

    /**
     * @param agentName
     *            the name of the agent to look up
     * @return an agent object with the known information about the enemy agent
     */
    public Agent getEnemyInfo(String agentName) {
        if (enemyInfos.containsKey(agentName)) {
            return enemyInfos.get(agentName);
        } else {
            return new Agent();
        }
    }

    /**
     * Used by the internal action removeEnemyGhost to update the MapAgent's
     * knowledge about the locations of enemy agents. Removes an enemy agent
     * from the list of enemy agent positions.
     * 
     * @param enemyName
     *            the Jason name of the enemy agent.
     */
    public void removeFromEnemyPositions(String enemyName) {
        enemyPositions.remove(enemyName);
    }

    /**
     * Stores the position of an agent. Used for agents of both teams.
     * 
     * @param agent
     * @param position
     */
    public void storeAgentPosition(String agent, String position) {
        agentPositions.put(agent, getVertex(position));
    }

    public void addPercept(Percept percept) {
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
        int energy = Integer.parseInt(percept.getParameters().get(0).toString()); // 8
        int health = Integer.parseInt(percept.getParameters().get(1).toString()); // 9
        int maxEnergy = Integer.parseInt(percept.getParameters().get(2).toString()); // 8
        int maxHealth = Integer.parseInt(percept.getParameters().get(3).toString()); // 9
        String name = percept.getParameters().get(4).toString(); // b5
        Vertex node = getVertex(percept.getParameters().get(5).toString()); // v10
        String role = percept.getParameters().get(6).toString(); // explorer
        int strength = Integer.parseInt(percept.getParameters().get(7).toString()); // 6
        String team = percept.getParameters().get(8).toString(); // teamB
        int visRange = Integer.parseInt(percept.getParameters().get(9).toString()); // 2

        Agent enemy;
        if (!enemyInfos.containsKey(name)) {
            enemy = new Agent();
        } else {
            enemy = enemyInfos.get(name);
        }
        enemy.setEnergy(energy);
        enemy.setHealth(health);
        enemy.setMaxEnergy(maxEnergy);
        enemy.setMaxHealth(maxHealth);
        enemy.setServerName(name);
        enemy.setNode(node);
        enemy.setRole(role);
        enemy.setStrength(strength);
        enemy.setTeam(team);
        enemy.setVisRange(visRange);
        enemy.setInspectionStep(getStep());
        enemyInfos.put(name, enemy);
    }

    private void handleVisibleEntity(Percept percept) {
        String vehicle = percept.getParameters().get(0).toString();
        Vertex position = getVertex(percept.getParameters().get(1).toString());
        String team = percept.getParameters().get(2).toString();
        boolean disabled = percept.getParameters().get(3).toString().equalsIgnoreCase("disabled");

        if (team.equalsIgnoreCase(AgentHandler.enemyTeam)) {
            Agent enemyAgent = getEnemyInfo(vehicle);
            enemyAgent.setJasonName(vehicle);
            enemyAgent.setPosition(position);
            enemyAgent.setTeam(team);
            enemyAgent.setDisabled(disabled);
            if (!disabled) {
                enemyPositions.put(vehicle, position);
            } else {
                enemyPositions.remove(vehicle);
            }
        }
    }

    private void handleStep(Percept percept) {
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
            logger.info("[" + getStep() + "] Total Vertices: " + vertices + ". Visible: " + visibleVertices.size() + "(" + visibleVertices.size() * 100.0 / vertices + "%) Probed: " + probedVertices.size() + "(" + probedVertices.size() * 100.0 / vertices + "%)");
            logger.info("[" + getStep() + "] TotalEdges: " + edges + ". Visible: " + visibleEdges.size() + "(" + visibleEdges.size() * 100.0 / edges + "%) Surveyed: " + surveyedEdges.size() + "(" + surveyedEdges.size() * 100.0 / edges + "%)");
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
            zones.add(neighbour.getBestMinimalZone());
        }
        return zones;
    }

    /**
     * Retrieves the best zone (the one with the highest zone value per agent)
     * from a list of zones.
     * 
     * @param zones
     *            the list of zones to choose the best zone from
     * @return the zone with the highest zone value per agent
     */
    public Zone getBestZone(ArrayList<Zone> zones) {
        Zone bestZone = null;
        for (Zone zone : zones) {
            if (bestZone.getZoneValuePerAgent() < zone.getZoneValuePerAgent()) {
                bestZone = zone;
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
        if (zones.size() == 0) {
            return null; // should probably throw an exception instead
        } else {
            Zone bestZone = zones.get(0);
            for (Zone zone : zones) {
                if (bestZone.getZoneValuePerAgent() < zone.getZoneValuePerAgent()) {
                    bestZone = zone;
                }
            }
            return bestZone;
        }
    }

    public Vertex getBestHopToVertex(Vertex position, Vertex destination) {
        return position.getPath(destination).getNextHopVertex();
    }

    public Vertex getCheapestHopToVertex(Vertex position, Vertex destination) {
        return position.getPath(destination).getNextBestCostVertex();
    }

    public Integer getHopsToVertex(Vertex position, Vertex destination) {
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

    public HashMap<String, Vertex> getAgentZonePositions(
            Vertex zoneCenterVertex, ArrayList<String> agents) {
        TreeMap<Integer, String> distances = new TreeMap<Integer, String>();
        for (String agent : agents) {
            Vertex vertex = agentPositions.get(agent);
            int pathHops = vertex.getPath(zoneCenterVertex).getPathHops();
            distances.put(pathHops, agent);
            availableZoners.remove(agent);
        }

        Zone zone = zoneCenterVertex.getBestMinimalZone();
        ArrayList<Vertex> positions = zone.getPositions();
        HashMap<String, Vertex> map = new HashMap<String, Vertex>();
        int key = distances.lastKey();
        while (positions.size() > 0) {
            String agent = distances.get(key);
            key = distances.lowerKey(key);
            Vertex position = agentPositions.get(agent);
            Vertex closest = positions.get(0);
            for (Vertex destination : positions) {
                if (position.getPath(closest).getPathHops() > position.getPath(destination).getPathHops()) {
                    closest = destination;
                }
            }
            map.put(agent, closest);
            positions.remove(closest);
        }
        return map;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public Vertex getClosestEnemyPosition(Vertex position) {
        Vertex enemyPosition = position;
        if (enemyPositions.size() > 0) {
            int currentHops = 0;
            for (String key : enemyPositions.keySet()) {
                Vertex vertex = enemyPositions.get(key);
                if (enemyPosition == position) {
                    enemyPosition = vertex;
                    currentHops = position.getPath(enemyPosition).getPathHops();
                } else {
                    Path path = position.getPath(vertex);
                    if (path.getPathHops() < currentHops) {
                        enemyPosition = vertex;
                    }
                }
            }
        }
        return enemyPosition;
    }

    public Agent getClosestEnemy(Vertex position) {
        // logger.info("Closest enemy debug: Entering getClosestEnemy(" +
        // position + ")");
        Vertex enemyPosition = position;
        String agentName = null;
        if (enemyPositions.size() > 0) {
            int currentHops = 0;
            for (String key : enemyPositions.keySet()) {
                Vertex vertex = enemyPositions.get(key);
                if (enemyPosition == position) {
                    agentName = key;
                    enemyPosition = vertex;
                    currentHops = position.getPath(enemyPosition).getPathHops();
                } else {
                    Path path = position.getPath(vertex);
                    if (path.getPathHops() < currentHops) {
                        agentName = key;
                        enemyPosition = vertex;
                    }
                }
            }
        }
        Agent enemy = getEnemyInfo(agentName);
        // logger.info("Closest enemy debug: Leaving getClosestEnemy(" +
        // position + "). Enemy is " + enemy);
        return enemy;
    }

    public List<String> getClosestAgentsToZone(Vertex center, int count) {
        ArrayList<String> closestAgents = new ArrayList<String>();

        // sort agents in regard of their distance to the center of the zone
        TreeMap<Integer, ArrayList<String>> distanceFromZone = new TreeMap<Integer, ArrayList<String>>();
        for (String agent : availableZoners) {
            Vertex agentPosition = agentPositions.get(agent);
            int distance = agentPosition.getPath(center).getPathHops();
            if (!distanceFromZone.containsKey(distance)) {
                distanceFromZone.put(distance, new ArrayList<String>());
            }
            distanceFromZone.get(distance).add(agent);
        }

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
        return closestAgents;
    }

    public void registerForZoning(String agent) {
        availableZoners.add(agent);
    }
}