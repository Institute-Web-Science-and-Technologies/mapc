package mas.agentsHempelsSofa.data.zone;

import java.util.LinkedList;

import apltk.interpreter.data.LogicBelief;

import mas.agentsHempelsSofa.algorithms.ZoneAlgorithms;
import mas.agentsHempelsSofa.data.AgentToken;
import mas.agentsHempelsSofa.data.graph.Graph;
import mas.agentsHempelsSofa.data.graph.Vertex;

/**
 * An implementation of a zone manager which shall keep all zones up to date. it
 * can be created and there is a single method {@link #update()} to update all
 * zones.
 * @author Hempels-Sofa
 */
public class ZoneManager {

    /**
     * all zones which are managed.
     */
    private LinkedList<Zone> zones;
    /**
     * the graph the zone manager refers to.
     */
    private Graph graph;
    /**
     * the team of the agent which holds this zone manager.
     */
    private String ownTeam;
    /**
     * the teams occurring as dominant teams.
     */
    private LinkedList<String> teams;
    /**
     * an array of team names which are sorted by the vertices in {@link graph}.
     * They represent the dominant team of the corresponding vertex.
     */
    private String[] dominatingTeams;
    /**
     * The vertices which are already added to a zone.
     */
    private LinkedList<Vertex> alreadyInZone;
    private boolean verbose = false;

    // ----------------------- Constructors ---------------------------

    /**
     * Creates a new zone manager. It manages all zones in the {@code graph}.
     * @param graph the graph the zones shall refer to.
     * @param ownTeam the team of the agent which creates this manager.
     */
    public ZoneManager ( Graph graph ) {
        this.graph = graph;
        this.zones = new LinkedList<Zone>();
        alreadyInZone = new LinkedList<Vertex>();
        update();
    }

    public void setOwnTeam ( String ownTeam ) {
        this.ownTeam = ownTeam;
    }

    // --------------------- Provided Methods -------------------------

    /**
     * updates the zone manager. this should be executed everytimes after
     * updating the graph and agent tokens.
     */
    public void update () {
        zones.clear();
        alreadyInZone.clear();
        if ( graph.getNumberOfVertices() == 0 )
            return;
        if ( verbose )
            System.out.println("ZoneManager[77]: computing the teams");
        computeDominatingTeams();
        if ( verbose )
            System.out.println("ZoneManager[80]: determining the teams");
        determineTeams(dominatingTeams);
        if ( verbose )
            System.out.println("ZoneManager[83]: recomputing the zones");
        reComputeZones();
        for ( Zone z : zones )
            z.update();
    }

    @Override
    public String toString () {
        String s = "";
        for ( Zone z : zones )
            s += z.toString() + "\n";
        return s;
    }

    // ---------------------- computations ----------------------------

    /**
     * looks up which vertex is dominated by which ownTeam.
     */
    public void computeDominatingTeams () {
        dominatingTeams = new String[graph.getNumberOfVertices()];
        // determine dominating teams for each vertex
        for ( int i = 0; i < graph.getNumberOfVertices(); i++ ) {
            Vertex v = graph.getVertices().get(i);
            LinkedList<String> teamNames = new LinkedList<String>();
            LinkedList<Integer> teamCount = new LinkedList<Integer>();
            teamNames.add(null);
            teamCount.add(0);
            // count the agents of all teams
            for ( int j = 0; j < v.getTokens().size(); j++ ) {
                AgentToken token = v.getTokens().get(j);
                // disabled and non zone builder agents are neglected
                if ( !(token.isDisabled())
                        && (token.isZoneBuilder() || !token.isStrategyKnown()) ) {
                    boolean teamFound = false;
                    for ( int k = 1; k < teamNames.size(); k++ ) {
                        if ( token.getTeam().equals(teamNames.get(k)) ) {
                            teamCount.set(k, teamCount.get(k) + 1);
                            teamFound = true;
                            break;
                        }
                    }
                    if ( !teamFound ) {
                        teamCount.add(1);
                        teamNames.add(token.getTeam());
                    }
                }
            }
            int maxIndex = 0;
            int max = 0;
            boolean draw = true;
            for ( int j = 1; j < teamCount.size(); j++ )
                if ( max < teamCount.get(j) ) {
                    max = teamCount.get(j);
                    maxIndex = j;
                    draw = false;
                }
                else if ( max == teamCount.get(j) )
                    draw = true;
            if ( draw )
                dominatingTeams[i] = null;
            else
                dominatingTeams[i] = teamNames.get(maxIndex);
        }
    }

