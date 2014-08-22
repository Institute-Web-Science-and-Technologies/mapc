package eis;

import jason.asSyntax.ListTerm;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.StringTermImpl;
import jason.asSyntax.Term;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

public class JasonHelper {

    public static Term getTerm(String string) {
        return StringTermImpl.parseString(string);
    }

    public static Term getTerm(boolean bool) {
        return StringTermImpl.parseString(String.valueOf(bool));
    }

    public static Term getTerm(double value) {
        return StringTermImpl.parseString(String.valueOf(value));
    }

    public static Term getTerm(ArrayList<Vertex> vertices) {
        ListTerm listTerm = new ListTermImpl();
        for (Vertex vertex : vertices) {
            listTerm.add(getTerm(vertex.getIdentifier()));
        }
        return listTerm;

    }

    public static Term getTerm(HashMap<String, Vertex> positions) {
        ListTerm outerListTerm = new ListTermImpl();
        for (Entry<String, Vertex> entry : positions.entrySet()) {
            ListTerm innerListTerm = new ListTermImpl();
            innerListTerm.add(getTerm(entry.getKey()));
            innerListTerm.add(getTerm(entry.getValue()));
            outerListTerm.add(innerListTerm);
        }
        return outerListTerm;

    }

    public static Term getTerm(Vertex vertex) {
        return getTerm(vertex.getIdentifier());
    }

}
