package mas.agentsTargaryen;

import java.util.ArrayList;
import java.util.Collections;

import apltk.interpreter.data.LogicBelief;

public class TargaryenUtil {

	private TargaryenAgent agent;
	private ArrayList<LogicBelief> knoten = new ArrayList<LogicBelief>();
	private ArrayList<LogicBelief> kanten = new ArrayList<LogicBelief>();
	protected ArrayList<Vertex> vertices = new ArrayList<Vertex>();

	public TargaryenUtil(TargaryenAgent agent) {
		this.agent = agent;
	}

	/**
	 * Richtung zu einem bestimmten Vertex finden
	 */
	public ArrayList<String> getDirection(String start, ArrayList<String> ziel) {
		Dijkstra dija = new Dijkstra();

		// Gridstruktur updaten
		updateVertices();

		return dija.getDirection(vertices, ziel, start);
	}

	/**
	 * Informationen zu Kanten und Knoten updaten
	 */
	private void updateVertices() {
		for (LogicBelief b : agent.getAllBeliefs("vertex")) {
			LogicBelief fu = new LogicBelief("vertex", b.getParameters().get(0));
			if (!knoten.contains(fu)) {
				knoten.add(fu);
				vertices.add(new Vertex(b.getParameters().get(0)));
				vertices.get(vertices.size() - 1).adjacencies = new ArrayList<Edge>();
			}
		}
		for (LogicBelief b : agent.getAllBeliefs("edge")) {
			if (kanten.contains(new LogicBelief("edge", b.getParameters().get(0), b.getParameters().get(1), "11"))
					&& !b.getParameters().get(2).equals("11")) {
				kanten.set(kanten.indexOf(new LogicBelief(b.getPredicate(), b.getParameters().get(0), b.getParameters().get(1), "11")), b);
				for (Vertex v : vertices) {
					if (v.name.equals(b.getParameters().get(0))) {
						for (Edge e : v.adjacencies) {
							if (e.target.name.equals(b.getParameters().get(1))) {
								Vertex watt = e.target;
								double i = Integer.parseInt(b.getParameters().get(2));
								i = i * 2;
								i = i / Integer.parseInt(agent.getAllBeliefs("maxEnergy").get(0).getParameters().get(0));
								i = i + 1;

								v.adjacencies.set(v.adjacencies.indexOf(e), new Edge(watt, i));
							}
						}
					}
					if (v.name.equals(b.getParameters().get(1))) {
						for (Edge e : v.adjacencies) {
							if (e.target.name.equals(b.getParameters().get(0))) {
								Vertex watt = e.target;
								double i = Integer.parseInt(b.getParameters().get(2));
								i = i * 2;
								i = i / Integer.parseInt(agent.getAllBeliefs("maxEnergy").get(0).getParameters().get(0));
								i = i + 1;

								v.adjacencies.set(v.adjacencies.indexOf(e), new Edge(watt, i));
							}
						}
					}
				}
			}

			if (!kanten.contains(b)) {
				kanten.add(b);
				Vertex a = new Vertex("");
				Vertex c = new Vertex("");
				for (Vertex v : vertices) {
					if (v.name.equals(b.getParameters().get(0))) {
						a = v;
					}
					if (v.name.equals(b.getParameters().get(1))) {
						c = v;
					}
				}
				double i = Integer.parseInt(b.getParameters().get(2));
				i = i * 2;
				i = i / Integer.parseInt(agent.getAllBeliefs("maxEnergy").get(0).getParameters().get(0));
				i = i + 1;
				vertices.get(vertices.indexOf(a)).adjacencies.add(new Edge(c, i));
				vertices.get(vertices.indexOf(c)).adjacencies.add(new Edge(a, i));
			}
		}
	}

	/**
	 * Unerforschte Kanten finden
	 */
	public ArrayList<String> getUnknownEdge(String start) {
		ArrayList<String> nah = new ArrayList<String>();
		ArrayList<String> path = new ArrayList<String>();
		if (!agent.getAllBeliefs("edge", "", "", "11").isEmpty()) {
			for (LogicBelief b : agent.getAllBeliefs("edge", "", "", "11")) {
				if (!nah.contains(b.getParameters().get(0)))
					nah.add(b.getParameters().get(0));
				if (!nah.contains(b.getParameters().get(1)))
					nah.add(b.getParameters().get(1));
			}
		}
		ArrayList<String> small = new ArrayList<String>();
		for (LogicBelief b : agent.getAllBeliefs("edge", "start")) {
			String a = b.getParameters().get(0);
			if (nah.contains(a))
				small.add(a);
		}
		for (LogicBelief b : agent.getAllBeliefs("edge", "", "start")) {
			String a = b.getParameters().get(1);
			if (nah.contains(a))
				small.add(a);
		}
		if (!small.isEmpty()) {
			path.clear();
			Collections.shuffle(small);
			path.add(start);
			path.add(small.get(0));
			small.clear();
			small.add("small");
			return small;
		}
		return nah;
	}

	/**
	 * Unerforschte Knoten finden
	 */
	public ArrayList<String> getUnknownVertexes(String start) {
		ArrayList<String> nah = new ArrayList<String>();
		ArrayList<String> fern = new ArrayList<String>();
		if (!agent.getAllBeliefs("vertex", "", "-1").isEmpty()) {
			for (LogicBelief b : agent.getAllBeliefs("vertex", "", "-1")) {
				String a = b.getParameters().get(0);
				if (!agent.getAllBeliefs("edge", a, start).isEmpty() || !agent.getAllBeliefs("edge", start, a).isEmpty()) {
					nah.add(a);
				} else
					fern.add(a);
			}
			if (nah.isEmpty()) {
				return fern;
			}
			return nah;
		}
		return null;
	}

	/**
	 * Unmittelbare Nachbarknoten finden
	 */
	public ArrayList<String> getNeighborVertexes(String start) {
		ArrayList<String> vertexes = new ArrayList<String>();
		String position = start;
		for (LogicBelief p : agent.getAllBeliefs("edge")) {
			if (p.getParameters().get(0).toString().equals(position)) {
				vertexes.add(p.getParameters().get(1).toString());
				continue;
			}
			if (p.getParameters().get(1).toString().equals(position)) {
				vertexes.add(p.getParameters().get(0).toString());
				continue;
			}
		}
		return vertexes;
	}
}