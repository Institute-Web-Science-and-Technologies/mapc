package graph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import eis.Agent;
import eis.iilang.Identifier;
import eis.iilang.Numeral;

/**
 * This class represents global graph accessible by all agents. Implements
 * IGraph interface.
 * 
 * @author Sergey Dedukh
 * @author Miriam Koelle
 * @author Michael Ruster
 */
public class Graph implements IGraph {
	/**
	 * The Integer maps the vertex id for faster querying.
	 */
	private HashMap<Integer, Vertex> vertices;
	private HashSet<Edge> edges;

	private int amountVertices;
	private int amountEdges;

	@Override
	public synchronized void addVertex(Identifier vertexID) {
		// TODO Auto-generated method stub

	}

	@Override
	public synchronized void updateVertexValue(Identifier vertexID,
			Numeral value) {
		// TODO Auto-generated method stub

	}

	@Override
	public synchronized void addEdge(Identifier vertexA, Identifier vertexB) {
		// TODO Auto-generated method stub

	}

	@Override
	public synchronized void removeEnemyPosition(Agent a) {
		// TODO Auto-generated method stub

	}

	@Override
	public synchronized void updateEdgeWeight(Identifier vertexID,
			Numeral weight) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<Identifier> getShortestPath(Identifier vertexS,
			Identifier vertexD) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Identifier> getNeighborhood(Identifier vertexV, int depth) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getVertexValue(Identifier vertexID) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getEdgeWeight(Identifier vertexS, Identifier vertexD) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean existsPath(Identifier vertexS, Identifier vertexD) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public synchronized void addEnemyPosition(Identifier vertexID, Agent a) {
		// TODO Auto-generated method stub

	}

	@Override
	public synchronized void updateEnemyPosition(Identifier newVertexID, Agent a) {
		// TODO Auto-generated method stub

	}

	@Override
	public HashMap<Agent, Integer> getCloseEnemiesDistance(Identifier vertexID,
			int depth) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HashMap<Agent, Vertex> getCloseEnemies(Identifier vertexID, int depth) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HashMap<Agent, Vertex> getCloseEnemySaboteurs(Identifier vertexID,
			int depth) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HashMap<Agent, Vertex> getCloseEnemyRepairers(Identifier vertexID,
			int depth) {
		// TODO Auto-generated method stub
		return null;
	}

}
