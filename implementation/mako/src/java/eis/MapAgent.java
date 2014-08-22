package eis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
            visibleVertices.add(percept.toProlog());
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
        enemyInfos.put(name, enemy);
    }

    private void handleVisibleEntity(Percept percept) {
        String vehicle = percept.getParameters().get(0).toString();
        String position = percept.getParameters().get(1).toString();
        String team = percept.getParameters().get(2).toString();
        String state = percept.getParameters().get(3).toString();

        if (team.equalsIgnoreCase(AgentHandler.enemyTeam)) {
            if (state.equalsIgnoreCase("normal")) {
                enemyPositions.put(vehicle, getVertex(position));
            } else {
                enemyPositions.remove(vehicle);
            }
        }
    }

    private void handleStep(Percept percept) {
        int newStep = ((Numeral) percept.getParameters().get(0)).getValue().intValue();
        if (newStep > step) {
            reservedProbedVertices.clear();
            reservedUnsurveyedVertices.clear();
            step = newStep;
            logger.info("[" + step + "] Total Vertices: " + vertices + ". Visible: " + visibleVertices.size() + "(" + visibleVertices.size() * 100.0 / vertices + "%) Probed: " + probedVertices.size() + "(" + probedVertices.size() * 100.0 / vertices + "%)");
            logger.info("[" + step + "] TotalEdges: " + edges + ". Visible: " + visibleEdges.size() + "(" + visibleEdges.size() * 100.0 / edges + "%) Surveyed: " + surveyedEdges.size() + "(" + surveyedEdges.size() * 100.0 / edges + "%)");
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
            int key = unsurveyedVertices.firstKey();
            vertex = unsurveyedVertices.get(key);
            while (reservedUnsurveyedVertices.contains(vertex)) {
                key = unsurveyedVertices.higherKey(key);
                vertex = unsurveyedVertices.get(key);
            }
            reservedUnsurveyedVertices.add(vertex);
        }
        return vertex;
    }

    public Vertex getNextUnprobedVertex(Vertex position) {
        Vertex vertex = position;
        TreeMap<Integer, Vertex> unprobedVertices = position.getNextUnprobedVertices(1);
        if (!unprobedVertices.isEmpty()) {
            int key = unprobedVertices.firstKey();
            vertex = unprobedVertices.get(key);
            while (reservedProbedVertices.contains(vertex)) {
                key = unprobedVertices.higherKey(key);
                vertex = unprobedVertices.get(key);
            }
            reservedProbedVertices.add(vertex);
        }
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
        for (int i = zones.size() - 1; i > 0; i--) {
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

    public boolean isVertexProbed(Vertex vertex) {
        return vertex.isProbed();
    }

    public boolean isVertexSurveyed(Vertex vertex) {
        return vertex.isSurveyed();
    }

    public Vertex getBestHopToVertex(Vertex position, Vertex destination) {
        return position.getPath(destination).getNextHopVertex();
    }

    public Vertex getCheapestHopToVertex(Vertex position, Vertex destination) {
        return position.getPath(destination).getNextBestCostVertex();
    }

    public int getHopsToVertex(Vertex position, Vertex destination) {
        return position.getPath(destination).getPathHops();
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
}