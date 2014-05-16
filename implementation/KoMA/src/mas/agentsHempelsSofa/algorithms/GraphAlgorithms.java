package mas.agentsHempelsSofa.algorithms;

import java.util.*;

import mas.agentsHempelsSofa.data.graph.*;

/**
 * An implementation of some graph algorithms which are important for our agents
 * to compute their next actions.
 * @author Hempels-Sofa
 */
public class GraphAlgorithms {

    private static boolean verbose = false;

    /**
     * Runs the standard Dijkstra Algorithm on a given {@code graph}.
     * @param graph The actually known graph.
     * @return The shortest paths from the actual position in {@code graph} to
     *         all vertices as a list of lists of edges.
     */
    public static LinkedList<LinkedList<Vertex>> dijkstra ( Graph graph ) {
        if ( verbose )
            System.out.println("GraphAlgorithms[21]: disjkstra");
        return dijkstra(graph, 0, 1);
    }

    /**
     * Runs a weighted Dijkstra Algorithm on a given {@code graph}.
     * @param graph The actually known graph.
     * @param stepWeight The weight for the number of steps. This is important,
     *        since every goto issue costs a step.
     * @param edgeWeight The weight for the edges weights. This determines the
     *        influence of edge weights.
     * @return The shortest paths from the actual position in {@code graph} to
     *         all vertices as a list of lists of edges.
     */
    public static LinkedList<LinkedList<Vertex>> dijkstra ( Graph graph,
            double stepWeight, double edgeWeight ) {
        LinkedList<Vertex> connectedComponent = graph
                .getSurveyedConnectedComponent();
        return generateListOfPaths(graph, connectedComponent, findPredecessors(
                graph, connectedComponent, stepWeight, edgeWeight, 10, true));
    }

    // J make the goTowards algorithm consider the recharge rate
    /**
     * this action is pretty similar to dijkstra() above only difference: it
     * considers the unsurveyed edges as well, weighting them by 5
     */
    public static LinkedList<LinkedList<Vertex>> goTowards ( Graph graph,
            double stepWeight, double edgeWeight, int maxEdgeCost ) {
        LinkedList<Vertex> connectedComponent = graph.getConnectedComponent();
        return generateListOfPaths(graph, connectedComponent, findPredecessors(
                graph, connectedComponent, stepWeight, edgeWeight, maxEdgeCost,
                false));
    }

    /**
     *this method call finds all shortest path from a given Vertex root
     */
    public static LinkedList<LinkedList<Vertex>> goTowards ( Vertex root,
            Graph graph, double stepWeight, double edgeWeight ) {
        LinkedList<Vertex> connectedComponent = graph
                .getConnectedComponent(root);
        return generateListOfPaths(graph, connectedComponent, findPredecessors(
                graph, connectedComponent, stepWeight, edgeWeight, 10, false));
    }

    public static LinkedList<LinkedList<Vertex>> goTowards ( Graph graph ) {
        return goTowards(graph, 1, 1, 10);
    }

