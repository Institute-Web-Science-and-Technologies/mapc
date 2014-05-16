package mas.agentsHempelsSofa;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import apltk.interpreter.data.LogicBelief;
import apltk.interpreter.data.LogicGoal;
import apltk.interpreter.data.Message;

import mas.agentsHempelsSofa.data.*;
import mas.agentsHempelsSofa.data.graph.Edge;
import mas.agentsHempelsSofa.data.graph.Graph;
import mas.agentsHempelsSofa.data.graph.Vertex;
import mas.agentsHempelsSofa.data.zone.ZoneManager;
import mas.agentsHempelsSofa.tools.GraphWindow;
import mas.agentsHempelsSofa.util.ActionGenerator;
import mas.agentsHempelsSofa.util.StrategyInterpreter;
import eis.iilang.*;
import massim.javaagents.ParseException;
import massim.javaagents.agents.*;
import massim.javaagents.agents.MarsUtil;

/**
 * An implemenation of a generic Goal Oriented Agent.
 * @author Hempels-Sofa
 */
@SuppressWarnings("unused")
public abstract class StrategyBasedAgent extends massim.javaagents.Agent {

    /* ------------------------ Fields ---------------------------- */

    private static String logname = "";

    private static StrategyBasedAgent logger;

    private int id = 0;
    /**
     * The environment of the agent.
     */
    protected Environment environment;
    /**
     * The generic strategy of this agent.
     */
    private Strategy strategy;
    /**
     * The default generic strategy of this agent. It is parsed from the fitting
     * strategyconfig_agentX file. Everytimes step is executed, strategy is
     * reset to this default strategy. You can recall the previously used
     * strategy via {@link #getLastStrategy()}.
     */
    private Strategy defaultStrategy;
    /**
     * The strategy from the previous step.
     */
    private Strategy lastStrategy;
    /**
     * The believed Looking of the graph.
     */
    protected Graph graph;
    /**
     * The action generator.
     */
    protected ActionGenerator ag;
    /**
     * The percepts which are received in each step.
     */
    protected Collection<Percept> percepts;
    /**
     * The Zone manager.
     */
    private ZoneManager zoneManager;
    /**
     * The token which holds percepted states of this agent.
     */
    public AgentToken myToken;
    /**
     * The current tactical state of the agent. This value can be specified
     * freely by every agent.
     */
    protected int tactics;
    /**
     * The field for the messages
     */
    Collection<Message> messages;
    /**
     * The maximum weight of an edge.
     */
    private static int MAX_EDGE_WEIGHT = 10;
    /**
     * The costs of an attack action [normal, disabled].
     */
    private static int[] COSTS_ATTACK = { 2, 2 };
    /**
     * The costs of an parry action [normal, disabled].
     */
    private static int[] COSTS_PARRY = { 2, 2 };
    /**
     * The costs of an goto action [normal, disabled].
     */
    private static int[] COSTS_GOTO = { 1, 1 };
    /**
     * The costs of an probe action [normal, disabled].
     */
    private static int[] COSTS_PROBE = { 1, 1 };
    /**
     * The costs of an survey action [normal, disabled].
     */
    private static int[] COSTS_SURVEY = { 1, 1 };
    /**
     * The costs of an inspect action [normal, disabled].
     */
    private static int[] COSTS_INSPECT = { 2, 2 };
    /**
     * The costs of an repair action [normal, disabled].
     */
    private static int[] COSTS_REPAIR = { 2, 3 };
    /**
     * The costs of an buy action [normal, disabled].
     */
    private static int[] COSTS_BUY = { 2, 2 };
    /**
     * Determines for which agents a graph window shall be shown.
     */
    // private static final boolean[] SHOW_GRAPH_WINDOW =
    // {false,false,false,false,false,false,false,false,false,false};
    /**
     * The graph window of the agent.
     */
    // private GraphWindow graphWindow;
    private boolean justNewlyInitialized;
    private static boolean VERBOSE = false;
    public static boolean SHOW_GRAPH_WINDOW = true;

    /* --------------------- Constructors ------------------------- */

    /**
     * Creates a new Subsumption Agent with a specified agent-{@code name} and
     * {@code team}-name.
     * @param name The name of the agent.
     * @param team The team-name the agent belongs to.
     */
    public StrategyBasedAgent ( String name, String team ) {
        super(name, team);
        initialize();
        if ( logname.equals("") ) {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
            Date date = new Date();
            logname = getTeam() + "_" + dateFormat.format(date) + ".log";
            try {
                (new File("../../../" + logname)).createNewFile();
            }
            catch ( IOException e ) {
                e.printStackTrace();
            }
            logger = this;
            if ( SHOW_GRAPH_WINDOW )
                new GraphWindow(graph, getZoneManager(), environment);
        }
        if ( SHOW_GRAPH_WINDOW )
            GraphWindow.registerAgent(this);
    }

    private void initialize () {
        try {
            defaultStrategy = (new StrategyInterpreter()).parseConfig("agentsconfig/config_"
                    + getName() + ".xml");
        }
        catch ( ParseException e ) {
            e.printStackTrace();
        }
        environment = new Environment();
        strategy = defaultStrategy.clone();
        graph = new Graph();
        setZoneManager(new ZoneManager(graph));
        ag = new ActionGenerator(graph, getZoneManager(), this);
    }

    /* ------------------- Provided Methods ----------------------- */

