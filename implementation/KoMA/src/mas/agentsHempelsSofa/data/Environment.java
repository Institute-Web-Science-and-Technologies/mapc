package mas.agentsHempelsSofa.data;

import java.util.LinkedList;

import eis.iilang.Action;
import eis.iilang.Parameter;

import apltk.interpreter.data.LogicBelief;

/**
 * An implementation of the environment as seen by an agent.
 * @author Hempels-Sofa
 */
public class Environment {

    /**
     * the current step value
     */
    private int step;
    /**
     * the current score value
     */
    private int score;
    /**
     * TODO S
     */
    private int zoneValue;
    /**
     * TODO S
     */
    private int zonesValue;
    /**
     * the score gained in the last step
     */
    private int lastStepScore;
    /**
     * the action generated in the last step
     */
    private Action lastAction;
    /**
     * indicates whether lastAction was successful or not
     */
    private boolean lastActionSuccessful;
    /**
     * the current money value
     */
    private int money;
    /**
     * TODO S
     */
    private String deadline;
    /**
     * the number of enemy agents inpected by the own team
     */
    private int numberOfInspectedAgents;
    /**
     * the number of enemy agents attacked by the own team
     */
    private int numberOfSuccessfulAttacks;
    /**
     * the number of successful parries by agents of the own team
     */
    private int numberOfSuccessfulParries;
    /**
     * the maximum value that has been reached with an area
     */
    private int maxAreaValue;

    /**
     * updates the environment
     * @param step
     * @param score
     * @param zoneValue
     * @param zonesValue
     * @param lastStepScore
     * @param lastAction
     * @param lastActionSuccessful
     * @param money
     * @param deadline
     */
    public void update ( int step, int score, int zoneValue, int zonesValue,
            int lastStepScore, Action lastAction, boolean lastActionSuccessful,
            int money, String deadline ) {
        this.step = step;
        this.score = score;
        this.zoneValue = zoneValue;
        this.zonesValue = zonesValue;
        this.lastStepScore = lastStepScore;
        this.lastAction = lastAction;
        this.lastActionSuccessful = lastActionSuccessful;
        this.money = money;
        this.deadline = deadline;
        if ( zonesValue > maxAreaValue )
            maxAreaValue = zonesValue;
    }

    /**
     * @param step the step to set
     */
    public void setStep ( int step ) {
        this.step = step;
    }

    /**
     * @return the step
     */
    public int getStep () {
        return step;
    }

    /**
     * @param score the score to set
     */
    public void setScore ( int score ) {
        this.score = score;
    }

    /**
     * @return the score
     */
    public int getScore () {
        return score;
    }

    /**
     * @param zoneValue the zoneValue to set
     */
    public void setZoneValue ( int zoneValue ) {
        this.zoneValue = zoneValue;
    }

    /**
     * @return the zoneValue
     */
    public int getZoneValue () {
        return zoneValue;
    }

    /**
     * @param zonesValue the zonesValue to set
     */
    public void setZonesValue ( int zonesValue ) {
        this.zonesValue = zonesValue;
    }

    /**
     * @return the zonesValue
     */
    public int getZonesValue () {
        return zonesValue;
    }

    /**
     * @param lastStepScore the lastStepScore to set
     */
    public void setLastStepScore ( int lastStepScore ) {
        this.lastStepScore = lastStepScore;
    }

    /**
     * @return the lastStepScore
     */
    public int getLastStepScore () {
        return lastStepScore;
    }

    /**
     * @param lastAction the lastAction to set
     */
    public void setLastAction ( Action lastAction ) {
        this.lastAction = lastAction;
    }

    /**
     * @return the lastAction
     */
    public Action getLastAction () {
        return lastAction;
    }

    /**
     * @param lastActionSuccessful the lastActionSuccessful to set
     */
    public void setLastActionSuccessful ( boolean lastActionSuccessful ) {
        this.lastActionSuccessful = lastActionSuccessful;
    }

