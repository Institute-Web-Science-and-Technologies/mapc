package mas.agentsHempelsSofa.data.graph;

import mas.agentsHempelsSofa.data.AgentToken;
import eis.iilang.Identifier;

import java.util.LinkedList;
import java.util.Random;

/**
 * An implementation of some utility methods which can be helpful for our graph.
 * @author Hempels-Sofa
 */
public class Util {

    /**
     * Generates a random graph.
     * @param numberOfNodes the number of nodes to generate
     * @param numberOfEdges the number of edges to generate
     * @return a random graph
     */
    public static Graph generateRandomGraph ( int numberOfNodes,
            int numberOfEdges, boolean allSurveyed ) {
        Graph g = new Graph();
        for ( int i = 0; i < numberOfNodes; i++ )
            g.add(new Vertex(new Identifier("vertex" + i)));
        Edge e;
        for ( int i = 0; i < numberOfEdges; i++ ) {
            do {
                e = generateRandomEdge(g, allSurveyed);
            } while ( g.getEdge(e) != null );
            g.add(e);
        }
        g.setPosition(g.getVertices()
                .get((int) (Math.random() * numberOfNodes)));
        return g;
    }

    /**
     * Generates a random edge
     * @param graph the graph the edge shall be created from
     * @return the edge
     */
    public static Edge generateRandomEdge ( Graph graph, boolean surveyed ) {
        Vertex v1 = graph.getVertices().get(
                (int) (Math.random() * graph.getNumberOfVertices()));
        Vertex v2;
        do {
            v2 = graph.getVertices().get(
                    (int) (Math.random() * graph.getNumberOfVertices()));
        } while ( v1.equals(v2) );
        int weight = -1;
        if ( surveyed && Math.random() < 0.5 )
            weight = (int) (Math.random() * 8) + 1;
        return new Edge(v1, v2, weight);
    }

    /**
     * Gets a random edge of a graph
     * @param graph the graph the edge shall be picked from
     * @return the edge
     */
    public static Edge getRandomEdge ( Graph graph ) {
        return graph.getEdges().get(
                (int) (Math.random() * graph.getNumberOfVertices()));
    }

    /**
     * Generates tokens on graph g
     * @param numberOfTokens number of tokens to be generated
     * @param g the graph on which the tokens will be placed
     * @param team the Team to which the tokens belong
     * @param state the state of the generated tokens
     * @param role the role of the generated tokens
     */
    public static LinkedList<AgentToken> genereateRandomTokens (
            int numberOfTokens, Graph g, String team, String state,
            String role, int energy, int health, int strength, int visRange,
            int lastInspection ) {

        LinkedList<AgentToken> tokens = new LinkedList<AgentToken>();
        Random generator = new Random();
        int r;

        for ( int i = 0; i < numberOfTokens; i++ ) {
            r = generator.nextInt(g.getVertices().size());
            AgentToken token = new AgentToken(team + i, team, g
                    .getVertex("vertex" + r), state, role, energy, health,
                    strength, visRange, lastInspection);

            tokens.add(token);

        }
        return tokens;

    }

}
