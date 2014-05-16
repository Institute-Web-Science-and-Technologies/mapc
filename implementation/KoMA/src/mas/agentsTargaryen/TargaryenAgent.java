package mas.agentsTargaryen;

import java.util.Collection;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Vector;

import mas.agentsTargaryen.strategy.DefendZoningStrategy;
import mas.agentsTargaryen.strategy.DodgeSaboteurStrategy;
import mas.agentsTargaryen.strategy.ExplorerExploreStrategy;
import mas.agentsTargaryen.strategy.InspectorInspectStrategy;
import mas.agentsTargaryen.strategy.LearnPerceptsStrategy;
import mas.agentsTargaryen.strategy.NeedRepairStrategy;
import mas.agentsTargaryen.strategy.ReadMessagesStrategy;
import mas.agentsTargaryen.strategy.RechargeStrategy;
import mas.agentsTargaryen.strategy.RepairerRepairStrategy;
import mas.agentsTargaryen.strategy.RethinkGoalsStrategy;
import mas.agentsTargaryen.strategy.SaboteurAttackStrategy;
import mas.agentsTargaryen.strategy.SentinelWardStrategy;
import mas.agentsTargaryen.strategy.SkipStrategy;
import mas.agentsTargaryen.strategy.StrategyIf;
import mas.agentsTargaryen.strategy.SurveyStrategy;
import mas.agentsTargaryen.strategy.ZoningStrategy;
import massim.javaagents.Agent;
import apltk.interpreter.data.LogicBelief;
import apltk.interpreter.data.LogicGoal;
import apltk.interpreter.data.Message;
import eis.iilang.Action;
import eis.iilang.Percept;

// BUGS & TODO
//
// - Exploring darf nicht so viel earlygame wegnehmen
// - Brauchen bessere Zoning-Algos, insbesondere frs earlygame. Erforschen kann man auch als Zone noch
// - Weitere Buy-Strategien auer Ward? Oder doch lieber Geld->Score?
public class TargaryenAgent extends Agent {

	private TargaryenUtil util;
	private Vector<StrategyIf> strategies;

	public TargaryenAgent(String name, String team) {
		super(name, team);
		util = new TargaryenUtil(this);
		strategies = new Vector<StrategyIf>();
		strategies.add(new LearnPerceptsStrategy());
		strategies.add(new ReadMessagesStrategy());
		strategies.add(new RethinkGoalsStrategy());
		strategies.add(new NeedRepairStrategy());
		strategies.add(new RepairerRepairStrategy());
		strategies.add(new DodgeSaboteurStrategy());
		strategies.add(new RechargeStrategy());
		strategies.add(new InspectorInspectStrategy());
		strategies.add(new SaboteurAttackStrategy());
		strategies.add(new SentinelWardStrategy());
		strategies.add(new DefendZoningStrategy());
		strategies.add(new ZoningStrategy());
		strategies.add(new ExplorerExploreStrategy());
		strategies.add(new SurveyStrategy());
		strategies.add(new SkipStrategy());
	}

	@Override
	public Action step() {
		Action action = null;
		try {
			for (StrategyIf strategy : strategies) {
				action = strategy.execute(this);
				if (action != null) {
					break;
				}
			}
		} catch (NoSuchElementException e) {
			System.err.println("Fehler... ist der Server nicht an?");
		}
		System.out.println(getName() + ": " + action);
		return action;
	}

	public TargaryenUtil getUtil() {
		return util;
	}

	public int getTotalSteps() {
		if (getAllBeliefs("totalSteps").isEmpty()) {
			return -1;
		}
		return Integer.parseInt(getAllBeliefs("totalSteps").getFirst().getParameters().get(0));
	}

	public int getTotalEdges() {
		if (getAllBeliefs("totalEdges").isEmpty()) {
			return -1;
		}
		return Integer.parseInt(getAllBeliefs("totalEdges").getFirst().getParameters().get(0));
	}

	public int getTotalVertices() {
		if (getAllBeliefs("totalVertices").isEmpty()) {
			return -1;
		}
		return Integer.parseInt(getAllBeliefs("totalVertices").getFirst().getParameters().get(0));
	}

	public String getRole() {
		if (getAllBeliefs("role").isEmpty()) {
			return null;
		}
		return getAllBeliefs("role").getFirst().getParameters().get(0);
	}

