package mas.agentsHempelsSofa.data.graph;

import java.util.LinkedList;

import mas.agentsHempelsSofa.data.Believable;

import apltk.interpreter.data.LogicBelief;
import eis.iilang.Identifier;
import eis.iilang.Parameter;

/**
 * An implementation of an undirected edge for our Graph. An edge knows
 * <ul>
 * <li>it's incident vertices,</li>
 * <li>it's weight,</li>
 * <li>whether it is already surveyed or not.</li>
 * </ul>
 * @author Hempels-Sofa
 */
public class Edge implements Believable {

    /* ------------------------ Fields ---------------------------- */

    /**
     * The two adjacent vertices.
     */
    private Vertex[] vertices;
    /**
     * The weight of the edge.
     */
    private int weight;
    public static final int UNSURVEYED_WEIGHT = -1;

    /* --------------------- Constructors ------------------------- */

    /**
     * Creates a new edge from {@code vertex1} to {@code vertex2} with a
     * specified {@code weight}. {@code surveyed} says weather the real weight
     * is known.
     * @param vertex1 The first vertex of the edge.
     * @param vertex2 The second vertex of the edge.
     * @param weight The weight of the edge.
     */
    public Edge ( Vertex vertex1, Vertex vertex2, int weight ) {
        setVertices(new Vertex[] { vertex1, vertex2 });
        this.setWeight(weight);
    }

    /**
     * Creates a new unsurveyed edge from {@code vertex1} to {@code vertex2}
     * with unknown {@code weight} (0).
     * @param vertex1 The first vertex of the edge.
     * @param vertex2 The second vertex of the edge.
     */
    public Edge ( Vertex vertex1, Vertex vertex2 ) {
        this(vertex1, vertex2, UNSURVEYED_WEIGHT);
    }

    /**
     * Creates a new edge from {@code param1} to {@code param2} with unknown
     * {@code weight} (0).
     * @param param1 The identifier for the first vertex.
     * @param param2 The identifier for the second vertex.
     */
    public Edge ( Parameter param1, Parameter param2 ) {
        this(new Vertex(param1), new Vertex(param2));
    }

    public Edge ( String param1, String param2 ) {
        this(new Identifier(param1), new Identifier(param2));
    }

    /* ------------------- Provided Methods ----------------------- */

    @Override
    public final boolean equals ( Object obj ) {
        Edge e = (Edge) obj;
        if ( (e.getVertices()[0].equals(this.getVertices()[0]) || e
                .getVertices()[0].equals(this.getVertices()[1]))
                && (e.getVertices()[1].equals(this.getVertices()[0]) || e
                        .getVertices()[1].equals(this.getVertices()[1])) )
            return true;
        return false;
    }

    @Override
    public final Edge clone () {
        return new Edge(vertices[0], vertices[1], weight);
    }

    @Override
    public final String toString () {
        return vertices[0] + " <" + weight + "> " + vertices[1];
    }

    /**
     * generates a belief for this edge.
     * @return a belief with
     *         <ul>
     *         <li>{@code predicate} - edge,</li>
     *         <li>{@code parameter1} - vertex1,</li>
     *         <li>{@code parameter2} - vertex2,</li>
     *         <li>{@code parameter3} - weight,</li>
     *         <li>{@code parameter4} - surveyed</li>
     *         </ul>
     */
    @Override
    public LogicBelief toBelief () {
        LinkedList<String> parameters = new LinkedList<String>();
        parameters.add(vertices[0].getName());
        parameters.add(vertices[1].getName());
        parameters.add(Integer.toString(weight));
        return new LogicBelief("edge", parameters);
    }

    /* -------------------- Getter, Setter ------------------------ */

    /**
     * @param vertices the vertices to set
     */
    public void setVertices ( Vertex[] vertices ) {
        this.vertices = vertices;
    }

    /**
     * @return the vertices as an array.
     */
    public Vertex[] getVertices () {
        return vertices;
    }

    /**
     * @param weight the weight of the edge.
     */
    public void setWeight ( int weight ) {
        this.weight = weight;
    }

    /**
     * @return the weight of the edge.
     */
    public int getWeight () {
        return weight;
    }

    /**
     * @return <ul>
     *         <li>{@code true}, if the real weight is known,</li>
     *         <li>{@code false}, otherwise.</li>
     *         </ul>
     */
    public boolean isSurveyed () {
        return (this.weight != UNSURVEYED_WEIGHT);
    }

    public void setUnsurveyed () {
        this.weight = UNSURVEYED_WEIGHT;
    }
}
