package eis;

import java.util.HashSet;
import java.util.LinkedList;

import eis.iilang.Identifier;
import eis.iilang.Numeral;
import eis.iilang.Parameter;
import eis.iilang.TruthValue;

/**
 * @author Artur Daudrich
 * @author Michael Sewell
 */
public class SimulationState {
    private Numeral step;
    private Numeral maxSteps;

    private Numeral lastTimeStamp;
    private Numeral deadline;
    private TruthValue isTournamentOver;

    private Numeral edgeCount;
    private Numeral verticesCount;
    private Identifier id;

    private Numeral lastStepScore;
    private Numeral predictedStepScore;
    private Numeral score;
    private HashSet<Identifier> achievements = new HashSet<Identifier>();

    private Numeral money;
    private Numeral ranking;

    public static String NAME = SimulationState.class.getName();
    AgentLogger logger = new AgentLogger(SimulationState.NAME);

    private static SimulationState instance = null;

    private SimulationState() {
        logger.setVisible(false);
    }

    public static SimulationState getInstance() {
        if (instance == null) {
            instance = new SimulationState();
        }
        return instance;
    }

    public Numeral getStep() {
        return step;
    }

    public void setStep(LinkedList<Parameter> parameters) {
        Numeral newValue = (Numeral) parameters.get(0);
        if (!newValue.equals(this.step)) {
            logger.info("Step:" + step + "->" + newValue);
            this.step = newValue;
        }
    }

    public Numeral getMaxSteps() {
        return maxSteps;
    }

    public void setMaxSteps(LinkedList<Parameter> parameters) {
        Numeral newValue = (Numeral) parameters.get(0);
        if (!newValue.equals(this.maxSteps)) {
            logger.info("Max Steps:" + maxSteps + "->" + newValue);
            this.maxSteps = newValue;
        }
    }

    public Numeral getLastTimeStamp() {
        return lastTimeStamp;
    }

    public void setLastTimeStamp(LinkedList<Parameter> parameters) {
        Numeral newValue = (Numeral) parameters.get(0);
        if (!newValue.equals(this.lastTimeStamp)) {
            logger.info("Last Timestamp:" + this.lastTimeStamp + "->" + newValue);
            this.lastTimeStamp = newValue;
        }
    }

    public Numeral getDeadline() {
        return deadline;
    }

    public void setDeadline(LinkedList<Parameter> parameters) {
        Numeral newValue = (Numeral) parameters.get(0);
        if (!newValue.equals(this.deadline)) {
            logger.info("Deadline:" + this.deadline + "->" + newValue);
            this.deadline = newValue;
        }
    }

    public TruthValue getIsTournamentOver() {
        return isTournamentOver;
    }

    public void setIsTournamentOver(TruthValue isTournamentOver) {
        logger.info("Tournament is over.");
        this.isTournamentOver = isTournamentOver;
    }

    public Numeral getEdgeCount() {
        return edgeCount;
    }

    public void setEdgeCount(LinkedList<Parameter> parameters) {
        Numeral newValue = (Numeral) parameters.get(0);
        if (!newValue.equals(this.deadline)) {
            logger.info("Number of edges:" + this.edgeCount + "->" + newValue);
            this.edgeCount = newValue;
        }
    }

    public Numeral getVerticesCount() {
        return verticesCount;
    }

    public void setVerticesCount(LinkedList<Parameter> parameters) {
        Numeral newValue = (Numeral) parameters.get(0);
        if (!newValue.equals(this.verticesCount)) {
            logger.info("Number of vertices:" + this.verticesCount + "->" + newValue);
            this.verticesCount = newValue;
        }
    }

    public Identifier getId() {
        return id;
    }

    public void setId(LinkedList<Parameter> parameters) {
        Identifier newValue = (Identifier) parameters.get(0);
        if (!newValue.equals(this.id)) {
            logger.info("id:" + this.id + "->" + newValue);
            this.id = newValue;
        }
    }

    public Numeral getLastStepScore() {
        return lastStepScore;
    }

    public void setLastStepScore(LinkedList<Parameter> parameters) {
        Numeral newValue = (Numeral) parameters.get(0);
        if (!newValue.equals(this.lastStepScore)) {
            logger.info("Score of last step:" + this.lastStepScore + "->" + newValue);
            this.lastStepScore = newValue;
        }
    }

    public Numeral getPredictedStepScore() {
        return predictedStepScore;
    }

    public void setPredictedStepScore(Numeral newValue) {
        if (!newValue.equals(this.predictedStepScore)) {
            logger.info("Predicted score of next step: " + newValue);
            this.predictedStepScore = newValue;
        }
    }

    public Numeral getScore() {
        return score;
    }

    public void setScore(LinkedList<Parameter> parameters) {
        Numeral newValue = (Numeral) parameters.get(0);
        if (!newValue.equals(this.score)) {
            logger.info("Score:" + this.score + "->" + newValue);
            this.score = newValue;
        }
    }

    public Numeral getMoney() {
        return money;
    }

    public void setMoney(LinkedList<Parameter> parameters) {
        Numeral newValue = (Numeral) parameters.get(0);
        if (!newValue.equals(this.score)) {
            logger.info("Money:" + this.money + "->" + newValue);
            this.money = newValue;
        }
    }

    public Numeral getRanking() {
        return ranking;
    }

    public void setRanking(LinkedList<Parameter> parameters) {
        Numeral newValue = (Numeral) parameters.get(0);
        if (!newValue.equals(this.ranking)) {
            logger.info("Ranking:" + this.ranking + "->" + newValue);
            this.ranking = newValue;
        }
    }

    @SuppressWarnings("unchecked")
    public HashSet<Identifier> getAchievements() {
        return (HashSet<Identifier>) achievements.clone();
    }

    public boolean addAchievement(LinkedList<Parameter> parameters) {
        Identifier newValue = (Identifier) parameters.get(0);
        if (!achievements.contains(newValue)) {
            logger.info("new Achievement: " + newValue);
            return achievements.add(newValue);
        }
        return false;
    }
}