	public String getEnemyTeam() {
		if (getAllBeliefs("enemyTeam").isEmpty()) {
			return null;
		}
		return getAllBeliefs("enemyTeam").getFirst().getParameters().get(0);
	}

	public String getPosition() {
		return getPosition(getName());
	}

	public String getPosition(String agent) {
		if (getAllBeliefs("position", "", agent).isEmpty()) {
			return null;
		}
		return getAllBeliefs("position", "", agent).getFirst().getParameters().get(0);
	}

	public int getMoney() {
		if (getAllBeliefs("money").isEmpty()) {
			return -1;
		}
		return Integer.parseInt(getAllBeliefs("money").getFirst().getParameters().get(0));
	}

	public int getStrength() {
		if (getAllBeliefs("strength").isEmpty()) {
			return -1;
		}
		return Integer.parseInt(getAllBeliefs("strength").getFirst().getParameters().get(0));
	}

	public boolean isDisabled() {
		return (getHealth() == 0);
	}

	public int getHealth() {
		if (getAllBeliefs("health").isEmpty()) {
			return -1;
		}
		return Integer.parseInt(getAllBeliefs("health").getFirst().getParameters().get(0));
	}

	public int getMaxHealth() {
		if (getAllBeliefs("maxHealth").isEmpty()) {
			return -1;
		}
		return Integer.parseInt(getAllBeliefs("maxHealth").getFirst().getParameters().get(0));
	}

	public int getEnergy() {
		if (getAllBeliefs("energy").isEmpty()) {
			return -1;
		}
		return Integer.parseInt(getAllBeliefs("energy").getFirst().getParameters().get(0));
	}

	public int getMaxEnergy() {
		if (getAllBeliefs("maxEnergy").isEmpty()) {
			return -1;
		}
		return Integer.parseInt(getAllBeliefs("maxEnergy").getFirst().getParameters().get(0));
	}

	public int getVisRange() {
		if (getAllBeliefs("visRange").isEmpty()) {
			return -1;
		}
		return Integer.parseInt(getAllBeliefs("visRange").getFirst().getParameters().get(0));
	}

	public int getScore() {
		if (getAllBeliefs("score").isEmpty()) {
			return -1;
		}
		return Integer.parseInt(getAllBeliefs("score").getFirst().getParameters().get(0));
	}

	public int getZoneScore() {
		if (getAllBeliefs("zoneScore").isEmpty()) {
			return -1;
		}
		return Integer.parseInt(getAllBeliefs("zoneScore").getFirst().getParameters().get(0));
	}

	public int getZonesScore() {
		if (getAllBeliefs("zonesScore").isEmpty()) {
			return -1;
		}
		return Integer.parseInt(getAllBeliefs("zonesScore").getFirst().getParameters().get(0));
	}

	public int getLastStepScore() {
		if (getAllBeliefs("lastStepScore").isEmpty()) {
			return -1;
		}
		return Integer.parseInt(getAllBeliefs("lastStepScore").getFirst().getParameters().get(0));
	}

	public int getStep() {
		if (getAllBeliefs("step").isEmpty()) {
			return -1;
		}
		return Integer.parseInt(getAllBeliefs("step").getFirst().getParameters().get(0));
	}

	@Override
	public void handlePercept(Percept p) {
	}

	public LinkedList<LogicGoal> getAllGoals(String predicate) {
		LinkedList<LogicGoal> ret = new LinkedList<LogicGoal>();
		for (LogicGoal g : goals) {
			if (g.getPredicate().equals(predicate))
				ret.add(g);
		}
		return ret;
	}
	
	public LinkedList<LogicBelief> getAllBeliefs(String name, String attribut1) {
		LinkedList<LogicBelief> beliefs = new LinkedList<LogicBelief>();
		if (attribut1.equals("")) {
			beliefs = getAllBeliefs(name);
		} else {
			if (name.equals("")) {
				for (LogicBelief b : getBeliefBase()) {
					if (b.getParameters().get(0).equals(attribut1)) {
						beliefs.add(b);
					}
				}
			} else {
				for (LogicBelief b : getAllBeliefs(name)) {
					if (b.getParameters().get(0).equals(attribut1)) {
						beliefs.add(b);
					}
				}
			}
		}
		return beliefs;
	}

