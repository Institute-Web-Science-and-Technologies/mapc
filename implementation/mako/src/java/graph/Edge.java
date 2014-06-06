package graph;

import eis.iilang.Identifier;
import eis.iilang.Numeral;

/**
 * This class models an undirected edge of a graph. Edges don't have their own
 * identifier. But they can be identified through the Id of the both vertices it
 * connects.
 * <p>
 * This method does not offer Setters for the vertices as edges cannot change in
 * the Mars scenario.
 * 
 * @author Sergey Dedukh
 * @author Miriam Koelle
 * @author Michael Ruster
 */
public class Edge {
    private Identifier vertexAID;
    private Identifier vertexBID;
    private Numeral weight;

    /**
     * Creates a new edge between Vertex A and B while setting the initial
     * weight to 1000. Vertex A is determined by the vertex with the lower
     * number.
     * 
     * @throws IllegalArgumentException
     *             if a Vertex ID is null or Vertex IDs are identical.
     * @throws NumberFormatException
     *             if vertexAID and vertexBID do not adhere the naming scheme
     *             "v123".
     * @param vertexAID
     *            Identifier of Vertex A of format "v123" (with 123 being
     *            {@code int}).
     * @param vertexBID
     *            Identifier of Vertex B of format "v123" (with 234 being
     *            {@code int}).
     */
    Edge(Identifier vertexAID, Identifier vertexBID) {
        if (vertexAID == null || vertexBID == null) {
            throw new IllegalArgumentException("Vertex IDs must be set and hence may not be null.");
        }
        if (vertexAID.equals(vertexBID)) {
            throw new IllegalArgumentException("An edge may not connect a vertex with itself.");
        }
        try {
            int vertexAIDValue = new Integer(vertexAID.toString().split("v")[0]);
            int vertexBIDValue = new Integer(vertexBID.toString().split("v")[0]);

            this.vertexAID = (vertexAIDValue < vertexBIDValue) ? vertexAID : vertexBID;
            this.vertexBID = (vertexAIDValue > vertexBIDValue) ? vertexAID : vertexBID;

            this.weight = new Numeral(1000);
        } catch (NumberFormatException e) {
            throw new NumberFormatException("A vertex ID was not represented by the char 'v' followed by an int.");
        }
    }

    public Identifier getVertexAID() {
        return vertexAID;
    }

    public Identifier getVertexBID() {
        return vertexBID;
    }

    public Numeral getWeight() {
        return weight;
    }

    public void setWeight(Numeral weight) {
        this.weight = weight;
    }

    /**
     * @return a hash value for this edge ignoring its weight.
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((vertexAID == null) ? 0 : vertexAID.hashCode());
        result = prime * result + ((vertexBID == null) ? 0 : vertexBID.hashCode());
        return result;
    }

    /**
     * Compares two edges while ignoring their weight. This is done by only
     * looking at the vertices it connects. An explicit test for
     * bidirectionality is not done because the constructor enforces a strict
     * order of Vertex A and B.
     * 
     * @see #Edge(Identifier, Identifier)
     * 
     * @return {@code true} iff {@code obj} is connected to the same vertices as
     *         this object is.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Edge)) {
            return false;
        }
        Edge otherEdge = (Edge) obj;

        assert (this.vertexAID != null);
        assert (this.vertexBID != null);
        assert (otherEdge.getVertexAID() != null);
        assert (otherEdge.getVertexBID() != null);

        if (!vertexAID.equals(otherEdge.vertexAID)) {
            return false;
        }
        if (!vertexBID.equals(otherEdge.vertexBID)) {
            return false;
        }
        return true;
    }

}
