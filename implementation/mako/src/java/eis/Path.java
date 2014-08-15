package eis;

public class Path {

    private Vertex destination;
    private Vertex nextHopVertex;
    private Vertex nextBestCostVertex;
    private int costs = 0;
    private int hops = 0;

    public Path(Vertex destination) {
        this.destination = destination;
    }

    public Vertex getDestination() {
        return this.destination;
    }

    public int getPathCosts() {
        return this.costs;
    }

    public Vertex getNextBestCostVertex() {
        return this.nextBestCostVertex;
    }

    public boolean setPathCosts(int pathCosts, Vertex nextBestCostVertex) {
        if (this.costs == 0 || this.costs > pathCosts) {
            this.costs = pathCosts;
            this.nextBestCostVertex = nextBestCostVertex;
            return true;
        }
        return false;
    }

    public int getPathHops() {
        return this.hops;
    }

    public Vertex getNextHopVertex() {
        return this.nextHopVertex;
    }

    public boolean setPathHops(int hops, Vertex nextHopVertex) {
        if (this.hops == 0 || this.hops > hops) {
            this.hops = hops;
            this.nextHopVertex = nextHopVertex;
            return true;
        }
        return false;
    }

    public String toString() {
        return "-(" + nextHopVertex + "[" + hops + "]/" + nextBestCostVertex + "[" + costs + "])->" + destination;
    }
}
