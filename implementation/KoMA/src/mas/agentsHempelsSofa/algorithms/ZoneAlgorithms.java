package mas.agentsHempelsSofa.algorithms;

import java.util.LinkedList;

import mas.agentsHempelsSofa.data.AgentToken;
import mas.agentsHempelsSofa.data.graph.Graph;
import mas.agentsHempelsSofa.data.graph.Vertex;
import mas.agentsHempelsSofa.data.zone.Zone;

/**
 * An implementation of some algorithms that are needed to calculate our zones.
 * @author Hempels-Sofa
 */
public class ZoneAlgorithms {

    /**
     * tests whether vertices in the zone is isolated by a frontier. this fails,
     * if there are no enemy vertices known yet.
     * @param graph the graph the zone is created on.
     * @param zone the zone which shall be checked.
     * @param dominatingTeams an array of dominating teams which follows the
     *        mapping graph.vertices(i) -> dominatingTeams(i).
     * @param start the vertex the vertices shall be expanded from.
     * @return either the list of vertices which are in the zone or (this can be
     *         trusted) null, if the start-vertex was not in the zone.
     */
    public static LinkedList<Vertex> testIsolated ( Graph graph, Zone zone,
            String[] dominatingTeams, Vertex start ) {
        LinkedList<Vertex> queue = new LinkedList<Vertex>();
        LinkedList<Vertex> visited = new LinkedList<Vertex>();
        queue.addLast(start);
        visited.addLast(start);
        while ( !queue.isEmpty() ) {
            Vertex v = queue.removeFirst();
            // if there is an enemy node -> abort
            for ( AgentToken a : v.getTokens() )
                if ( !a.isOfTeam(zone.getTeam()) )
                    return null;
            for ( Vertex neigh : v.getAdjacentVertices() )
                if ( !visited.contains(neigh)
                        && !zone.getVertices().contains(neigh) ) {
                    queue.addLast(neigh);
                    visited.addLast(neigh);
                }
        }
        visited.removeAll(zone.getVertices());
        return visited;
    }

}
