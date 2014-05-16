package mas.agentsHempelsSofa.algorithms;

import java.util.Collections;
import java.util.LinkedList;

import mas.agentsHempelsSofa.data.graph.Vertex;

public class GeneralAlgorithms {

    public static class SortObject implements Comparable<Object> {

        public Object data;
        private int key;

        public SortObject ( Object data, int key ) {
            this.data = data;
            this.key = key;
        }

        @Override
        public int compareTo ( Object o ) {
            if ( !(o instanceof SortObject) )
                return -2;
            SortObject v = (SortObject) o;
            if ( key < v.key )
                return -1;
            if ( key > v.key )
                return 1;
            return 0;
        }

    }

    public static void sort ( LinkedList<Vertex> criticalFrontierVertices,
            int[] keys ) {
        LinkedList<SortObject> sortObjects = new LinkedList<SortObject>();
        for ( int i = 0; i < criticalFrontierVertices.size(); i++ )
            sortObjects.add(new SortObject(criticalFrontierVertices.get(i),
                    keys[i]));
        Collections.sort(sortObjects);
        criticalFrontierVertices.clear();
        for ( SortObject o : sortObjects )
            criticalFrontierVertices.add((Vertex) o.data);
    }

}