    /**
     * computes a list of all different teams which are dominating at least one
     * vertex.
     * @param dominatingTeams the array which is a mapping: graph.vertices(i) ->
     *        dominatingTeam(i)
     */
    public void determineTeams ( String[] dominatingTeams ) {
        teams = new LinkedList<String>();
        teams.add(ownTeam);
        for ( String team : dominatingTeams ) {
            if ( team != null ) {
                boolean contained = false;
                for ( String t : teams )
                    if ( t.equals(team) ) {
                        contained = true;
                        break;
                    }
                if ( !contained )
                    teams.add(team);
            }
        }
    }

    // --------------------- Computing the zones -------------------------

    /**
     * computes the zones.
     * @param dominatingTeams
     */
    private void reComputeZones () {
        // if only one team dominates vertices, the whole graph is a zone of
        // this team
        if ( teams.size() == 1 && !graph.getEnemyTokens(ownTeam).isEmpty() ) {
            Zone bigZone = new Zone();
            bigZone.setTeam(teams.getFirst());
            for ( Vertex v : graph.getVertices() )
                bigZone.addVertex(v);
            zones.add(bigZone);
            return;
        }
        if ( verbose )
            System.out.println("ZoneManager[188]: creating elementary zones");
        // 1. create zones for all dominated vertices
        createElementaryZones();
        if ( verbose )
            System.out
                    .println("ZoneManager[192]: creating elementary neighboring zones");
        // 2.a) create zones for all vertices which are adjacent.
        createElementaryNeighboringZones();
        if ( verbose )
            System.out.println("ZoneManager[196]: uniting neighboring zones");
        // 2.b) unite all zones that belong together (if they are adjacent)
        uniteAllNeighboringZones();
        // 3. add the isolated nodes to each zone
        if ( enemiesSpotted() )
            addAllIsolatedVertices();
    }

    /**
     * creates elementary zones for each dominated vertex and saves these in
     * {@link #zones}.
     */
    public void createElementaryZones () {
        for ( Vertex v : graph.getVertices() )
            if ( dominatingTeams[graph.getVertices().indexOf(v)] != null ) {
                Zone z = new Zone();
                z.setTeam(dominatingTeams[graph.getVertices().indexOf(v)]);
                z.addVertex(v);
                alreadyInZone.add(v);
                zones.add(z);
            }
    }

    /**
     * creates elementary zones for a team out of each vertex, if the following
     * conditioned are complied with.
     * <ol>
     * <li>at least two zones of the same team are neighboring the vertex.</li>
     * <li>the team has more neighboring zones to the vertex than each other
     * teams.</li>
     * </ol>
     */
    public void createElementaryNeighboringZones () {
        LinkedList<Vertex> potentiallyAdd = new LinkedList<Vertex>();
        // for all neighbors of all colored vertices
        for ( Vertex v : alreadyInZone )
            for ( Vertex w : v.getAdjacentVertices() )
                // if not colored yet
                if ( !alreadyInZone.contains(w) && !potentiallyAdd.contains(w) )
                    potentiallyAdd.add(w);
        // count the team's agents
        int[][] teamCount = new int[teams.size()][potentiallyAdd.size()];
        for ( Vertex v : potentiallyAdd )
            for ( Vertex neighbor : v.getAdjacentVertices() ) {
                String neighborTeam = dominatingTeams[graph.getVertices()
                        .indexOf(neighbor)];
                if ( neighborTeam != null )
                    for ( int i = 0; i < teams.size(); i++ )
                        if ( teams.get(i).equals(neighborTeam) ) {
                            teamCount[i][potentiallyAdd.indexOf(v)]++;
                            break;
                        }
            }
        // create elementary zones for winners
        for ( int j = 0; j < potentiallyAdd.size(); j++ ) {
            int max = 0, maxIndex = 0;
            boolean draw = true;
            for ( int i = 0; i < teams.size(); i++ ) {
                if ( teamCount[i][j] > max ) {
                    max = teamCount[i][j];
                    maxIndex = i;
                    draw = false;
                }
                else if ( teamCount[i][j] == max )
                    draw = true;
            }
            if ( !draw && max > 1 ) {
                Zone z = new Zone();
                z.setTeam(teams.get(maxIndex));
                Vertex v = potentiallyAdd.get(j);
                z.addVertex(v);
                alreadyInZone.add(v);
                zones.add(z);
            }
        }
    }