	public LinkedList<LogicBelief> getAllBeliefs(String name, String attribut1, String attribut2) {
		LinkedList<LogicBelief> beliefs = new LinkedList<LogicBelief>();
		if (attribut2.equals(""))
			beliefs = getAllBeliefs(name, attribut1);
		else {
			if (name.equals("")) {
				for (LogicBelief b : getBeliefBase()) {
					if ((b.getParameters().get(0).equals(attribut1) || (attribut1.equals("")))
							&& (b.getParameters().get(1).equals(attribut2))) {
						beliefs.add(b);
					}
				}
			} else {
				for (LogicBelief b : getAllBeliefs(name)) {
					if ((b.getParameters().get(0).equals(attribut1) || (attribut1.equals("")))
							&& (b.getParameters().get(1).equals(attribut2))) {
						beliefs.add(b);
					}
				}
			}
		}
		return beliefs;
	}

	public LinkedList<LogicBelief> getAllBeliefs(String name, String attribut1, String attribut2, String attribut3) {
		LinkedList<LogicBelief> beliefs = new LinkedList<LogicBelief>();
		if (attribut3.equals(""))
			beliefs = getAllBeliefs(name, attribut1, attribut2);
		else {
			if (name.equals("")) {
				for (LogicBelief b : getBeliefBase()) {
					if ((b.getParameters().get(0).equals(attribut1) || (attribut1.equals("")))
							&& (b.getParameters().get(1).equals(attribut2) || attribut2.equals(""))
							&& (b.getParameters().get(2).equals(attribut3))) {
						beliefs.add(b);
					}
				}
			} else {
				for (LogicBelief b : getAllBeliefs(name)) {
					if ((b.getParameters().get(0).equals(attribut1) || (attribut1.equals("")))
							&& (b.getParameters().get(1).equals(attribut2) || attribut2.equals(""))
							&& (b.getParameters().get(2).equals(attribut3))) {
						beliefs.add(b);
					}
				}
			}
		}
		return beliefs;
	}

	public void removeBeliefs(String predicate, String attribut1) {

		LinkedList<LogicBelief> remove = new LinkedList<LogicBelief>();

		for (LogicBelief b : beliefs) {
			if ((b.getPredicate().equals(predicate) || predicate.equals(""))
					&& (b.getParameters().get(0).equals(attribut1) || attribut1.equals("")))
				remove.add(b);
		}

		beliefs.removeAll(remove);

	}

	public void removeBeliefs(String predicate, String attribut1, String attribut2) {

		LinkedList<LogicBelief> remove = new LinkedList<LogicBelief>();

		for (LogicBelief b : beliefs) {
			if ((b.getPredicate().equals(predicate) || predicate.equals(""))
					&& (b.getParameters().get(0).equals(attribut1) || attribut1.equals(""))
					&& (b.getParameters().get(1).equals(attribut2) || attribut2.equals("")))
				remove.add(b);
		}

		beliefs.removeAll(remove);

	}

	public void removeBeliefs(String predicate, String attribut1, String attribut2, String attribut3) {

		LinkedList<LogicBelief> remove = new LinkedList<LogicBelief>();

		for (LogicBelief b : beliefs) {
			if ((b.getPredicate().equals(predicate) || predicate.equals(""))
					&& (b.getParameters().get(0).equals(attribut1) || attribut1.equals(""))
					&& (b.getParameters().get(1).equals(attribut2) || attribut2.equals(""))
					&& (b.getParameters().get(2).equals(attribut3) || attribut3.equals("")))
				remove.add(b);
		}

		beliefs.removeAll(remove);
	}
	
	//FIXME
	public Collection<Percept> retrieveAllPercepts(){
		return getAllPercepts();
	}
	
	
	public void updateBelief(LogicBelief zuErsetzen, LogicBelief ersetzer) {
		LinkedList<LogicBelief> remove = new LinkedList<LogicBelief>();
		if (beliefs.contains(zuErsetzen))
			remove.add(zuErsetzen);
		beliefs.removeAll(remove);

		if (!beliefs.contains(ersetzer))
			beliefs.add(ersetzer);
	}
	
	//FIXME
	public Collection<Message> retrieveMessages() {
		return getMessages();
	}
	
	//FIXME
	public void deleteGoals(String string) {
		removeGoals(string);
	}
	
}