/*
 * @author Artur Daudrich
 * @author Michael Sewell
 */
package eis;

import java.util.Collection;

import eis.exceptions.ActException;
import eis.iilang.Action;
import eis.iilang.Identifier;
import eis.iilang.Numeral;
import eis.iilang.Parameter;
import eis.iilang.Percept;

public class Agent implements AgentListener {

	private EnvironmentInterfaceStandard environmentInterface;

	private String name;
	private String team;
	private String entity;
	private String internalName;

	private Numeral health;
	private Numeral maxHealth;

	private Numeral strength;

	private Numeral energy;
	private Numeral maxEnergy;
	private Numeral maxEnergyDisabled;

	private Identifier position;
	private Numeral visualRange;

	private AgentLogger logger;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTeam() {
		return team;
	}

	public void setTeam(String team) {
		this.team = team;
	}

	public String getEntity() {
		return entity;
	}

	public void setEntity(String entity) {
		this.entity = entity;
	}

	public String getInternalName() {
		return internalName;
	}

	public void setInternalName(String type) {
		logger = new AgentLogger(type);
		this.internalName = type;
	}

	public Numeral getHealth() {
		return health;
	}

	public void setHealth(Numeral health) {
		logger.info(name + ": Health = " + health);
		this.health = health;
	}

	public Numeral getMaxHealth() {
		return maxHealth;
	}

	public boolean isDisabled() {
		return this.health == new Numeral(0);
	}

	public void setMaxHealth(Numeral maxHealth) {
		logger.info(name + ": MaxHealth = " + maxHealth);
		this.maxHealth = maxHealth;
	}

	public Numeral getStrength() {
		return strength;
	}

	public void setStrength(Numeral strength) {
		logger.info(name + ": Stength = " + strength);
		this.strength = strength;
	}

	public Numeral getEnergy() {
		return energy;
	}

	public void setEnergy(Numeral energy) {
		logger.info(name + ": Energy = " + energy);
		this.energy = energy;
	}

	public Numeral getMaxEnergy() {
		return maxEnergy;
	}

	public void setMaxEnergy(Numeral maxEnergy) {
		logger.info(name + ": MaxEnergy = " + maxEnergy);
		this.maxEnergy = maxEnergy;
	}

	public Numeral getMaxEnergyDisabled() {
		return maxEnergyDisabled;
	}

	public void setMaxEnergyDisabled(Numeral maxEnergyDisabled) {
		logger.info(name + ": MaxEnergyDisabled = " + maxEnergyDisabled);
		this.maxEnergyDisabled = maxEnergyDisabled;
	}

	public Identifier getPosition() {
		return position;
	}

	public void setPosition(Identifier position) {
		logger.info(name + ": Position = " + position);
		this.position = position;
	}

	public Numeral getVisualRange() {
		return visualRange;
	}

	public void setVisualRange(Numeral visualRange) {
		logger.info(name + ": VisualRange = " + visualRange);
		this.visualRange = visualRange;
	}

	public EnvironmentInterfaceStandard getEnvironmentInterface() {
		return environmentInterface;
	}

	public void setEnvironmentInterface(
			EnvironmentInterfaceStandard environmentInterface) {
		this.environmentInterface = environmentInterface;
	}

	public void print() {
		System.out.println("[" + team + "]Name: " + name + " Entity: " + entity
				+ " Type: " + internalName);
	}

	public void handlePercept(String agentName, Percept percept) {
		// TODO Auto-generated method stub
		System.out.println("Percept: " + percept.getName() + " - Source: "
				+ percept.getSource());
		for (Parameter param : percept.getParameters()) {
			System.out.println(" -" + param.toString());
		}
		System.out.println("-----------------------------------------------");
		updateAgentState(percept);
	}

	public void handlePercept(String agentName, Collection<Percept> percepts) {
		for (Percept percept : percepts) {
			updateAgentState(percept);
		}
	}

	private void updateAgentState(Percept percept) {
		// update agents values
		Parameter parameter = null;
		if (percept.getParameters().size() > 0) {
			parameter = percept.getParameters().getFirst();
		} else {
			logger.info(percept.getName());
		}
		switch (percept.getName()) {
		case "health":
			this.setHealth((Numeral) parameter);
			break;
		case "maxHealth":
			this.setMaxHealth((Numeral) parameter);
			break;
		case "energy":
			this.setEnergy((Numeral) parameter);
			break;
		case "maxEnergy":
			this.setMaxEnergy((Numeral) parameter);
			break;
		case "maxEnergyDisabled":
			this.setMaxEnergyDisabled((Numeral) parameter);
			break;
		case "strength":
			this.setStrength((Numeral) parameter);
			break;
		case "visRange":
			this.setVisualRange((Numeral) parameter);
			break;
		case "position":
			this.setPosition((Identifier) parameter);
			break;
		}
	}

	public boolean doAction(Action action) {
		try {
			environmentInterface.performAction(this.name, action);
			return true;
		} catch (ActException e) {
			return false;
		}
	}

}