    /**
     * Finds the predecessor vertices for all vertices in the graph.
     * @param graph the graph
     * @param stepWeight the step weight
     * @param edgeWeight the edge weight
     * @return A field of predecessor vertices for all vertices in the graph.
     */
    @SuppressWarnings("unchecked")
    private static Vertex[] findPredecessors ( Graph graph,
            LinkedList<Vertex> connectedComponent, double stepWeight,
            double edgeWeight, int maxEdgeWeight, boolean onlySurveyed ) {

        LinkedList<Vertex> notExpanded; // the nodes which are not yet expanded
        Vertex[] predecessorVertices; // the vertices predeceding to each vertex
        Vertex runner; // a runner vertex (for finding predecessors)

        // I. initialize fields

        // not expanded contain all nodes of the graph
        notExpanded = (LinkedList<Vertex>) connectedComponent.clone();
        // predecessor vertices are the same as position
        predecessorVertices = new Vertex[notExpanded.size()];
        predecessorVertices[connectedComponent.indexOf(graph.getPosition())] = graph
                .getPosition();
        // runner starts at the actual position
        runner = graph.getPosition();
        // all distances except for position are infinitely high
        for ( Vertex vertex : notExpanded )
            if ( vertex == graph.getPosition() )
                vertex.setWeightedDistance(0);
            else
                vertex.setWeightedDistance(Double.POSITIVE_INFINITY);

        // II. find the best predecessors for shortest paths

        if ( verbose )
            System.out.println("GraphAlgrorithms[114]: finding Predecessors");
        while ( !notExpanded.isEmpty() && runner != null ) {
            // 1. expand runner1
            notExpanded.remove(runner);

            // 1.1 update adjacent vertices
            /*
             * for(Vertex neighbor : runner.getAdjacentVertices())
             * if(notExpanded.contains(neighbor)){
             */
            for ( Vertex neighbor : notExpanded ) {
                if ( !neighbor.isAdjacentTo(runner) )
                    continue;
                double distance;
                if ( graph.getEdge(runner, neighbor).isSurveyed() ) {
                    if ( graph.getEdge(runner, neighbor).getWeight() > maxEdgeWeight )
                        distance = Double.POSITIVE_INFINITY;
                    else
                        distance = edgeWeight
                                * graph.getEdge(runner, neighbor).getWeight()
                                + stepWeight + runner.getWeightedDistance();
                }
                else {
                    if ( maxEdgeWeight < 10 ) {
                        distance = edgeWeight * 10 + stepWeight
                                + runner.getWeightedDistance();
                    }
                    else {
                        distance = edgeWeight * 4.5 + stepWeight
                                + runner.getWeightedDistance();
                    }

                }
                if ( distance < neighbor.getWeightedDistance() ) {
                    neighbor.setWeightedDistance(distance);
                    // update predecessor vertex
                    if ( runner != null )
                        predecessorVertices[connectedComponent
                                .indexOf(neighbor)] = runner;
                }
            }

            // 2. determine the next vertex to expand
            Vertex next = null;
            double min = Double.POSITIVE_INFINITY;
            for ( Vertex search : notExpanded ) {
                if ( min > search.getWeightedDistance() ) {
                    min = search.getWeightedDistance();
                    next = search;
                }
            }
            runner = next; // if null, notExpanded is empty -> termination of
                           // while
        }
        return predecessorVertices;
    }

    /**
     * Generates a list of paths from a field of predecessor nodes in a graph.
     * @param graph the graph
     * @param predecessorVertices the predecessor nodes field
     * @return a list of paths to all vertices in the graph.
     */
    private static LinkedList<LinkedList<Vertex>> generateListOfPaths (
            Graph graph, LinkedList<Vertex> connectedComponent,
            Vertex[] predecessorVertices ) {

        LinkedList<LinkedList<Vertex>> listOfPaths = new LinkedList<LinkedList<Vertex>>();

        // generate paths
        for ( Vertex runner : connectedComponent ) {
            if ( runner == null )
                continue;
            LinkedList<Vertex> path = new LinkedList<Vertex>();
            while ( !runner.equals(graph.getPosition()) ) {
                path.addFirst(runner);

                runner = predecessorVertices[connectedComponent.indexOf(runner)];
                if ( runner == null )
                    break;
            }
            path.addFirst(graph.getPosition());
            listOfPaths.add(path);
        }
        return listOfPaths;
    }

