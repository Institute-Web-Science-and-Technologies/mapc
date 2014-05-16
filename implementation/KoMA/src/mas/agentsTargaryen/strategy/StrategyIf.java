package mas.agentsTargaryen.strategy;

import mas.agentsTargaryen.TargaryenAgent;
import eis.iilang.Action;

public interface StrategyIf {

	public abstract Action execute(TargaryenAgent agent);
}