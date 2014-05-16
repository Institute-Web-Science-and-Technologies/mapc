package mas.agentsHempelsSofa.data;

import apltk.interpreter.data.LogicBelief;

/**
 * An interface fo all classes which can be converted to a
 * {@link apltk.interpreter.data.LogicBelief}.
 * @author Hempels-Sofa
 */
public interface Believable {

    /**
     * @return a logic belief which contains all important information of the
     *         object.
     */
    LogicBelief toBelief ();

}
