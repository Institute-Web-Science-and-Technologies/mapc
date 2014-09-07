package eis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

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
    private HashSet<Vertex> reservedScoreVertices = new HashSet<Vertex>();
    private HashSet<Agent> reservedEnemiesForInspection = new HashSet<Agent>();

    /**
     * Because AgentSpeak treats any string that starts with an upper case
     * letter as a variable, we have to make sure to convert agent and team
     * names from mixed case to lower case before we send them to AgentSpeak,
     * and then convert them back to mixed case in the case where we have to
     * send an action to the server that contains an agent name. The key is the
     * agent name in lower case, and the saved value is the "real" name.
     */
    public static HashMap<String, String> agentNameConversionMap = new HashMap<String, String>();

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
     * For the given vertex, return a list of "safe" neighbour vertices. A safe
     * vertex is one where no not-disabled, enemy could-be-saboteur agent is
     * currently either on or adjacent to.
     * 
     * @param vertex
     *            the vertex to return the list of enemy-free neighbour vertices
     *            for
     * @return the neighbour vertices of the given vertex that have no enemy
     *         agent standing on or next to them (possibly empty)
     */
    public ArrayList<Vertex> getSafeNeighbours(Vertex vertex) {
        ArrayList<Vertex> neighbours = vertex.getNeighbours();
        for (Agent enemy : getEnemyAgents()) {
            Vertex enemyPosition = enemy.getPosition();
            Object enemyRole = enemy.getRole();
            if (enemyPosition != null && (enemyRole == null || enemyRole == "saboteur") && !enemy.isDisabled()) {
                neighbours.remove(enemyPosition);
                neighbours.removeAll(enemyPosition.getNeighbours());
            }
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
        String role = percept.getParameters().get(2).toString().toLowerCase();
        Vertex position = getVertex(percept.getParameters().get(3).toString());
        int energy = Integer.parseInt(percept.getParameters().get(4).toString());
        int maxEnergy = Integer.parseInt(percept.getParameters().get(5).toString());
        int health = Integer.parseInt(percept.getParameters().get(6).toString());
        int maxHealth = Integer.parseInt(percept.getParameters().get(7).toString());
        int strength = Integer.parseInt(percept.getParameters().get(8).toString());
        int visRange = Integer.parseInt(percept.getParameters().get(9).toString());

        Agent enemy = getAgent(name.toLowerCase());
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
        String team = percept.getParameters().get(2).toString();
        boolean disabled = percept.getParameters().get(3).toString().equalsIgnoreCase("disabled");

        Agent agent = getAgent(vehicle.toLowerCase());
        agent.setServerName(vehicle);
        agent.setTeam(team);
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
            for (Vertex vertex : reservedScoreVertices) {
                vertex.setReservedForScoring(false);
            }
            reservedProbedVertices.clear();
            reservedUnsurveyedVertices.clear();
            reservedScoreVertices.clear();
            reservedEnemiesForInspection.clear();
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
            logger.info("[Step " + getStep() + "] TotalEdges: " + edges + ". Visible: " + visibleEdges.size() + "(" + visibleEdges.size() * 100.0 / edges + "%) Surveyed: " + surveyedEdges.size() + "(" + surveyedEdges.size() * 100.0 / edges + "%)");

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
        TreeSet<SortablePair<Agent>> distanceAgentPairs = new TreeSet<>();
        ArrayList<Agent> blockedAgents = new ArrayList<>();
        for (String agentName : agents) {
            Agent agent = getAgent(agentName);
            if (agent != null) {
                Vertex vertex = agent.getPosition();
                Path path = vertex.getPath(zoneCenterVertex);
                if (path != null) {
                    int pathHops = path.getPathHops();
                    distanceAgentPairs.add(new SortablePair<Agent>(pathHops, agent));
                    agent.setBuildingZone(true);
                    blockedAgents.add(agent);
                } else {
                    logger.info("[bug] If you can read this then getBestZone does not work. No path to CentreNode.");
                }
            } else {
                logger.info("[bug] If you can read this then getBestZone does not work. No agent with given name.");
            }
        }

        // map agents to zone positions
        HashMap<String, Vertex> map = new HashMap<String, Vertex>();
        Zone zone = zoneCenterVertex.getBestMinimalZone();
        if (zone != null || distanceAgentPairs.isEmpty()) {
            ArrayList<Vertex> zoneNodes = zone.getPositions();
            assert (distanceAgentPairs.size() == zoneNodes.size());
            SortablePair<Agent> pair = distanceAgentPairs.first();

            while (zoneNodes.size() > 0 && pair != null) {
                Agent agent = pair.getO();
                Vertex agentPosition = agent.getPosition();

                Vertex closest = null;
                Path closestPath = null;
                for (Vertex zoneNode : zoneNodes) {
                    Path destinationPath = agentPosition.getPath(zoneNode);
                    // there is a closestPath
                    if (destinationPath != null) {
                        if (closest == null || closestPath.getPathHops() > destinationPath.getPathHops()) {
                            closest = zoneNode;
                            closestPath = destinationPath;
                        }
                    }
                }
                if (closest != null) { // not unreachable for current agent
                    map.put(agent.getJasonName(), closest);
                    zoneNodes.remove(closest);
                    // remove positioned agent:
                    distanceAgentPairs.remove(pair);
                    // reset looking for an agent to start from the closest
                    // again:
                    if (!distanceAgentPairs.isEmpty()) {
                        // throws NoSuchElementException instead of returning
                        // null:
                        pair = distanceAgentPairs.first();
                    }
                } else {
                    // choose the next more distant agent for the next round:
                    pair = distanceAgentPairs.higher(pair);
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
            // Unset the zone building flag in all touched agents:
            for (Agent agent : blockedAgents) {
                agent.setBuildingZone(false);
            }
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

    /**
     * Returns the closest enemy agent from the given position. Prioritizes
     * saboteurs.
     * 
     * @param position
     *            the vertex to check for closest enemies from
     * @return the closest enemy agent
     */
    public Agent getClosestEnemy(Vertex position) {
        Agent closestEnemy = null;
        Integer distanceToClosestEnemy = null;
        for (Agent enemy : getEnemyAgents()) {
            // Check if we even know where the enemy is, and if the enemy is not
            // disabled.
            Vertex enemyPosition = enemy.getPosition();
            if (enemyPosition == null || enemy.isDisabled()) {
                continue;
            }
            // If there is an enemy on our node, look no further
            if (enemyPosition == position) {
                if (enemy.getRole() == "saboteur") {
                    return enemy;
                } else {
                    closestEnemy = enemy;
                    distanceToClosestEnemy = 0;
                    continue;
                }
            }
            Path pathToEnemy = position.getPath(enemyPosition);
            // If no path to the enemy location exists, we don't know how to
            // reach the enemy
            if (pathToEnemy == null) {
                continue;
            }
            // If we haven't found a suitable enemy yet, save this one
            if (closestEnemy == null) {
                closestEnemy = enemy;
                distanceToClosestEnemy = pathToEnemy.getPathHops();
                continue;
            }

            // Compare the distances for the agent we currently think is
            // closest and this one. Prioritize saboteurs.
            int distanceToThisEnemy = pathToEnemy.getPathHops();
            if ((distanceToClosestEnemy > distanceToThisEnemy) || (distanceToClosestEnemy >= distanceToThisEnemy && enemy.getRole() == "saboteur")) {
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
        Zone zone = center.getZone(size);
        if (size == 1 || zone == null) {
            currentZoneVertices.remove(center);
        } else {
            currentZoneVertices.removeAll(zone.getZonePointVertices());
        }
    }

    public Agent getAgent(String name) {
        if (name != name.toLowerCase()) {
            throw new IllegalArgumentException("Agent name must be in lower case when calling getAgent method. Was: " + name);
        }
        name = name.toLowerCase();
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
        Agent newAgent = new Agent();
        newAgent.setServerName(name);
        newAgent.setJasonName("unknown");
        agents.put(name, newAgent);
        return newAgent;
    }

    public Vertex getNextBestValueVertex(Vertex position, int range) {
        Vertex bestScoreVertex = position;
        ArrayList<Vertex> list = position.getNeighbourhood(range);
        for (Vertex vertex : list) {
            if (vertex.getValue() > bestScoreVertex.getValue() && !vertex.isReservedForScoring()) {
                bestScoreVertex = vertex;
            }
        }
        if (bestScoreVertex != position) {
            reservedScoreVertices.add(bestScoreVertex);
            bestScoreVertex.setReservedForScoring(true);
        }
        return bestScoreVertex;
    }

    /**
     * Returns the closest uninspected enemy. Used by Inspector agents. Since we
     * don't want to send more than one inspector after an enemy, we also keep
     * track of which enemies are already getting chased by one of our
     * inspectors.
     * 
     * @param position
     *            the vertex to find the closest uninspected enemy for
     * @return the closest uninspected enemy
     */
    public Agent getClosestUninspectedEnemy(Vertex position) {
        Agent closestUninspectedEnemy = null;
        Integer distanceToClosestUninspectedEnemy = null;
        for (Agent enemy : getEnemyAgents()) {
            // Check if the enemy is inspected before doing anything else, or if
            // another inspector is already chasing it. Skip it if it is.
            if (enemy.isInspected() || reservedEnemiesForInspection.contains(enemy)) {
                continue;
            }
            // Check if we even know where the enemy is, and if the enemy is not
            // disabled. We don't consider disabled agents to be important
            // enough to seek them out.
            Vertex enemyPosition = enemy.getPosition();
            if (enemyPosition == null || enemy.isDisabled()) {
                continue;
            }
            // If there is an uninspected enemy on our node, look no further
            if (enemyPosition == position) {
                return enemy;
            }

            // If no path to the enemy location exists, we don't know how to
            // get there. Skip it.
            Path pathToEnemy = position.getPath(enemyPosition);
            if (pathToEnemy == null) {
                continue;
            }
            // If we haven't found a suitable enemy yet, save this one.
            if (closestUninspectedEnemy == null) {
                closestUninspectedEnemy = enemy;
                distanceToClosestUninspectedEnemy = pathToEnemy.getPathHops();
                continue;
            }

            // Compare the distances for the agent we currently think is
            // closest and this one.
            int distanceToThisEnemy = pathToEnemy.getPathHops();
            if ((distanceToClosestUninspectedEnemy > distanceToThisEnemy)) {
                closestUninspectedEnemy = enemy;
                distanceToClosestUninspectedEnemy = distanceToThisEnemy;
            }
        }
        reservedEnemiesForInspection.add(closestUninspectedEnemy);
        return closestUninspectedEnemy;
    }
}
