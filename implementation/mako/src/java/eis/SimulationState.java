package eis;

import java.util.HashSet;
import java.util.logging.Logger;

import eis.iilang.Identifier;
import eis.iilang.Numeral;
import eis.iilang.TruthValue;

public class SimulationState {
	private static Numeral step;
	private static Numeral maxSteps;
	
	private static Numeral lastTimeStamp;
	private static Numeral deadline;
	private static TruthValue isTournamentOver;
		
	private static Numeral edgeCount;
	private static Numeral verticesCount;
	private static Identifier id;
	
	private static Numeral lastStepScore;
	private static Numeral predictedStepScore;	
	private static Numeral score;
	private static HashSet<Identifier> achievements = new HashSet<Identifier>();
	
	private static Numeral money;
	private static Numeral ranking;

	private static final Logger logger = Logger.getLogger(SimulationState.class.getCanonicalName());
	
	public static Numeral getStep() {
		return step;
	}
	public static void setStep(Numeral step) {
		logger.info("Step = " +step);
		SimulationState.step = step;
	}
	public static Numeral getMaxSteps() {
		return maxSteps;
	}
	public static void setMaxSteps(Numeral maxSteps) {
		logger.info("MaxSteps = " +maxSteps);
		SimulationState.maxSteps = maxSteps;
	}
	public static Numeral getLastTimeStamp() {
		return lastTimeStamp;
	}
	public static void setLastTimeStamp(Numeral lastTimeStamp) {
		logger.info("TimeStamp = " +lastTimeStamp);
		SimulationState.lastTimeStamp = lastTimeStamp;
	}
	public static Numeral getDeadline() {
		return deadline;
	}
	public static void setDeadline(Numeral deadline) {
		logger.info("Deadline = " +deadline);
		SimulationState.deadline = deadline;
	}
	public static TruthValue getIsTournamentOver() {
		return isTournamentOver;
	}
	public static void setIsTournamentOver(TruthValue isTournamentOver) {
		logger.info("Tournament is over.");
		SimulationState.isTournamentOver = isTournamentOver;
	}
	public static Numeral getEdgeCount() {
		return edgeCount;
	}
	public static void setEdgeCount(Numeral edgeCount) {
		logger.info("EdgeCount = " +edgeCount);
		SimulationState.edgeCount = edgeCount;
	}
	public static Numeral getVerticesCount() {
		return verticesCount;
	}
	public static void setVerticesCount(Numeral verticesCount) {
		logger.info("VerticesCount = " +verticesCount);
		SimulationState.verticesCount = verticesCount;
	}
	public static Identifier getId() {
		return id;
	}
	public static void setId(Identifier id) {
		logger.info("Id = " +id);
		SimulationState.id = id;
	}
	public static Numeral getLastStepScore() {
		return lastStepScore;
	}
	public static void setLastStepScore(Numeral lastStepScore) {
		logger.info("LastStepScore = " +lastStepScore);
		SimulationState.lastStepScore = lastStepScore;
	}
	public static Numeral getPredictedStepScore() {
		return predictedStepScore;
	}
	public static void setPredictedStepScore(Numeral predictedStepScore) {
		logger.info("PredictedStepScore = " +predictedStepScore);
		SimulationState.predictedStepScore = predictedStepScore;
	}
	public static Numeral getScore() {
		return score;
	}
	public static void setScore(Numeral score) {
		logger.info("Score = " +score);
		SimulationState.score = score;
	}
	public static Numeral getMoney() {
		return money;
	}
	public static void setMoney(Numeral money) {
		logger.info("Money = " +money);
		SimulationState.money = money;
	}
	public static Numeral getRanking() {
		return ranking;
	}
	public static void setRanking(Numeral ranking) {
		logger.info("Ranking = " +ranking);
		SimulationState.ranking = ranking;
	}
	
	@SuppressWarnings("unchecked")
	public static HashSet<Identifier> getAchievements()
	{
		return (HashSet<Identifier>) achievements.clone();
	}
	
	public static boolean addAchievement(Identifier achievement) {
		logger.info("new Achievement: " +achievement);
		return achievements.add(achievement);
	}
}