    @Override
    public String toString () {
        return myToken.toString();
    }

    @Override
    public void handlePercept ( Percept p ) {
        System.out.println("I received some percepts via handlePercepts(): "
                + p);
    }

    @Override
    public synchronized Action step () {
        try {
            printHead();
            Action action = new Action("skip");
            environment.setLastAction(action);
            try {
                percepts = this.getAllPercepts();
            }
            catch ( Exception ex ) {
                System.out.println("no percepts");
                return action;
            }
            if ( percepts.size() < 10 ) {
                System.out.println("ERROR: less then 10 percepts!!!");
                return action;
            }
            int newId = Integer.parseInt(MarsUtil.filterPercepts(percepts,
                    "id").getFirst().getParameters().getFirst().toString());
            if ( newId != id && !justNewlyInitialized ) {
                logResult();
                id = newId;
                initialize();
                justNewlyInitialized = true;
                getMessages();
                System.out.println("Newly initialized. Waiting for a new simulation ...");
            }
            else {
                justNewlyInitialized = false;
            }
            if ( graph.getVertexArray() == null ) {
                graph.setVertexArray(new Vertex[Integer.parseInt(MarsUtil.filterPercepts(
                        percepts, "vertices").getFirst().getParameters().getFirst().toString())]);
                graph.SetTotalNumberOfEdges(Integer.parseInt(MarsUtil.filterPercepts(
                        percepts, "edges").getFirst().getParameters().getFirst().toString()));
            }
            if ( VERBOSE )
                System.out.println("StrategyBasedAgent[229]: handling messages");
            handleMessages();
            if ( VERBOSE )
                System.out.println("StrategyBasedAgent[232]: handling agent-specific messages");
            handleAgentSpecificMessages();
            if ( VERBOSE )
                System.out.println("StrategyBasedAgent[235]: revision of beliefs and goals");
            beliefsAndGoalsRevision();
            if ( VERBOSE )
                System.out.println("StrategyBasedAgent[238]: updating zones");
            getZoneManager().update();
            if ( VERBOSE )
                System.out.println("StrategyBasedAgent[241]: updating beliefs");
            updateBeliefs();
            if ( VERBOSE )
                System.out.println("StrategyBasedAgent[244]: revision of preferences");
            preferencesRevision();
            updateGoals();
            if ( VERBOSE )
                System.out.println("StrategyBasedAgent[248]: buy revision");
            buyRevision();
            if ( VERBOSE )
                System.out.println("StrategyBasedAgent[251]: selecting an action");
            action = actionSelection();
            broadcastBelief((myToken.toBelief()));
            if ( VERBOSE )
                System.out.println("StrategyBasedAgent[255]: checking the action");
            action = actionRevision(action);
            broadcastBelief(myToken.toBelief());
            outputEnding(action);
            if ( this == logger && SHOW_GRAPH_WINDOW )
                GraphWindow.graphWindow.repaintGraph();
            return action;
        }
        catch ( Exception e ) {
            e.printStackTrace();
            return new Action("recharge");
        }
    }

    /* ---------------------- Revisions --------------------------- */