    /**
     * unites all neighboring zones to one zone in {@link #zones}.
     */
    @SuppressWarnings("unchecked")
    public void uniteAllNeighboringZones () {
        LinkedList<Zone> newZones = new LinkedList<Zone>();
        for ( Zone z1 : zones ) {
            if ( !z1.isEmpty() ) {
                newZones.add(z1);
                for ( Zone z2 : zones ) {
                    if ( !(z1 == z2) && !z2.isEmpty()
                            && z1.getTeam().equals(z2.getTeam())
                            && z1.isAdjacentTo(z2) )
                        z1.union(z2);
                }
            }
        }
        zones = (LinkedList<Zone>) newZones.clone();
        newZones.clear();
        for ( Zone z : zones )
            if ( !z.isEmpty() )
                newZones.add(z);
        zones = newZones;
    }

    /**
     * adds all isolated vertices to each zone. This can be incorrectly
     * calculated if there is no enemy agent is spotted yet and the percepted
     * value is not set. So it is recommended to pass the percepted value of
     * each zone of the own team. The stability value is computed too.
     */
    public void addAllIsolatedVertices () {
        for ( Zone z : zones ) {
            LinkedList<Vertex> isolatedVertices = null;
            for ( Vertex v : z.getVertices() )
                // look at all neighbors of all vertices in the zone
                for ( Vertex neighbor : v.getAdjacentVertices() ) {
                    if ( !alreadyInZone.contains(neighbor) ) {
                        isolatedVertices = ZoneAlgorithms.testIsolated(graph,
                                z, dominatingTeams, neighbor);
                        if ( isolatedVertices != null )
                            break;
                    }
                }
            // if the zone is circular
            if ( isolatedVertices != null ) {
                z.setIsolatedVertices(isolatedVertices);
                for ( Vertex isolatedVertex : isolatedVertices )
                    z.addVertex(isolatedVertex);
            }
            // determine the frontier of the zone
            for ( Vertex v : z.getVertices() ) {
                for ( AgentToken a : z.getTokens() )
                    if ( a.getTeam().equals(z.getTeam()) )
                        z.getTokens().add(a);
                for ( Vertex w : v.getAdjacentVertices() )
                    if ( !z.contains(w) ) {
                        z.getCriticalFrontier().add(v);
                        for ( AgentToken a : z.getCriticalFrontierTokens() )
                            if ( a.getTeam().equals(z.getTeam()) )
                                z.getCriticalFrontierTokens().add(a);
                        break;
                    }
            }
        }
    }

    // ----------------------- Getter, Setter ------------------------

    /**
     * @return the total value of all zones of the own team.
     */
    public int getValue () {
        int value = 0;
        for ( Zone z : zones )
            if ( z.getTeam().equals(ownTeam) )
                value += z.getValue();
        return value;
    }

    /**
     * @param vertex the vertex of one zone.
     * @return the value of the zone the {@code vertex} belongs to. if {@code
     *         vertex} is in no zone the value will be 0.
     */
    public int getValue ( Vertex vertex ) {
        for ( Zone z : zones )
            if ( z.contains(vertex) )
                return z.getValue();
        return 0;
    }