    /**
     * Runs breadth first search on the graph to find the fastest path
     * difference to findConnectdComponent is that this algorithm stops when the
     * target is found
     * @param graph the graph
     * @param source the source vertex
     * @param target the target to go to
     * @return the fastest path
     */
    public static LinkedList<Vertex> findFastestPath ( Graph graph,
            Vertex source, Vertex target ) {
        LinkedList<Vertex> queue = new LinkedList<Vertex>();
        LinkedList<Vertex> connectedComponent = new LinkedList<Vertex>();

        queue.add(source);
        connectedComponent.add(source);
        if ( verbose )
            System.out.println("GraphAlgorithms[213]: finding fastest path");
        while ( !queue.isEmpty() ) {
            Vertex v = queue.removeFirst();
            for ( Edge e : v.getIncidentEdges() ) {
                Vertex w = e.getVertices()[0];
                if ( w.equals(v) )
                    w = e.getVertices()[1];
                if ( !connectedComponent.contains(w) ) {
                    queue.add(graph.getVertex(w));
                    connectedComponent.add(graph.getVertex(w));
                    if ( w.equals(target) )
                        break;
                }
            }
        }

        // Find path in connectedComponent
        LinkedList<Vertex> path = new LinkedList<Vertex>();
        Vertex runner = target;
        while ( !runner.equals(source) ) {
            path.addFirst(runner);
            // determine new runner
            // find minimum index
            int min = Integer.MAX_VALUE;
            for ( Vertex x : runner.getAdjacentVertices() ) {
                if ( connectedComponent.indexOf(x) < min )
                    min = connectedComponent.indexOf(x);
            }
            runner = connectedComponent.get(min);

        }
        path.add(source);

        return path;

    }

    /**
     * Runs breadth first search on the graph.
     * @param graph the graph
     * @param root the source node
     * @return The connected component which contains {@code source}.
     */
    public static LinkedList<Vertex> findConnectedComponent ( Graph graph,
            Vertex root ) {

        LinkedList<Vertex> queue = new LinkedList<Vertex>();
        LinkedList<Vertex> connectedComponent = new LinkedList<Vertex>();
        queue.add(root);
        connectedComponent.add(root);
        if ( verbose )
            System.out
                    .println("GraphAlgorithms[262]: finding connected component");
        while ( !queue.isEmpty() ) {
            Vertex v = queue.removeFirst();
            for ( Edge e : v.getIncidentEdges() ) {
                Vertex w = e.getVertices()[0];
                if ( w.equals(v) )
                    w = e.getVertices()[1];
                if ( !connectedComponent.contains(w) ) {
                    queue.add(graph.getVertex(w));
                    connectedComponent.add(graph.getVertex(w));
                }
            }
        }
        return connectedComponent;
    }

    /**
     * Runs breadth first search on the graph.
     * @param graph the graph
     * @param root the source node
     * @return The connected component which contains {@code source}.
     */
    @SuppressWarnings("unchecked")
    public static LinkedList<Vertex> findSurveyedConnectedComponent (
            Graph graph, Vertex root ) {

        LinkedList<Vertex> queue = new LinkedList<Vertex>();
        LinkedList<Vertex> connectedComponent = new LinkedList<Vertex>();
        queue.add(root);
        connectedComponent.add(root);
        while ( !queue.isEmpty() ) {
            Vertex v = queue.removeFirst();
            LinkedList<Edge> incidentEdges = (LinkedList<Edge>) v
                    .getIncidentEdges().clone();
            LinkedList<Edge> edgesToRemove = new LinkedList<Edge>();
            // remove edges which are not surveyed
            for ( Edge e : incidentEdges )
                if ( !e.isSurveyed() )
                    edgesToRemove.add(e);
            incidentEdges.removeAll(edgesToRemove);
            for ( Edge e : incidentEdges ) {
                e = graph.getEdge(e);
                Vertex w = e.getVertices()[0];
                if ( w.equals(v) )
                    w = e.getVertices()[1];
                if ( !connectedComponent.contains(w) ) {
                    queue.add(graph.getVertex(w));
                    connectedComponent.add(graph.getVertex(w));
                }
            }
        }
        return connectedComponent;
    }

    /**
     * returns a list of all the neighbours which are at most 2 steps away of
     * the position. the position itself is not included
     * @param core the center vertex.
     */
    public static LinkedList<Vertex> getSurrounding ( Vertex core ) {
        HashSet<Vertex> surround = new HashSet<Vertex>();
        LinkedList<Vertex> returnList = new LinkedList<Vertex>();
        surround.addAll(core.getAdjacentVertices());
        for ( Vertex v : core.getAdjacentVertices() )
            surround.addAll(v.getAdjacentVertices());
        for ( Vertex v : surround )
            if ( v.getName() != core.getName() )
                returnList.add(v);
        return returnList;

    }

}