    /**
     * updates the beliefs and goals, especially the graph.
     */
    private void beliefsAndGoalsRevision () {
        // list of updates to broadcast
        //System.out.println(percepts);
        
        LinkedList<LogicBelief> updates = new LinkedList<LogicBelief>();

        // ====== FETCH PHASE ======

        // only explore and survey, if there are edges left
        LinkedList<Percept> visibleEdges;
        LinkedList<Percept> surveyedEdges;
        if ( graph.getRatioOfExploredEdges() != 1 )
            visibleEdges = MarsUtil.filterPercepts(getPercepts(),
                    "visibleEdge");
        else
            visibleEdges = new LinkedList<Percept>();
        if ( graph.getRatioOfSurveyedEdges() != 1 )
            surveyedEdges = MarsUtil.filterPercepts(getPercepts(),
                    "surveyedEdge");
        else
            surveyedEdges = new LinkedList<Percept>();

        LinkedList<Percept> probedVertices = MarsUtil.filterPercepts(
                getPercepts(), "probedVertex");
        LinkedList<Percept> visibleEntities = MarsUtil.filterPercepts(
                getPercepts(), "visibleEntity");
        LinkedList<Percept> inspectedEntities = MarsUtil.filterPercepts(
                getPercepts(), "inspectedEntity");
        String myName = "", myTeam = "";
        for ( Percept p : visibleEntities ) {
            String name, team;
            name = p.getParameters().get(0).toString();
            team = p.getParameters().get(2).toString();
            if ( name.replaceAll("[^0-9]", "").equals(
                    getName().replaceAll("[^0-9]", ""))
                    && getTeam().equals(team) ) {
                myName = name;
                myTeam = team;
            }
        }
        getZoneManager().setOwnTeam(myTeam);
        // environment specific
        LinkedList<Percept> steps = MarsUtil.filterPercepts(getPercepts(),
                "step");
        int step = 0;
        if ( !steps.isEmpty() )
            step = Integer.parseInt(steps.getFirst().getParameters().getFirst().toString());
        int score = Integer.parseInt(MarsUtil.filterPercepts(getPercepts(),
                "score").getFirst().getParameters().getFirst().toString());
        int zoneValue = Integer.parseInt(MarsUtil.filterPercepts(percepts,
                "zoneScore").getFirst().getParameters().getFirst().toString());
        int zonesValue = Integer.parseInt(MarsUtil.filterPercepts(percepts,
                "zonesScore").getFirst().getParameters().getFirst().toString());
        int lastStepScore = Integer.parseInt(MarsUtil.filterPercepts(
                getPercepts(), "lastStepScore").getFirst().getParameters().getFirst().toString());
        boolean lastActionSuccessful = MarsUtil.filterPercepts(
                getPercepts(), "lastActionResult").getFirst().getParameters().getFirst().toString().equals(
                "successful");
        Action lastAction;
        Percept lastActionPercept = MarsUtil.filterPercepts(getPercepts(),
                "lastAction").getFirst();
        if ( lastActionPercept.getParameters().size() == 1 )
            lastAction = new Action(
                    lastActionPercept.getParameters().getFirst().toString());
        else
            lastAction = new Action(
                    lastActionPercept.getParameters().getFirst().toString(),
                    lastActionPercept.getParameters().getLast());
        int money = Integer.parseInt(MarsUtil.filterPercepts(getPercepts(),
                "money").getFirst().getParameters().getFirst().toString());
        String deadline = MarsUtil.filterPercepts(getPercepts(), "deadline").getFirst().getParameters().getFirst().toString();
        // agent specific
        Vertex position = new Vertex(MarsUtil.filterPercepts(getPercepts(),
                "position").getFirst().getParameters().getFirst().toString());
        String role = "unknown";
        if ( !MarsUtil.filterPercepts(getPercepts(), "role").isEmpty() )
            role = MarsUtil.filterPercepts(getPercepts(), "role").getFirst().getParameters().getFirst().toString();
        int energy = Integer.parseInt(MarsUtil.filterPercepts(
                getPercepts(), "energy").getFirst().getParameters().getFirst().toString());
        int maxEnergy = Integer.parseInt(MarsUtil.filterPercepts(
                getPercepts(), "maxEnergy").getFirst().getParameters().getFirst().toString());
        int maxEnergyDisabled = Integer.parseInt(MarsUtil.filterPercepts(
                getPercepts(), "maxEnergyDisabled").getFirst().getParameters().getFirst().toString());
        int health = Integer.parseInt(MarsUtil.filterPercepts(
                getPercepts(), "health").getFirst().getParameters().getFirst().toString());
        int maxHealth = Integer.parseInt(MarsUtil.filterPercepts(
                getPercepts(), "maxHealth").getFirst().getParameters().getFirst().toString());
        int strength = Integer.parseInt(MarsUtil.filterPercepts(
                getPercepts(), "strength").getFirst().getParameters().getFirst().toString());
        int visibilityRange = Integer.parseInt(MarsUtil.filterPercepts(
                getPercepts(), "visRange").getFirst().getParameters().getFirst().toString());

        // ====== UPDATE PHASE ======

        environmentRevision(step, score, zoneValue, zonesValue, lastStepScore,
                lastAction, lastActionSuccessful, money, deadline, updates);
        graphRevision(visibleEdges, surveyedEdges, probedVertices, updates);
        ownTokenRevision(myName, myTeam, position, role, energy, maxEnergy,
                maxEnergyDisabled, health, maxHealth, strength,
                visibilityRange, step, updates);
        tokenRevision(visibleEntities, inspectedEntities, updates);

        // ===== COMMIT PHASE =====

        // only send, if there are updates
        if ( updates.isEmpty() )
            return;
        for ( LogicBelief update : updates )
            this.broadcastBelief(update);
    }

    /**
     * updates the {@link #environment}.
     * @param step the current step count
     * @param score the current score
     * @param zoneValue the value of the zone the agent is in
     * @param zonesValue the value of all zones
     * @param lastStepScore the score of the last step
     * @param lastAction the last executed action
     * @param lastActionSuccessful <ul>
     *        <li>{@code true}, if the last action was successful,</li>
     *        <li>{@code false}, otherwise,</li>
     *        </ul>
     * @param money the current money of the team
     * @param deadline the deadline ???
     * @param updates the list of updates to broadcast.
     */
    private void environmentRevision ( int step, int score, int zoneValue,
            int zonesValue, int lastStepScore, Action lastAction,
            boolean lastActionSuccessful, int money, String deadline,
            LinkedList<LogicBelief> updates ) {
        environment.update(step, score, zoneValue, zonesValue, lastStepScore,
                lastAction, lastActionSuccessful, money, deadline);
        if ( lastActionSuccessful ) {
            if ( lastAction.getName().equals("attack") ) {
                environment.increaseNumberOfSuccessfulAttacks(1);
                broadcastBelief(new LogicBelief("successfulAttack"));
            }
            if ( lastAction.getName().equals("parry") ) {
                environment.increaseNumberOfSuccessfulParries(1);
                broadcastBelief(new LogicBelief("successfulParry"));
            }
        }
    }

    /**
     * updates the own token.
     * @param position the position of the agent
     * @param role the role of the agent
     * @param energy the current energy of the agent
     * @param maxEnergy the maximum energy of the agent
     * @param maxEnergyDisabled the maximum energy of the agent if it is
     *        disabled
     * @param health the current health of the agent
     * @param maxHealth the maximum health of the agent
     * @param strength the strength of the agent
     * @param visibilityRange the visibility range of the agent
     * @param step the number of the actually executed step
     * @param updates
     */
    private void ownTokenRevision ( String myName, String myTeam,
            Vertex position, String role, int energy, int maxEnergy,
            int maxEnergyDisabled, int health, int maxHealth, int strength,
            int visibilityRange, int step, LinkedList<LogicBelief> updates ) {
        graph.setPosition(graph.getVertex(position));
        myToken = new AgentToken(myName, myTeam, graph.getPosition(), null,
                null, role, energy, maxEnergy, maxEnergyDisabled, health,
                maxHealth, strength, visibilityRange, step);
        graph.updateToken(myToken);
        myToken = graph.getToken(myToken);
    }

