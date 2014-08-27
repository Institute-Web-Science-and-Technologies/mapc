package eis;

import jason.asSyntax.ASSyntax;
import jason.asSyntax.ListTerm;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.Term;
import jason.asSyntax.parser.ParseException;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class JasonHelper {

    public static Term getTerm(String string) {
        try {
            return ASSyntax.parseTerm(string);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public static Term getTerm(boolean bool) {
        try {
            return ASSyntax.parseTerm(String.valueOf(bool));
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public static Term getTerm(double value) {
        try {
            return ASSyntax.parseTerm(String.valueOf(value));
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public static Term getTerm(List<Vertex> vertices) {
        ListTerm listTerm = new ListTermImpl();
        for (Vertex vertex : vertices) {
            listTerm.add(getTerm(vertex.getIdentifier()));
        }
        return listTerm;
    }

    public static Term getStringList(List<String> list) {
        ListTerm listTerm = new ListTermImpl();
        for (String item : list) {
            listTerm.add(getTerm(item));
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