    /**
     * @return the lastActionSuccessful
     */
    public boolean isLastActionSuccessful () {
        return lastActionSuccessful;
    }

    /**
     * @param money the money to set
     */
    public void setMoney ( int money ) {
        this.money = money;
    }

    /**
     * @return the money
     */
    public int getMoney () {
        return money;
    }

    /**
     * @param deadline the deadline to set
     */
    public void setDeadline ( String deadline ) {
        this.deadline = deadline;
    }

    /**
     * @return the deadline
     */
    public String getDeadline () {
        return deadline;
    }

    /**
     * transforms the environment into beliefs
     * @return a list of beliefs
     */
    public LinkedList<LogicBelief> toBeliefs () {
        LinkedList<LogicBelief> beliefs = new LinkedList<LogicBelief>();
        beliefs.add(new LogicBelief("step", Integer.toString(step)));
        beliefs.add(new LogicBelief("score", Integer.toString(score)));
        beliefs.add(new LogicBelief("zoneValue", Integer.toString(zoneValue)));
        beliefs
                .add(new LogicBelief("zonesValue", Integer.toString(zonesValue)));
        beliefs.add(new LogicBelief("lastStepScore", Integer
                .toString(lastStepScore)));
        LinkedList<String> params = new LinkedList<String>();
        params.add(lastAction.getName());
        for ( Parameter p : lastAction.getParameters() )
            params.add(p.toString());
        beliefs.add(new LogicBelief("lastAction", params));
        beliefs.add(new LogicBelief("lastActionSuccessful", Boolean
                .toString(lastActionSuccessful)));
        beliefs.add(new LogicBelief("money", Integer.toString(money)));
        beliefs.add(new LogicBelief("deadline", deadline));
        return beliefs;
    }

    /**
     * @param numberOfInspectedAgents the numberOfInspectedAgents to set
     */
    public void setNumberOfInspectedAgents ( int numberOfInspectedAgents ) {
        this.numberOfInspectedAgents = numberOfInspectedAgents;
    }

    /**
     * @return the numberOfInspectedAgents
     */
    public int getNumberOfInspectedAgents () {
        return numberOfInspectedAgents;
    }

    public void increaseNumberOfInspectedAgents ( int n ) {
        numberOfInspectedAgents += n;
    }

    /**
     * @param numberOfSuccessfulAttacks the numberOfSuccessfulAttacks to set
     */
    public void setNumberOfSuccessfulAttacks ( int numberOfSuccessfulAttacks ) {
        this.numberOfSuccessfulAttacks = numberOfSuccessfulAttacks;
    }

    /**
     * @return the numberOfSuccessfulAttacks
     */
    public int getNumberOfSuccessfulAttacks () {
        return numberOfSuccessfulAttacks;
    }

    /**
     * increases the number of successful attacks about n
     * @param n
     */
    public void increaseNumberOfSuccessfulAttacks ( int n ) {
        numberOfSuccessfulAttacks += n;
    }

    /**
     * @param numberOfSuccessfulParries the numberOfSuccessfulParries to set
     */
    public void setNumberOfSuccessfulParries ( int numberOfSuccessfulParries ) {
        this.numberOfSuccessfulParries = numberOfSuccessfulParries;
    }

    /**
     * @return the numberOfSuccessfulParries
     */
    public int getNumberOfSuccessfulParries () {
        return numberOfSuccessfulParries;
    }

    /**
     * increases the number of successful parries about n
     * @param n
     */
    public void increaseNumberOfSuccessfulParries ( int n ) {
        numberOfSuccessfulParries += n;
    }

    /**
     * @param numberOfAreaValue the numberOfAreaValue to set
     */
    public void setMaxAreaValue ( int numberOfAreaValue ) {
        this.maxAreaValue = numberOfAreaValue;
    }

    /**
     * @return the numberOfAreaValue
     */
    public int getMaxAreaValue () {
        return maxAreaValue;
    }

}
