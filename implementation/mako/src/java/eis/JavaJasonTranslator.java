package eis;

import jason.asSyntax.Literal;
import eis.iilang.Percept;

/**
 * This class adds the functionality to translate between Java percepts and
 * Jason literals.
 * 
 * @author Michael Sewell
 * 
 */
public class JavaJasonTranslator {
    public static Literal perceptToLiteral(Percept percept) {
        Literal literal = Literal.parseLiteral(percept.toString());
        return literal;
    }
}
