/*
 * @author Artur Daudrich
 * @author Michael Sewell
 */
package eis;

import java.util.HashSet;

import eis.iilang.Identifier;
import eis.iilang.Numeral;
import eis.iilang.TruthValue;

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

	private AgentLogger logger = new AgentLogger(SimulationState.class.getCanonicalName());

	private static SimulationState instance = null;
	
    private SimulationState() {
    	
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

	public void setStep(Numeral step) {
		logger.info("Step = " + step);
		this.step = step;
	}

	public Numeral getMaxSteps() {
		return maxSteps;
	}

	public void setMaxSteps(Numeral maxSteps) {
		logger.info("MaxSteps = " + maxSteps);
		this.maxSteps = maxSteps;
	}

	public Numeral getLastTimeStamp() {
		return lastTimeStamp;
	}

	public void setLastTimeStamp(Numeral lastTimeStamp) {
		logger.info("TimeStamp = " + lastTimeStamp);
		this.lastTimeStamp = lastTimeStamp;
	}

	public Numeral getDeadline() {
		return deadline;
	}

	public void setDeadline(Numeral deadline) {
		logger.info("Deadline = " + deadline);
		this.deadline = deadline;
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

	public void setEdgeCount(Numeral edgeCount) {
		logger.info("EdgeCount = " + edgeCount);
		this.edgeCount = edgeCount;
	}

	public Numeral getVerticesCount() {
		return verticesCount;
	}

	public void setVerticesCount(Numeral verticesCount) {
		logger.info("VerticesCount = " + verticesCount);
		this.verticesCount = verticesCount;
	}

	public Identifier getId() {
		return id;
	}

	public void setId(Identifier id) {
		logger.info("Id = " + id);
		this.id = id;
	}

	public Numeral getLastStepScore() {
		return lastStepScore;
	}

	public void setLastStepScore(Numeral lastStepScore) {
		logger.info("LastStepScore = " + lastStepScore);
		this.lastStepScore = lastStepScore;
	}

	public Numeral getPredictedStepScore() {
		return predictedStepScore;
	}

	public void setPredictedStepScore(Numeral predictedStepScore) {
		logger.info("PredictedStepScore = " + predictedStepScore);
		this.predictedStepScore = predictedStepScore;
	}

	public Numeral getScore() {
		return score;
	}

	public void setScore(Numeral score) {
		logger.info("Score = " + score);
		this.score = score;
	}

	public Numeral getMoney() {
		return money;
	}

	public void setMoney(Numeral money) {
		logger.info("Money = " + money);
		this.money = money;
	}

	public Numeral getRanking() {
		return ranking;
	}

	public void setRanking(Numeral ranking) {
		logger.info("Ranking = " + ranking);
		this.ranking = ranking;
	}

	@SuppressWarnings("unchecked")
	public HashSet<Identifier> getAchievements() {
		return (HashSet<Identifier>) achievements.clone();
	}

	public boolean addAchievement(Identifier achievement) {
		logger.info("new Achievement: " + achievement);
		return achievements.add(achievement);
	}
}
