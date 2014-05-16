package mas.agents0815;

import java.util.LinkedList;

import eis.iilang.Action;
import eis.iilang.Parameter;

public class ExternalAction extends Action {

	public ExternalAction(String name){
		super(name);
	}

	public ExternalAction(String name, LinkedList<Parameter> parameters) {
		super(name, parameters);
	}

	public ExternalAction(String name, Parameter... parameters) {
		super(name, parameters);
	}


}