    /**
     * updates the beliefs about edges and vertices in the graph.
     * @param visibleEdges the visible edges
     * @param surveyedEdges the surveyed edges
     * @param probedVertices the probed vertices
     * @param updates the beliefs to share with team agents.
     */
    private void graphRevision ( LinkedList<Percept> visibleEdges,
            LinkedList<Percept> surveyedEdges,
            LinkedList<Percept> probedVertices, LinkedList<LogicBelief> updates ) {

        // add edge weights to graph
        if ( environment.getLastAction().getName().equals("survey") ) {
            if ( graph.getPosition().getNumberOfUnsurveyedEdges() == 0 ) {
                // (new
                // RuntimeException("Redundant survey on vertex "+graph.getPosition().toBelief())).printStackTrace();
            }
            else {
                for ( Percept p : surveyedEdges ) {
                    Edge edge = graph.add(p.getParameters().get(0),
                            p.getParameters().get(1));
                    if ( !edge.isSurveyed() ) {
                        graph.setSurveyed(
                                edge,
                                Integer.parseInt(p.getParameters().getLast().toString()));
                        updates.add(edge.toBelief());
                    }
                }
                graph.getPosition().surveyAllIncidentEdges();
            }
        }
        // add visible edges to graph
        for ( Percept p : visibleEdges ) {
            Edge edge = new Edge(p.getParameters().getFirst(),
                    p.getParameters().getLast());
            if ( !graph.contains(edge) ) {
                graph.add(edge);
                updates.add(edge.toBelief());
            }
        }
        // update probed vertices
        if ( environment.getLastAction().getName().equals("probe") ) {
            for ( Percept p : probedVertices ) {
                Vertex vertex = graph.getVertex(p.getParameters().getFirst());
                if ( !vertex.isProbed() ) {
                    graph.setProbed(
                            vertex,
                            Integer.parseInt(p.getParameters().getLast().toString()));
                    updates.add(vertex.toBelief());
                }
                // else
                // (new
                // RuntimeException("Redundant probe of vertex "+vertex.toBelief())).printStackTrace();
            }
        }
    }

    /**
     * updates the beliefs about the entities.
     * @param visibleEntities the visible entities.
     * @param inspectedEntities
     * @param updates the beliefs to share with team agents.
     */
    private void tokenRevision ( LinkedList<Percept> visibleEntities,
            LinkedList<Percept> inspectedEntities,
            LinkedList<LogicBelief> updates ) {
        for ( Percept p : visibleEntities ) {
            LinkedList<Parameter> params = p.getParameters();
            Vertex position = new Vertex(params.get(1));
            AgentToken token = new AgentToken(params.get(0).toString(),
                    params.get(2).toString(), position,
                    params.get(3).toString(), environment.getStep());
            if ( !params.get(2).toString().equals(myToken.getTeam()) )
                token.setStrategyType(AgentToken.STRATEGY_UNKNOWN);
            if ( token.equals(myToken) ) {
                myToken.setState(token.getState());
                if ( position.equals(myToken.getTargetVertex()) )
                    myToken.setTargetVertex(null);
            }
            else{
                graph.updateToken(token);
                updates.add(token.toBelief());
            }
        }
        for ( Percept p : inspectedEntities ) {
            LinkedList<Parameter> params = p.getParameters();
            Vertex position = new Vertex(params.get(3));
            AgentToken token = new AgentToken(params.get(0).toString(),
                    params.get(1).toString(), position, null, null, params.get(
                            2).toString(),
                    Integer.parseInt(params.get(4).toString()),
                    Integer.parseInt(params.get(5).toString()), -1,
                    Integer.parseInt(params.get(6).toString()),
                    Integer.parseInt(params.get(7).toString()),
                    Integer.parseInt(params.get(8).toString()),
                    Integer.parseInt(params.get(9).toString()),
                    environment.getStep());
            if ( !token.getTeam().equals(myToken.getTeam())
                    && graph.getToken(token).getLastInspection() < 0 )
                environment.increaseNumberOfInspectedAgents(1);
            graph.updateToken(token);
            if ( !token.equals(myToken) )
                updates.add(token.toBelief());
        }
    }

    /**
     * ensures that the upper limits of the buy actions are not exceeded
     */
    private void buyRevision () {

        if ( myToken.isDisabled() ) {
            getStrategy().buyPref = 0;
            return;
        }

        if ( environment.getMoney() < 10 ) {
            getStrategy().buyPref = 0;
            return;
        }

        boolean maxEnergyReached = myToken.getMaxEnergy() >= strategy.maxEnergyLimit;
        boolean maxHealthReached = myToken.getMaxHealth() >= strategy.maxHealthLimit;
        boolean visRangeReached = myToken.getVisibilityRange() >= strategy.visRangeLimit;
        boolean maxStrengthReached = myToken.getStrength() >= strategy.strengthLimit;

        if ( maxEnergyReached )
            getStrategy().buyBatteryPref = 0;
        if ( maxHealthReached )
            getStrategy().buyShieldPref = 0;
        if ( visRangeReached )
            getStrategy().buySensorPref = 0;
        if ( maxStrengthReached )
            getStrategy().buySabotageDevicePref = 0;

        if ( maxEnergyReached && maxHealthReached && visRangeReached
                && maxStrengthReached )
            getStrategy().buyPref = 0;
    }

