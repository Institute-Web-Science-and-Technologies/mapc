package mas.agents0815;

import java.util.LinkedList;

import eis.iilang.Action;
import eis.iilang.Parameter;

public class InternalAction extends Action{

	/**
	 * @param name
	 * @param parameters
	 */
	public InternalAction(String name, LinkedList<Parameter> parameters) {
		super(name, parameters);
	}

	public InternalAction(String name, Parameter... parameters) {
		super(name, parameters);
	}

	public InternalAction(String name) {
		super(name);
	}




}
