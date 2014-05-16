package mas.agentsHempelsSofa.data.graph;

import java.util.LinkedList;

/**
 * Provides an adjacency matrix to represent a graph.
 * @author Hempels-Sofa
 */
public class AdjacencyMatrix {

    /**
     * the matrix.
     */
    private int[][] matrix;
    /**
     * the dimension of the matrix.
     */
    private int dim;
    /**
     * the ids of the vertices.
     */
    private int[] ids;
    /**
     * true, if reachable, false if not.
     */
    private boolean[] reachable;
    /**
     * the position of the agent on the graph.
     */
    private int position;
    /**
     * the agent names.
     */
    private String[][] agentNames;

    /**
     * Creates a new adjacency matrix of a given graph g.
     * @param g - the given graph
     */
    public AdjacencyMatrix ( Graph g ) {
        dim = g.getNumberOfVertices();
        matrix = new int[dim][dim];
        ids = new int[dim];
        reachable = new boolean[dim];
        agentNames = new String[dim][];
        for ( Vertex v : g.getVertices() ) {
            int i = g.getVertices().indexOf(v);
            String name = g.getVertices().get(i).getName();
            name = name.replace("vertex", "");
            try {
                ids[i] = Integer.parseInt(name);
            }
            catch ( Exception ex ) {
            }
            if ( v.isAdjacentTo(g.getPosition()) )
                reachable[i] = true;
            agentNames[i] = v.getAgentNames();
        }
        position = g.getVertices().indexOf(g.getPosition());
        LinkedList<Vertex> vertices = g.getVertices();
        for ( Edge e : g.getEdges() ) {
            int i = vertices.indexOf(e.getVertices()[0]);
            int j = vertices.indexOf(e.getVertices()[1]);
            if ( !e.isSurveyed() )
                matrix[i][j] = matrix[j][i] = -1;
            else
                matrix[i][j] = matrix[j][i] = e.getWeight();
        }
    }

    @Override
    public String toString () {
        String s = "     ";
        for ( int i = 0; i < dim; i++ ) {
            if ( ids[i] <= 9 )
                s += " ";
            s += ids[i] + " ";
        }
        s += "\n\n";
        for ( int i = 0; i < dim; i++ ) {
            if ( reachable[i] )
                s += "r ";
            else if ( i == position )
                s += "p ";
            else
                s += "  ";
            s += ids[i] + " ";
            if ( ids[i] <= 9 )
                s += " ";
            if ( ids[i] >= 100 )
                s += "  ";
            for ( int j = 0; j < dim; j++ ) {
                if ( matrix[i][j] > -1 )
                    s += " ";
                s += matrix[i][j] + " ";
            }
            for ( int j = 0; j < agentNames[i].length; j++ )
                s += " " + agentNames[i][j];
            s += "\n";
        }
        return s;
    }

    /**
     * @return a string representation of the adjacency matrix without any other
     *         information.
     */
    public String toCleanString () {
        String s = "";
        for ( int i = 0; i < dim; i++ ) {
            for ( int j = 0; j < dim; j++ ) {
                if ( matrix[i][j] > -1 )
                    s += " ";
                s += matrix[i][j] + " ";
            }
            s += "\n";
        }
        return s;
    }

    /**
     * @param row the row of the matrix.
     * @param column the column of the matrix.
     * @return the entry at the position {@code (row, column)} in the matrix.
     */
    public int getEntry ( int row, int column ) {
        return matrix[row][column];
    }

    /**
     * @return the adjacency matrix as a double integer array.
     */
    public int[][] getEntries () {
        return matrix;
    }

}