    /**
     * updates the belief base of this agent.
     */
    private void updateBeliefs () {
        LinkedList<LogicBelief> newBeliefs = new LinkedList<LogicBelief>();
        newBeliefs.addAll(graph.toBeliefs());
        newBeliefs.addAll(getZoneManager().toBeliefs());
        newBeliefs.addAll(environment.toBeliefs());
        newBeliefs.add(new LogicBelief("probedVertices",
                Integer.toString(graph.getNumberOfProbedVertices())));
        newBeliefs.add(new LogicBelief("surveyedEdges",
                Integer.toString(graph.getNumberOfSurveyedEdges())));
        newBeliefs.add(new LogicBelief("inspectedAgents",
                Integer.toString(environment.getNumberOfInspectedAgents())));
        newBeliefs.add(new LogicBelief("successfulAttacks",
                Integer.toString(environment.getNumberOfSuccessfulAttacks())));
        newBeliefs.add(new LogicBelief("successfulParries",
                Integer.toString(environment.getNumberOfSuccessfulParries())));
        newBeliefs.add(new LogicBelief("areaValue",
                Integer.toString(environment.getMaxAreaValue())));
        this.clearBeliefs();
        for ( LogicBelief belief : newBeliefs )
            this.addBelief(belief);
    }

    /**
     * updates the goals of this agent.
     */
    private void updateGoals () {
        this.clearGoals();
        this.addGoal(new LogicGoal("areaValue",
                Integer.toString((int) strategy.achievementsAreaValueGoal)));
        this.addGoal(new LogicGoal(
                "inspectedAgents",
                Integer.toString((int) strategy.achievementsInspectedAgentsGoal)));
        this.addGoal(new LogicGoal("probedVertices",
                Integer.toString((int) strategy.achievementsProbedVerticesGoal)));
        this.addGoal(new LogicGoal(
                "successfulAttacks",
                Integer.toString((int) strategy.achievementsSuccessfulAttacksGoal)));
        this.addGoal(new LogicGoal(
                "successfulParries",
                Integer.toString((int) strategy.achievementsSuccessfulParriesGoal)));
        this.addGoal(new LogicGoal("surveyedEdges",
                Integer.toString((int) strategy.achievementsSurveyedEdgesGoal)));
    }

    /**
     * computes the preferences for the strategies and focuses.
     */
    public abstract void preferencesRevision ();

    /**
     * checks the action and does some changes if there is not enough energy or
     * the action is internal.
     * @param action the action to check
     * @return The resulting action.
     */
    private Action actionRevision ( Action action ) {
        action = checkEnergy(action);
        // TODO: -> AgentTokens
        if ( action.getName().equals("goto") ) {
            Parameter target = action.getParameters().getFirst();
            LinkedList<String> update = new LinkedList<String>();
            update.add(getGraph().getPosition().getName());
            update.add(target.toString());
            update.add(getGraph().getVertex(target).getName());
            this.broadcastBelief(new LogicBelief("newTarget", update));
            this.addGoal(new LogicGoal("target", update));
        }
        return action;
    }

    /* ----------------------- Messages --------------------------- */