    /**
     * @param agent the agent token of one zone
     * @return the value of the zone the {@code agent} is positioned in. if
     *         {@code agent} is in no zone the value will be 0.
     */
    public int getValue ( AgentToken agent ) {
        for ( Zone z : zones )
            if ( z.contains(agent) )
                return z.getValue();
        return 0;
    }

    /**
     * @param vertex the vertex of one zone.
     * @return the zone the {@code vertex} belongs to. if the {@code vertex} is
     *         in no zone the return value will be {@code null}.
     */
    public Zone getZone ( Vertex vertex ) {
        for ( Zone z : zones )
            if ( z.contains(vertex) )
                return z;
        return null;
    }

    /**
     * @param agent the agent token of one zone.
     * @return the zone the {@code agent} is positioned in. if the {@code agent}
     *         is in no zone the return value will be {@code null}.
     */
    public Zone getZone ( AgentToken agent ) {
        for ( Zone z : zones )
            if ( z.contains(agent) )
                return z;
        return null;
    }

    /**
     * @return the most precious zone of the own team.
     */
    public Zone getMostPreciousZone () {
        Zone mostPrecious = null;
        for ( Zone z : zones )
            if ( (mostPrecious == null || z.getValue() > mostPrecious
                    .getValue())
                    && z.getTeam().equals(ownTeam) )
                mostPrecious = z;
        return mostPrecious;
    }

    /**
     * @return the most precious zone of all enemy teams.
     */
    public Zone getMostPreciousEnemyZone () {
        Zone mostPrecious = null;
        for ( Zone z : zones )
            if ( (mostPrecious == null || z.getValue() > mostPrecious
                    .getValue())
                    && !z.getTeam().equals(ownTeam) )
                mostPrecious = z;
        return mostPrecious;
    }

    /**
     * @return the zone with the most vertices of the own team.
     */
    public Zone getBiggestZone () {
        Zone biggest = null;
        for ( Zone z : zones )
            if ( (biggest == null || z.size() > biggest.size())
                    && z.getTeam().equals(ownTeam) )
                biggest = z;
        return biggest;
    }

    /**
     * @return the zone with the most vertices of all enemy teams.
     */
    public Zone getBiggestEnemyZone () {
        Zone biggest = null;
        for ( Zone z : zones )
            if ( (biggest == null || z.size() > biggest.size())
                    && !z.getTeam().equals(ownTeam) )
                biggest = z;
        return biggest;
    }

    /**
     * @return <ul>
     *         <li>{@code true}, if there are enemy agent tokens on the graph</li>
     *         <li>{@code false}, otherwise.</li>
     *         </ul>
     */
    public boolean enemiesSpotted () {
        for ( AgentToken token : graph.getTokens() )
            if ( !token.getTeam().equals(ownTeam) )
                return true;
        return false;
    }

    /**
     * @return all zones
     */
    public LinkedList<Zone> getZones () {
        return zones;
    }

    public LinkedList<LogicBelief> toBeliefs () {
        LinkedList<LogicBelief> beliefs = new LinkedList<LogicBelief>();
        for ( Zone z : zones )
            beliefs.add(z.toBelief());
        return beliefs;
    }

    /**
     * @param vertex the vertex
     * @return <ul>
     *         <li>{@code true}, if the {@code vertex} is in the most precious
     *         zone of the own team.</li>
     *         <li>{@code false}, otherwise.</li>
     *         </ul>
     */
    public boolean isInMostPreciousZone ( Vertex vertex ) {
        if ( getMostPreciousZone() == null )
            return false;
        return getMostPreciousZone().contains(vertex);
    }

    /**
     * @param zones the zones to set
     */
    public void setZones ( LinkedList<Zone> zones ) {
        this.zones = zones;
    }

    /**
     * @param teams the teams to set
     */
    public void setTeams ( LinkedList<String> teams ) {
        this.teams = teams;
    }

    /**
     * @return the teams
     */
    public LinkedList<String> getTeams () {
        return teams;
    }

}
