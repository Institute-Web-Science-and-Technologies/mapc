package eis;

import java.util.Collection;
import java.util.LinkedList;

import eis.exceptions.ActException;
import eis.iilang.Action;
import eis.iilang.Identifier;
import eis.iilang.Numeral;
import eis.iilang.Parameter;
import eis.iilang.Percept;

/**
 * @author Artur Daudrich
 * @author Michael Sewell
 */
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

    private Identifier lastActionResult;
    private Identifier lastActionParam;
    private Identifier lastAction;

    private AgentLogger logger;

    public EnvironmentInterfaceStandard getEnvironmentInterface() {
        return environmentInterface;
    }

    public void setEnvironmentInterface(
            EnvironmentInterfaceStandard environmentInterface) {
        this.environmentInterface = environmentInterface;
    }

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

    public void setHealth(LinkedList<Parameter> parameters) {
        Numeral newValue = (Numeral) parameters.get(0);
        if (!newValue.equals(this.health)) {
            logger.info("Health: " + this.health + " -> " + newValue);
            this.health = newValue;
        }
    }

    public Numeral getMaxHealth() {
        return maxHealth;
    }

    public boolean isDisabled() {
        return this.health == new Numeral(0);
    }

    public void setMaxHealth(LinkedList<Parameter> parameters) {
        Numeral newValue = (Numeral) parameters.get(0);
        if (!newValue.equals(this.maxHealth)) {
            logger.info("MaxHealth: " + newValue);
            this.maxHealth = newValue;
        }
    }

    public Numeral getStrength() {
        return strength;
    }

    public void setStrength(LinkedList<Parameter> parameters) {
        Numeral newValue = (Numeral) parameters.get(0);
        if (!newValue.equals(this.strength)) {
            logger.info("Stength: " + newValue);
            this.strength = newValue;
        }
    }

    public Numeral getEnergy() {
        return energy;
    }

    public void setEnergy(LinkedList<Parameter> parameters) {
        Numeral newValue = (Numeral) parameters.get(0);
        if (!newValue.equals(this.energy)) {
            logger.info("Energy: " + this.energy + " -> " + newValue);
            this.energy = newValue;
        }
    }

    public Numeral getMaxEnergy() {
        return maxEnergy;
    }

    public void setMaxEnergy(LinkedList<Parameter> parameters) {
        Numeral newValue = (Numeral) parameters.get(0);
        if (!newValue.equals(this.maxEnergy)) {
            logger.info("MaxEnergy: " + newValue);
            this.maxEnergy = newValue;
        }
    }

    public Numeral getMaxEnergyDisabled() {
        return maxEnergyDisabled;
    }

    public void setMaxEnergyDisabled(LinkedList<Parameter> parameters) {
        Numeral newValue = (Numeral) parameters.get(0);
        if (!newValue.equals(this.maxEnergyDisabled)) {
            logger.info("MaxEnergy when disabled: " + newValue);
            this.maxEnergyDisabled = newValue;
        }
    }

    public Identifier getPosition() {
        return position;
    }

    public void setPosition(LinkedList<Parameter> parameters) {
        Identifier newValue = (Identifier) parameters.get(0);
        if (!newValue.equals(this.position)) {
            logger.info("Position: " + newValue);
            this.position = newValue;
        }
    }

    public Numeral getVisualRange() {
        return visualRange;
    }

    public void setVisualRange(LinkedList<Parameter> parameters) {
        Numeral newValue = (Numeral) parameters.get(0);
        if (!newValue.equals(this.visualRange)) {
            logger.info("VisualRange: " + newValue);
            this.visualRange = newValue;
        }
    }

    private void setLastActionResult(LinkedList<Parameter> parameters) {
        Identifier newValue = (Identifier) parameters.get(0);
        if (!newValue.equals(this.lastActionResult)) {
            logger.info("Result of last action: " + newValue);
            this.lastActionResult = newValue;
        }
    }

    public Identifier getLastActionResult() {
        return lastActionResult;
    }

    private void setLastActionParam(LinkedList<Parameter> parameters) {
        Identifier newValue = (Identifier) parameters.get(0);
        if (!newValue.equals(this.lastActionParam)) {
            logger.info("Parameter of last action: " + newValue);
            this.lastActionParam = newValue;
        }
    }

    public Identifier getLastActionParam() {
        return lastActionParam;
    }

    private void setLastAction(LinkedList<Parameter> parameters) {
        Identifier newValue = (Identifier) parameters.get(0);
        if (!newValue.equals(this.lastAction)) {
            logger.info("Last action: " + newValue);
            this.lastAction = newValue;
        }
    }

    public Identifier getLastAction() {
        return lastAction;
    }

    public void print() {
        logger.info("[" + team + "]Name: " + name + " Entity: " + entity + " Type: " + internalName);
    }

    public void handlePercept(String agentName, Percept percept) {
        logger.info("single percept");
        updateAgentState(percept);
    }

    public void handlePercept(String agentName, Collection<Percept> percepts) {
        for (Percept percept : percepts) {
            updateAgentState(percept);
        }
    }

    private void updateAgentState(Percept percept) {
        // update agents values
        logger.info(percept.getName());

        switch (percept.getName()) {
        /* health(<Numeral>) indicates the current health of the vehicle. */
        case "health":
            this.setHealth(percept.getParameters());
            break;
        /*
         * maxHealth(<Numeral>) represents the maximum health the vehicle can
         * have.
         */
        case "maxHealth":
            this.setMaxHealth(percept.getParameters());
            break;
        /*
         * energy(<Numeral>) denotes the current amount of energy of the
         * vehicle.
         */
        case "energy":
            this.setEnergy(percept.getParameters());
            break;
        /*
         * maxEnergy(<Numeral>) denotes the maximum amount of energy the
         * vehicle. can have
         */
        case "maxEnergy":
            this.setMaxEnergy(percept.getParameters());
            break;
        /*
         * maxEnergyDisabled(<Numeral>) denotes the maximum amount of energy the
         * vehicle can have, when it is disabled.
         */
        case "maxEnergyDisabled":
            this.setMaxEnergyDisabled(percept.getParameters());
            break;
        /* strength(<Numeral>) represents the current strength of the vehicle. */
        case "strength":
            this.setStrength(percept.getParameters());
            break;
        /*
         * visRange(<Numeral>) denotes the current visibility-range of the
         * vehicle.
         */
        case "visRange":
            this.setVisualRange(percept.getParameters());
            break;
        /*
         * position(<Identifier>) indicates the current position of the vehicle.
         * The identifier is the vertex’s name.
         */
        case "position":
            this.setPosition(percept.getParameters());
            // Graph.getInstance().addPosition(this.getPosition(), this);
            break;
        /*
         * lastAction(<Identifier>) indicates the last action that was sent to
         * the server.
         */
        case "lastAction":
            this.setLastAction(percept.getParameters());
            break;
        /*
         * lastActionParam(<Identifier>) indicates the parameter of the last ac-
         * tion that was sent to the server.
         */
        case "lastActionParam":
            this.setLastActionParam(percept.getParameters());
            break;
        /*
         * lastActionResult(<Identifier>) indicates the outcome of the last
         * action.
         */
        case "lastActionResult":
            this.setLastActionResult(percept.getParameters());
            break;
        }
    }

    public boolean doAction(Action action) {
        try {
            environmentInterface.performAction(this.name, action);
            logger.info(action.getName());
            return true;
        } catch (ActException e) {
            return false;
        }
    }

}