    /**
     * Takes care of the messages each agent has to deal with Handles messages
     * containing a new vertex,a new edge, a new spotted agent or an updated
     * zone-value: A message can be created using broadcast(new
     * LogicBelief(predicate, parameter0, parameter1, ...)
     * <ul>
     * <li>Message for a new Vertex: Has the predicate "vertex" and the
     * Parameters are all the Strings returned by Vertex.toBelief()</li>
     * <li>Message for a new Edge: Has the predicate "edge" and the Parameters
     * are all the Strings returned by Edge.toBelief()</li>
     * <li>Message for a new Agent: Has the predicate "agent" and the Parameters
     * are all the Strings returned by AgentToken.toBelief()</li>
     * <li>Message for a new ZoneValue: Has the predicate "zoneValue" and the
     * Parameters ???</li>
     * </ul>
     */
    private void handleMessages () {
        // C werden irgendwo die messages mit newTarget bearbeitet?? sonst
        // funktioniert einiges nicht von julius
        // Get all messages from Server
        messages = getMessages();
        // If mailbox empty, be sad
        if ( messages.size() != 0 ) {
            // Check every message in mailbox
            for ( Message msg : messages ) {
                LogicBelief belief = (LogicBelief) msg.value;
                String predicate = belief.getPredicate();
                // Check, if the message has something to do with a new edge
                if ( predicate.equals("edge") ) {
                    Edge e = graph.add(new Edge(belief.getParameters().get(0),
                            belief.getParameters().get(1)));
                    int weight = Integer.parseInt(belief.getParameters().get(2));
                    if ( !e.isSurveyed() && weight != Edge.UNSURVEYED_WEIGHT )
                        graph.setSurveyed(e, weight);
                }
                // Check, if the message has something to do with a new vertex
                if ( predicate.equals("vertex") ) {
                    int value = Integer.parseInt(belief.getParameters().get(1));
                    Vertex v = graph.add(new Vertex(belief.getParameters().get(
                            0)));
                    if ( !v.isProbed() && value != Vertex.UNPROBED_VALUE )
                        graph.setProbed(v, value);
                }
                // If the message has something to do with a new target of some
                // agent, move the tokens on the graph accordingly
                if ( predicate.equals("agent") ) {
                    Vertex agentposition = new Vertex(
                            belief.getParameters().get(2));
                    if ( !graph.contains(agentposition) )
                        graph.add(agentposition);
                    Vertex targetVertex = null;
                    try {
                        targetVertex = graph.getVertex(belief.getParameters().get(
                                3));
                    }
                    catch ( Exception ex ) {
                    }
                    AgentToken token = new AgentToken(
                            belief.getParameters().get(0),
                            belief.getParameters().get(1),
                            graph.getVertex(belief.getParameters().get(2)),
                            targetVertex, belief.getParameters().get(4),
                            belief.getParameters().get(5),
                            Integer.parseInt(belief.getParameters().get(7)),
                            Integer.parseInt(belief.getParameters().get(8)),
                            Integer.parseInt(belief.getParameters().get(9)),
                            Integer.parseInt(belief.getParameters().get(10)),
                            Integer.parseInt(belief.getParameters().get(11)),
                            Integer.parseInt(belief.getParameters().get(12)),
                            Integer.parseInt(belief.getParameters().get(13)),
                            Integer.parseInt(belief.getParameters().get(14)));
                    token.setStrategyType(Integer.parseInt(belief.getParameters().get(
                            6)));
                    AgentToken graphToken = graph.getToken(token);
                    if ( graphToken != null && myToken != null
                            && !token.getTeam().equals(myToken.getTeam())
                            && graphToken.getLastInspection() < 0
                            && token.getHealth() > 0 )
                        environment.increaseNumberOfInspectedAgents(1);
                    graph.updateToken(token);
                }
                if ( predicate.equals("successfulAttack") )
                    environment.increaseNumberOfSuccessfulAttacks(1);
                if ( predicate.equals("successfulParry") )
                    environment.increaseNumberOfSuccessfulParries(1);
            }
        }

    }

    /**
     * handles some messages which are agent-role specific.
     */
    public abstract void handleAgentSpecificMessages ();

    /**
     * @param belief the belief to send to all other agents.
     */
    public void broadcast ( LogicBelief belief ) {
        super.broadcastBelief(belief);
    }

    /* ----------------------- Actions ---------------------------- */

    /**
     * Selects an action according to our strategies.
     * @return An external rule.
     */
    private Action actionSelection () {
        int chosen = strategy.chooseFocus();
        lastStrategy = strategy;
        strategy = defaultStrategy.clone();
        String focus = Strategy.allFocuses[chosen];
        Action generatedAction;
        if ( chosen < 3 )
            generatedAction = generateOffensiveAction(focus);
        else if ( chosen < 6 )
            generatedAction = generateDefensiveAction(focus);
        else if ( chosen < 10 )
            generatedAction = generateZoneAction(focus);
        else if ( chosen < 14 )
            generatedAction = generateBuyAction(focus);
        else
            generatedAction = generateAchievementAction(focus);

        if ( generatedAction == null
                || generatedAction.getName().equals("skip") )
            new RuntimeException("skip action returned, in focus " + focus).printStackTrace();

        return generatedAction;
    }

    /**
     * computes an offensive action with a certain focus.
     * @param focus <ul>
     *        <li>offensiveDestroyZones,</li>
     *        <li>offensiveDestroyAgents,</li>
     *        <li>offensiveDrawback</li>
     *        </ul>
     * @return a specific action.
     */
    public abstract Action generateOffensiveAction ( String focus );

    /**
     * computes an defensive action with a certain focus.
     * @param focus <ul>
     *        <li>defensiveParry,</li>
     *        <li>defensiveRunAway,</li>
     *        <li>defensiveRepair,</li>
     *        </ul>
     * @return a specific action.
     */
    public abstract Action generateDefensiveAction ( String focus );

    /**
     * computes a zone action with a certain focus
     * @param focus <ul>
     *        <li>zoneExpand,</li>
     *        <li>zoneStability,</li>
     *        <li>zoneMainZone,</li>
     *        <li>zoneDrawback</li>
     *        </ul>
     * @return a specific action.
     */
    public abstract Action generateZoneAction ( String focus );

    /**
     * computes a buy action with a certain focus
     * @param focus <ul>
     *        <li>buyBattery,</li>
     *        <li>buySabotageDevice,</li>
     *        <li>buySensor,</li>
     *        <li>buyShield</li>
     *        </ul>
     * @return a specific action.
     */
    public abstract Action generateBuyAction ( String focus );

    /**
     * computes a achievement supportive action with a certain focus
     * @param focus <ul>
     *        <li>achievementsProbedVertices,</li>
     *        <li>achievementsSurveyedEdges,</li>
     *        <li>achievementsInspectedAgents,</li>
     *        <li>achievementsSuccessfulAttacks,</li>
     *        <li>achievementsSuccessfulParries,</li>
     *        <li>achievementsAreaValue</li>
     *        </ul>
     * @return a specific action.
     */
    public abstract Action generateAchievementAction ( String focus );

