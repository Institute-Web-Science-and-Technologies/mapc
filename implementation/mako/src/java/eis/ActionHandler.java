package eis;

import eis.iilang.Action;
import eis.iilang.Identifier;

/**
 * @author Artur Daudrich
 * @author Michael Sewell
 */
public class ActionHandler {
    /**
     * Yields a valid goto-action.
     * 
     * @param nodeName
     * @return
     */
    static public Action goTo(String nodeName) {
        return new Action("goto", new Identifier(nodeName));
    }

    /**
     * Yields a valid skip-action.
     * 
     * @return
     */
    static public Action skip() {
        return new Action("skip");
    }

    /**
     * Yields a valid probe-action.
     * 
     * @return
     */
    static public Action probe() {
        return new Action("probe");
    }

    /**
     * Yields a valid probe-action for a specific node.
     * 
     * @return
     */
    static public Action probe(String nodeName) {
        return new Action("probe", new Identifier(nodeName));
    }

    /**
     * Yields a valid survey-action.
     * 
     * @return
     */
    static public Action survey() {
        return new Action("survey");
    }

    /**
     * Yields a valid inspect-action.
     * 
     * @return
     */
    static public Action inspect() {
        return new Action("inspect");
    }

    /**
     * Yields a valid inspect-action for a specific agent.
     * 
     * @return
     */
    static public Action inspect(String agentName) {
        return new Action("inspect", new Identifier(agentName));
    }

    /**
     * Yields a valid parry-action.
     * 
     * @return
     */
    public static Action parry() {
        return new Action("parry");
    }

    /**
     * Yields a valid attack action.
     * 
     * @param entityName
     * @return
     */
    static public Action attack(String entityName) {
        return new Action("attack", new Identifier(entityName));
    }

    /**
     * Yields a valid buy action.
     * 
     * @param item
     * @return
     */
    static public Action buy(String item) {
        return new Action("buy", new Identifier(item));
    }

    /**
     * Yields a valid repair action.
     * 
     * @param entity
     * @return
     */
    static public Action repair(String entity) {
        return new Action("repair", new Identifier(entity));
    }

    /**
     * Yields a valid recharge action.
     * 
     * @param entity
     * @return
     */
    static public Action recharge() {
        return new Action("recharge");
    }

}
