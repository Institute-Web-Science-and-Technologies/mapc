package eis;

import jason.asSyntax.ListTerm;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.LiteralImpl;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.Term;

import java.util.HashMap;

import eis.iilang.Numeral;
import eis.iilang.Percept;

public class MapAgent {

    private static MapAgent mapAgent;

    private final int maxEdgeWeight = 11;
    private int edges = 1;
    private int vertices = 1;
    private int step = 0;
    private HashMap<String, Vertex> vertexMap = new HashMap<String, Vertex>();

    public static MapAgent getInstance() {
        if (mapAgent == null) {
            mapAgent = new MapAgent();
        }
        return mapAgent;
    }

    public void addPercept(Percept percept) {
        switch (percept.getName()) {
        case "visibleVertex":
            handleVisibleVertex(percept);
            break;
        case "probedVertex":
            handleProbedVertex(percept);
            break;
        // case "visibleEntity":
        // handleVisibleEntity(percept);
        // break;
        case "visibleEdge":
            handleVisibleEdge(percept);
            break;
        case "surveyedEdge":
            handleSurveyedEdge(percept);
            break;
        case "edges":
            edges = ((Numeral) percept.getParameters().get(0)).getValue().intValue();
            break;
        case "vertices":
            vertices = ((Numeral) percept.getParameters().get(0)).getValue().intValue();
            break;
        case "step":
            handleStep(percept);
            break;
        }
    }

    private void handleStep(Percept percept) {
        int newStep = ((Numeral) percept.getParameters().get(0)).getValue().intValue();
        if (newStep > step) {
            step = newStep;
            System.out.println("[" + step + "] TotalEdges: " + edges + ". Total Vertices: " + vertices + ". Known vertices: " + vertexMap.size());
        }
    }

    private void handleSurveyedEdge(Percept percept) {
        String vertexNameA = percept.getParameters().get(0).toString();
        String vertexNameB = percept.getParameters().get(1).toString();
        int edgeWeight = ((Numeral) percept.getParameters().get(2)).getValue().intValue();

        Vertex vertexA = handleVertex(vertexNameA);
        Vertex vertexB = handleVertex(vertexNameB);

        vertexA.setNeighbour(vertexB, edgeWeight);
        vertexB.setNeighbour(vertexA, edgeWeight);
    }

    private void handleVisibleEdge(Percept percept) {
        String vertexNameA = percept.getParameters().get(0).toString();
        String vertexNameB = percept.getParameters().get(1).toString();

        Vertex vertexA = handleVertex(vertexNameA);
        Vertex vertexB = handleVertex(vertexNameB);

        vertexA.setNeighbour(vertexB, maxEdgeWeight);
        vertexB.setNeighbour(vertexA, maxEdgeWeight);
    }

    private void handleProbedVertex(Percept percept) {
        String vertexName = percept.getParameters().get(0).toString();
        int vertexValue = ((Numeral) percept.getParameters().get(1)).getValue().intValue();

        Vertex vertex = handleVertex(vertexName);
        vertex.setValue(vertexValue);
    }

    private void handleVisibleVertex(Percept percept) {
        String vertexName = percept.getParameters().get(0).toString();
        String team = percept.getParameters().get(1).toString();

        Vertex vertex = handleVertex(vertexName);
        vertex.setTeam(team);
    }

    private Vertex handleVertex(String name) {
        Vertex vertex;
        if (!vertexMap.containsKey(name)) {
            vertex = new Vertex(name);
            vertexMap.put(name, vertex);
        } else {
            vertex = vertexMap.get(name);
        }
        return vertex;
    }

    public Term getNextUnsurveyedVertices(String position) {
        ListTerm result = new ListTermImpl();
        if (vertexMap.containsKey(position)) {
            Vertex vertex = vertexMap.get(position);
            HashMap<Vertex, Integer> unsurveyedVertices = vertex.getNextUnsurveyedVertices(1);
            for (Vertex v : unsurveyedVertices.keySet()) {
                ListTerm object = new ListTermImpl();
                object.add(new NumberTermImpl(unsurveyedVertices.get(v)));
                object.add(new LiteralImpl(v.getIdentifier()));
                result.add(object);
            }
        }
        return result;
    }
}