    /**
     * checks whether the energy of the agent is sufficient to execute the
     * {@code action}.
     * @param action the action which shall be executed.
     * @return the same action
     */
    protected Action checkEnergy ( Action action ) {
        String name = action.getName();
        LinkedList<Parameter> parameters = action.getParameters();
        LinkedList<String> params = new LinkedList<String>();
        for ( Parameter par : parameters )
            params.add(par.toProlog());
        int energy = myToken.getEnergy();
        int stateIndex = 0;
        if ( myToken.isDisabled() )
            stateIndex = 1;
        if ( name == "attack" && energy < COSTS_ATTACK[stateIndex] )
            return new Action("recharge");
        else if ( name == "parry" && energy < COSTS_PARRY[stateIndex] )
            return new Action("recharge");
        else if ( name == "goto" ) {
            Edge e = graph.getEdge(
                    graph.getPosition().getIdentifier(),
                    new Identifier(action.getParameters().getFirst().toString()));
            if ( e == null )
                return new Action("skip");
            if ( !e.isSurveyed() && myToken.getMaxEnergy() < MAX_EDGE_WEIGHT )
                return new Action("buy", new Identifier("battery"));
            if ( myToken.isDisabled() ) {
                if ( myToken.getMaxEnergyDisabled() < 8 ) {
                    if ( !e.isSurveyed()
                            && energy < COSTS_GOTO[stateIndex]
                                    * myToken.getMaxEnergyDisabled() ) {
                        return new Action("recharge");
                    }
                }
                else {
                    if ( !e.isSurveyed()
                            && energy < COSTS_GOTO[stateIndex]
                                    * MAX_EDGE_WEIGHT )
                        return new Action("recharge");
                }
            }
            else {
                if ( !e.isSurveyed()
                        && energy < COSTS_GOTO[stateIndex] * MAX_EDGE_WEIGHT )
                    return new Action("recharge");
            }
            if ( e.isSurveyed()
                    && energy < COSTS_GOTO[stateIndex] * e.getWeight() )
                return new Action("recharge");
        }
        else if ( name == "probe" && energy < COSTS_PROBE[stateIndex] )
            return new Action("recharge");
        else if ( name == "survey" && energy < COSTS_SURVEY[stateIndex] )
            return new Action("recharge");
        else if ( name == "inspect" && energy < COSTS_INSPECT[stateIndex] )
            return new Action("recharge");
        else if ( name == "repair" && energy < COSTS_REPAIR[stateIndex] )
            return new Action("recharge");
        else if ( name == "buy" && energy < COSTS_BUY[stateIndex] )
            return new Action("recharge");
        return action;
    }

    /**
     * @return True, if there is an enemy agent or an unknown agent on my
     *         postion
     */
    protected boolean enemyNearby () {
        if ( enemySaboteurAt(getGraph().getPosition()) )
            return true;
        if ( enemyUnknownAgentAt(getGraph().getPosition()) )
            return true;
        return false;
    }

    /**
     * @param v the vertex to test
     * @return true, if there is an enemy saboteur, else false
     */
    public boolean enemySaboteurAt ( Vertex v ) {
        for ( AgentToken t : v.getTokens() ) {
            if ( t.getRole() == null && t.getTeam() != myToken.getTeam() )
                return false;
            if ( !t.getTeam().equals(myToken.getTeam())
                    && t.getRole().equals("Saboteur") )
                return true;
        }
        return false;
    }

    /**
     * @param v vertex to test
     * @return true if there is an enenmy unknown agent, else false
     */
    public boolean enemyUnknownAgentAt ( Vertex v ) {
        for ( AgentToken t : v.getTokens() ) {
            if ( t.getRole() == null && t.getTeam() != myToken.getTeam() )
                return true;
        }
        return false;
    }

    /* ------------------ logging and outputs --------------------- */

    /**
     * prints the agent name and role.
     */
    private void printHead () {
        String role = "unknown";
        String name = getName();
        if ( myToken != null ) {
            role = " (" + myToken.getRole() + ")";
            name = myToken.getName();
        }
        System.out.println("\n ***** ------- > " + name + role
                + " < ------ ***** \n ");
    }

    /**
     * prints important things to the console in the beginning of
     * {@link #step()}.
     */
    private void outputBeginning () {
        System.out.println("Number of messages = " + getMessages().size());
    }

    /**
     * prints important things to the console in the end of {@link #step()}.
     */
    private void outputEnding ( Action action ) {
        System.out.println("last action:      " + environment.getLastAction());
        System.out.println("success:          "
                + environment.isLastActionSuccessful());
        System.out.println("generated action: " + action);
        System.out.println("Strategy-Type:    "
                + myToken.getStrategyTypeString());
    }

    private void logResult () {
        if ( this == logger )
            log("----> Simulation: "
                    + (id / 12 + 1)
                    + " <----\n\n"
                    + "Team:               "
                    + myToken.getTeam()
                    + "\n"
                    + "won:                "
                    + MarsUtil.filterPercepts(percepts, "ranking").getFirst().getParameters().getFirst().toString().equals(
                            "1") + "\n" + "Number of Steps:    "
                    + (environment.getStep() + 1) + "\n"
                    + "Explored Vertices:  " + graph.getNumberOfVertices()
                    + " / " + graph.getTotalNumberOfVertices() + "\n"
                    + "Explored Edges:     " + graph.getNumberOfEdges() + " / "
                    + graph.getTotalNumberOfEdges() + "\n"
                    + "Probed Vertices:    "
                    + graph.getNumberOfProbedVertices() + "\n"
                    + "Surveyed Edges:     " + graph.getNumberOfSurveyedEdges()
                    + "\n" + "Inspected Agents:   "
                    + environment.getNumberOfInspectedAgents() + "\n"
                    + "Successful Attacks: "
                    + environment.getNumberOfSuccessfulAttacks() + "\n"
                    + "Successful Parries: "
                    + environment.getNumberOfSuccessfulParries() + "\n"
                    + "Score:              " + environment.getScore() + "\n\n");
    }

    /**
     * appends some string to the agents log.
     * @param s the string to append
     */
    protected void log ( String s ) {
        BufferedWriter output;
        try {
            output = new BufferedWriter(new FileWriter(new File("../../../"
                    + logname), true));
            output.append(s);
            output.close();
        }
        catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    /**
     * clears the agent's log.
     */
    protected void clearlog () {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File(
                    "../../../" + logname), false));
            bw.write("");
            bw.close();
        }
        catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    @Override
    protected void finalize () {
        try {
            logResult();
        }
        catch ( Exception ex ) {
        }
    }

    /* ------------------- Getter, Setter ------------------------ */

    /**
     * b
     * @param graph the graph to set
     */
    protected void setGraph ( Graph graph ) {
        this.graph = graph;
    }

    /**
     * @return the graph
     */
    public Graph getGraph () {
        return graph;
    }

    /**
     * @param environment the environment to set
     */
    public void setEnvironment ( Environment environment ) {
        this.environment = environment;
    }

    /**
     * @return the environment
     */
    public Environment getEnvironment () {
        return environment;
    }

    /**
     * @param percepts the percepts to set
     */
    public void setPercepts ( Collection<Percept> percepts ) {
        this.percepts = percepts;
    }

    /**
     * @return the percepts
     */
    public Collection<Percept> getPercepts () {
        return percepts;
    }

    /**
     * @param myToken the myToken to set
     */
    protected void setMyToken ( AgentToken myToken ) {
        this.myToken = myToken;
    }

    /**
     * @return the myToken
     */
    protected AgentToken getMyToken () {
        return myToken;
    }

    /**
     * @param strategy the strategy to set
     */
    public void setStrategy ( Strategy strategy ) {
        this.strategy = strategy;
    }

    /**
     * @return the current strategy which will be used for the determination of
     *         one of the next action-generated methods:
     *         <ul>
     *         <li>{@link #generateAchievementAction(String)}</li>
     *         <li>{@link #generateBuyAction(String)}</li>
     *         <li>{@link #generateZoneAction(String)}</li>
     *         <li>{@link #generateDefensiveAction(String)}</li>
     *         <li>{@link #generateOffensiveAction(String)}</li>
     *         </ul>
     *         Rules for the strategy and focuses preferences p:
     *         <ol>
     *         <li>For all strategies 0 <= p < 1: a strategy is randomly chosen.
     *         <li>A strategy has p=1: the first occurring strategy with p = 1
     *         is chosen.</li>
     *         <li>Multiple strategies have 1 < p < 2: only these strategies can
     *         be chosen with p' = p - 1.</li>
     *         <li>A strategy is chosen, for all ps of focuses p < 1: a focus is
     *         randomly chosen.</li>
     *         <li>A strategy is chosen, a focus has p = 1: the first occuring
     *         focus with p = 1 is chosen.</li>
     *         <li>A strategy is chosen, multiple focuses have 1 < p < 2: only
     *         these focuses are considered with p' = p - 1</li>
     *         </ol>
     */
    public Strategy getStrategy () {
        return strategy;
    }

    /**
     * @return the generic strategy from the last executed step.
     */
    public Strategy getLastStrategy () {
        return lastStrategy;
    }

    /**
     * @return the default generic strategy of this agent. It is parsed from the
     *         fitting strategyconfig_agentX file. Everytimes step is executed,
     *         strategy is reset to this default strategy. You can recall the
     *         previously used strategy via {@link #getLastStrategy()}.
     */
    public Strategy getDefaultStrategy () {
        return defaultStrategy;
    }

    /**
     * reloads the last strategy.
     */
    public void reloadStrategy () {
        strategy = lastStrategy.clone();
    }

    /**
     * Loads a strategy without cloning.
     * @param strategy the strategy to load
     */
    public void loadStrategy ( Strategy strategy ) {
        this.strategy = strategy;
    }

    /**
     * Loads a strategy
     * @param strategy the strategy to load
     * @param clone <ul>
     *        <li>{@code true}, if the strategy shall be cloned</li>
     *        <li>{@code false}, otherwise (more performant)</li>
     *        </ul>
     */
    public void loadStrategy ( Strategy strategy, boolean clone ) {
        if ( clone )
            this.strategy = strategy.clone();
        else
            this.strategy = strategy;
    }

    /**
     * @return True, if there is an unsurveyed edge around the vertex I am on
     */
    protected boolean unsurveyedEdgesNearby () {
        for ( Edge e : getGraph().getPosition().getIncidentEdges() )
            if ( !e.isSurveyed() )
                return true;
        return false;
    }

    /**
     * @param zoneManager the zoneManager to set
     */
    public void setZoneManager ( ZoneManager zoneManager ) {
        this.zoneManager = zoneManager;
    }

    /**
     * @return the zoneManager
     */
    public ZoneManager getZoneManager () {
        return zoneManager;
    }

}