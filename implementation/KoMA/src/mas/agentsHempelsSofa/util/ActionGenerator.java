package mas.agentsHempelsSofa.util;

import java.util.LinkedList;
import java.util.Random;

import apltk.interpreter.data.LogicBelief;

import mas.agentsHempelsSofa.StrategyBasedAgent;
import mas.agentsHempelsSofa.algorithms.GraphAlgorithms;
import mas.agentsHempelsSofa.data.AgentToken;

import mas.agentsHempelsSofa.data.graph.Graph;
import mas.agentsHempelsSofa.data.graph.Vertex;
import mas.agentsHempelsSofa.data.zone.Zone;
import mas.agentsHempelsSofa.data.zone.ZoneManager;
import eis.iilang.Action;

/**
 * An action generator which provides some general actions for all
 * strategy-based agents.
 * @author Hempels-Sofa
 */
public class ActionGenerator {

    private Graph graph;
    private ZoneManager zoneManager;
    private StrategyBasedAgent agent;

    public ActionGenerator ( Graph graph, ZoneManager zoneManager,
            StrategyBasedAgent agent ) {
        this.setGraph(graph);
        this.zoneManager = zoneManager;
        this.agent = agent;
    }

    /* --------------------- Enemy zone destroying ---------------- */

    /**
     * generates an action which contributes to destroying the enemies most
     * precious zone
     * @return action
     */
    public Action destroyZone () {
        return destroyZone(zoneManager.getMostPreciousEnemyZone());
    }

    /**
     * generates an action which contributes to destroying a target zone
     * @param targetZone the zone to destroy
     * @return action
     */
    public Action destroyZone ( Zone targetZone ) {
        // i do not know the target
        if ( targetZone == null )
            return expandOwnComponent();
        // i am not in targetZone
        if ( !targetZone.contains(agent.myToken) )
            return moveTowardsNearest(targetZone.getCriticalFrontier());
        // i am in targetZone
        else {
            for ( Vertex v : agent.myToken.getPosition().getAdjacentVertices() )
                if ( targetZone.contains(v) )
                    return new Action("goto", v.getIdentifier());
            return expandOwnComponent();
        }

    }

    /* --------------------- Zone building------------------------ */

    /*
     * The methods in here can be used by every type of agent buildZone() makes
     * the agent unite his connected component to the main Zone's component by
     * expanding his by randomwalking into new territories and earning money by
     * surveying once this is achieved he tries to become part of the main Zones
     * frontier being part of the frontier he holds it and tries to expand it by
     * a certain probability which takes into account: - his position - enemies
     * - the surrounding
     */

    /**
     * generates an action which contributes to building a zone
     */
    public Action buildZone () {

        // determine the biggest zone of the team
        Zone mainZone = zoneManager.getMostPreciousZone();
        if ( mainZone == null ) {
            return expandOwnComponent();
        }
        // determine whether the agent is in the connected component of the main
        // Zone
        if ( graph.getConnectedComponent(mainZone.getVertices().getFirst())
                .contains(graph.getPosition()) ) {
            // check whether the agent is part of the Zone
            if ( mainZone.contains(graph.getPosition()) ) {
                // check whether the agent is part of the frontier
                if ( mainZone.getCriticalFrontier().contains(
                        graph.getPosition()) ) {
                    return determineExpansion(mainZone);
                }
                // agent is inside of the zone
                // System.out.println("ich bin innen in der Zone");
                return expandZoneByValue(mainZone);
            }
            // agent is not part of the Zone
            return moveTowardsNearest(mainZone.getCriticalFrontier());
        }
        // agent is not in the same connected component as the main zone
        else {
            return expandOwnComponent();
        }
    }

    /**
     * determines whether an agent on the frontier of the zone should try to
     * expand, parry, survey or recharge
     * @param zone the zone which the agent might expand
     * @return action resulting action
     */
    public Action determineExpansion ( Zone zone ) {
        // check for enemy-agents
        LinkedList<AgentToken> allAgents = graph.getPosition().getTokens();
        int numberOfEnemies = 0;
        boolean containsSaboteur = false;
        LinkedList<String> enemyAgents = new LinkedList<String>();
        boolean containsExplorer = false;
        boolean unknownEnemy = false;
        for ( AgentToken a : allAgents ) {
            if ( !agent.myToken.getTeam().equals(a.getTeam())
                    && !a.isDisabled() ) {
                numberOfEnemies++;
                enemyAgents.add(a.getName());
                if ( a.getRole() == null )
                    unknownEnemy = true;
                if ( a.getRole() != null && a.getRole().equals("Saboteur") ) {
                    containsSaboteur = true;
                    enemyAgents.add(a.getName());
                }
            }
            if ( a.getRole() != null && a.getRole().equals("Explorer") )
                containsExplorer = true;
        }

        // >>>>>>>things which needs to be considered<<<<<<<<<<<<<<<<
        int majority = allAgents.size() - 2 * numberOfEnemies;
        int numberOfOwnAgents = allAgents.size() - numberOfEnemies;

        // the vertex outside of the Zone that maintains the Zone
        Vertex externalCandidate = determineBestExpansion(graph.getPosition(),
                zone);

        // the Vertex that has the most Connections to other frontiervertices
        Vertex zoneCandidate = determineBestStabPosition(this.graph
                .getPosition(), zone);

        int myConnectivity = determineConnectivity(this.graph.getPosition(),
                zone);
        double stab = zone.getStabilityValue();

        // >>>>>>>>>>>>>options<<<<<<<<<<<<<<<<
        // get a random number for later indeterminant decisions
        Random generator = new Random();
        double rand = generator.nextDouble();
        // threat of getting disabled
        if ( containsSaboteur ) {
            this.agent.broadcast(new LogicBelief("zoneAlert", enemyAgents));
            if ( numberOfOwnAgents == 1 )
                return new Action("parry");
            if ( numberOfOwnAgents == 2 )
                if ( rand < 0.3 )
                    return stabilizeZone(zone);
                else
                    return new Action("parry");
            else if ( rand < 0.5 )
                return stabilizeZone(zone);
            else
                return new Action("parry");
        }

        if ( unknownEnemy ) {
            this.agent.broadcast(new LogicBelief("zoneAlert", enemyAgents));

            if ( myConnectivity == 2 && this.graph.getPosition().getValue() > 5
                    && majority < 2 ) {
                if ( graph.getPosition().hasUnsurveyedEdges() )
                    return new Action("survey");
                return new Action("recharge");
            }
        }

        // if alone on Vertex
        if ( numberOfOwnAgents == 1 ) {
            // reasons to stay and hold Zone:
            if ( (stab < 0.65) || myConnectivity == 2
                    || this.graph.getPosition().getValue() >= 8 ) {
                if ( externalCandidate != null )
                    return new Action("goto", externalCandidate.getIdentifier());
                if ( graph.getPosition().hasUnsurveyedEdges() )
                    return new Action("survey");
                else
                    return new Action("recharge");
            }

        }
        // --> other teammates on this node:
        // if the agents position is about to get probed-> stay there and if not
        // done yet-> survey
        if ( containsExplorer ) {
            if ( !graph.getPosition().isProbed()
                    && graph.getPosition().hasUnsurveyedEdges() ) {
                return new Action("survey");
            }
            return new Action("recharge");
        }
        // if the agent isolates vertices, one connection is enough -> stay
        if ( myConnectivity < 2 && zone.isCircular()
                && isolatesVertices(graph.getPosition(), zone) ) {
            if ( !graph.getPosition().isProbed()
                    && graph.getPosition().hasUnsurveyedEdges() ) {
                return new Action("survey");
            }
            return new Action("recharge");
        }

        // reasons to expand
        if ( (containsExplorer && majority > 2)
                || (!containsExplorer && majority > 1) ) {
            if ( externalCandidate != null && myConnectivity == 2 )
                return new Action("goto", externalCandidate.getIdentifier());
            return expandZoneBySize(zone);
        }
        // if the agents connection to the Zone is thin --> stabilize
        if ( myConnectivity < 2 && !zone.isCircular() ) {
            return new Action("goto", zoneCandidate.getIdentifier());
        }
        if ( myConnectivity > 2 ) {
            if ( externalCandidate != null && myConnectivity == 2 )
                return new Action("goto", externalCandidate.getIdentifier());
            return expandZoneBySize(zone);
        }
        // unstable Zone & high Value Position & alone on Vertex -> stay
        if ( zone.getStabilityValue() < 0.65
                && this.graph.getPosition().getValue() > 5 && majority == 1 ) {
            return new Action("recharge");
        }

        return new Action("recharge");

    }

    /**
     * generates an action that extends the agents connected component by
     * surveying (-> money), or taking a walk towards unsurveyed edges
     */
    public Action expandOwnComponent () {

        if ( graph.getPosition().hasUnsurveyedEdges()
                && !agent.myToken.isDisabled() ) {
            return new Action("survey");
        }
        else {
            // expand your connected component by walking to an adjacent node
            // which is unprobed/has lots of unsurveyed edges
            LinkedList<Vertex> possibleGoals = graph.getPosition()
                    .getAdjacentVertices();
            for ( Vertex v : possibleGoals ) {
                if ( (agent.myToken != null)
                        && (this.agent.myToken.getRole().equals("Explorer") && !v
                                .isProbed()) || v.hasUnsurveyedEdges() ) {
                    return new Action("goto", v.getIdentifier());
                }
            }
        }
        return randomWalk();
    }

    /**
     * returns a random goto action
     */
    public Action randomWalk () {
        LinkedList<Vertex> possibleGoals = graph.getPosition()
                .getAdjacentVertices();
        Random generator = new Random();
        int rand = generator.nextInt(possibleGoals.size());
        return new Action("goto", possibleGoals.get(rand).getIdentifier());
    }

    /**
     * generates an action that moves the agent to another agent. if the agent
     * role is at the position, the returned action is "recharge"
     * @param role the role of the agent to go to
     * @return a suitable action
     */
    public Action moveTowardsAgent ( String role ) {
        // search team tokens
        LinkedList<Vertex> positions = new LinkedList<Vertex>();
        for ( AgentToken t : graph.getTokens() ) {
            if ( t.getRole() != null
                    && t.getTeam().equals(agent.myToken.getTeam())
                    && t.getRole().equals(role) && !t.equals(agent.myToken) )
                positions.add(t.getPosition());

        }
        if ( positions.contains(agent.myToken.getPosition()) )
            return new Action("recharge");

        return moveTowardsNearest(positions);

    }

    /**
     * Is agent-type unspecific method that generates a goto action towards the
     * closest vertex part of the frontier
     * @param vertex a vertex of the zone.
     * @return a goto action towards the frontier.
     */
    public Action moveTowards ( Vertex vertex ) {
        LinkedList<Vertex> targetList = new LinkedList<Vertex>();
        targetList.add(vertex);
        return moveTowardsNearest(targetList);
    }

    /**
     * Is agent-type unspecific method that generates a goto action towards the
     * closest of the given vertices
     * @param targetList the list of targets to choose from
     * @return a goto action towards the closest target
     */
    public Action moveTowardsNearest ( LinkedList<Vertex> targetList ) {
        if ( targetList == null || targetList.isEmpty() ) {
            return expandOwnComponent();
        }
        LinkedList<LinkedList<Vertex>> listOfPaths = new LinkedList<LinkedList<Vertex>>();
        if ( agent.myToken.isDisabled() )
            listOfPaths = GraphAlgorithms.goTowards(this.graph, 4.5, 1,
                    agent.myToken.getMaxEnergyDisabled()); // J vernünftigen
                                                           // Wert (average
                                                           // recharge rate)
        else
            listOfPaths = GraphAlgorithms.goTowards(this.graph, 4.5, 1,
                    agent.myToken.getMaxEnergy());
        LinkedList<Vertex> favoritePath = null;
        for ( int i = 0; i < listOfPaths.size(); i++ ) {
            LinkedList<Vertex> p = listOfPaths.get(i);
            // check whether path leads to a target
            if ( targetList.contains(p.getLast()) ) {
                // check if its the shortest path that does so trick (so far)
                if ( !agent.myToken.isDisabled() ) {
                    if ( (favoritePath == null || favoritePath.isEmpty() || p
                            .size() - 1 <= favoritePath.size())
                            && ((agent.getGraph().getEdge(p.getFirst(),
                                    agent.getGraph().getPosition()).getWeight() <= agent.myToken
                                    .getMaxEnergy())) ) {
                        p.removeFirst();
                        favoritePath = p;
                    }
                }
                else {
                    if ( (favoritePath == null || favoritePath.isEmpty() || p
                            .size() - 1 <= favoritePath.size())
                            && ((agent.getGraph().getEdge(p.getFirst(),
                                    agent.getGraph().getPosition()).getWeight() <= agent.myToken
                                    .getMaxEnergyDisabled())) ) {
                        p.removeFirst();
                        favoritePath = p;
                    }
                }
            }
        }
        if ( favoritePath == null || favoritePath.isEmpty() ) {
            return expandOwnComponent();
        }
        agent.myToken.setTargetVertex(favoritePath.getLast());
        if ( graph.getPosition().hasUnsurveyedEdges() )
            return new Action("survey");
        return new Action("goto", favoritePath.removeFirst().getIdentifier());
    }

    /**
     * generates an action, which leads to the path with least number of
     * vertices
     * @param target the vertex to move to
     * @return action the resulting action
     */
    public Action moveFastTo ( Vertex target ) {

        LinkedList<Vertex> path = GraphAlgorithms.findFastestPath(graph,
                agent.myToken.getPosition(), target);

        if ( path == null || path.size() <= 1 || path.isEmpty() )
            return expandOwnComponent();

        path.removeFirst();
        Vertex nextVertex = path.getFirst();
        agent.myToken.setTargetVertex(path.getLast());
        return new Action("goto", nextVertex.getIdentifier());
    }

    /**
     * generates a random goto action to the outside of the Zone if Agent is on
     * frontier agent @ inside of zone: -> move towards frontier agent @ outside
     * of zone: -> move towards frontier error: -> randomWalk()
     * @return action resulting action
     */
    public Action expandZoneAtRandom ( Zone zone ) {

        if ( zone == null )
            return expandOwnComponent();

        LinkedList<Vertex> listOfOptions = new LinkedList<Vertex>();
        // betrachte NachbarKnoten
        for ( Vertex u : this.graph.getPosition().getAdjacentVertices() ) {
            if ( !zone.contains(u) ) {
                listOfOptions.add(u);
            }
        }
        // if there are no direct neighbours move towards the frontier
        if ( listOfOptions.isEmpty() ) {
            // System.out.println("ich beweeg mich auf die grneze zu");
            return moveTowardsNearest(zone.getCriticalFrontier());
        }

        Random generator = new Random();
        int rand = generator.nextInt(listOfOptions.size());
        Vertex candidate = listOfOptions.get(rand);

        // kommt kein Knoten in frage (Fehler in voriger Berechnung), wird
        // randomwalk ausgeführt
        if ( candidate == null ) {
            // System.out.println("fehler bei auswahl der erweiterung in expandZoneatRandom");
            return randomWalk();
        }
        // System.out.println("ich erweitere meine Zone");
        return new Action("goto", candidate.getIdentifier());
    }

    /**
     * creates an gotoAction towards a probably very precious Vertex outside of
     * the Zone this vertex' value is guessed by the value of its neighbours
     * @param zone
     * @return goto action
     */
    public Action expandZoneByValue ( Zone zone ) {

        if ( zone == null ) {
            return expandOwnComponent();
        }

        if ( !zone.contains(this.graph.getPosition()) ) {
            return moveTowardsNearest(zone.getCriticalFrontier());
        }
        LinkedList<Vertex> candidates = zone.getMostPreciousVertices();
        for ( Vertex v : candidates ) {
            for ( Vertex u : v.getAdjacentVertices() )
                if ( !candidates.contains(u)
                        && !u.isTarget(agent.myToken.getTeam()) )
                    return moveTowards(u);
        }
        // System.out.println("kam irgendwei nix bei raus --> dumm rumlaufen");
        return expandZoneAtRandom(zone);
    }

    /**
     * creates an goto action to a vertex outside that will expand the Zone by
     * moving to a Node outside of the Zone
     * @param zone
     */
    public Action expandZoneBySize ( Zone zone ) {

        if ( zone == null ) {
            return expandOwnComponent();
        }

        if ( !zone.contains(this.graph.getPosition()) ) {
            return moveTowardsNearest(zone.getCriticalFrontier());
        }
        Vertex externalCandidate = determineBestExpansion(graph.getPosition(),
                zone);
        if ( externalCandidate == null )
            externalCandidate = determineSizeExpansion(graph.getPosition(),
                    zone);
        if ( externalCandidate == null ) {
            return buildZone();
        }
        return new Action("goto", externalCandidate.getIdentifier());
    }

    /**
     * picks a vertex that is well connected to the current zone and goes
     * towards it
     * @return a goto action towards the inside of the zone
     */
    public Action stabilizeZone ( Zone zone ) {

        if ( zone == null ) {
            return expandOwnComponent();
        }

        LinkedList<Vertex> listOfOptions = new LinkedList<Vertex>();
        for ( Vertex u : this.graph.getPosition().getAdjacentVertices() ) {
            if ( zone.contains(u) && !u.isTarget(agent.myToken.getTeam()) ) {
                listOfOptions.add(u);
            }
        }
        // if there are no direct neighbours part of the Zone move towards the
        // weakest frontierVertex
        if ( listOfOptions.isEmpty() ) {
            return moveTowards(zone.getWeakestCriticalFrontierVertices()
                    .getFirst());
        }

        Vertex candidate = determineBestStabPosition(graph.getPosition(), zone);

        if ( candidate == null ) {
            return randomWalk();
        }
        return moveTowards(candidate);
    }

    /**
     * @param position the agents position
     * @param zone the zone he is part of and you wish to expand
     * @return a vertex outside of the zone, if that will maintain the actual
     *         zone
     */
    private Vertex determineBestExpansion ( Vertex position, Zone zone ) {
        Vertex candidate = null;
        for ( Vertex v : position.getAdjacentVertices() ) {
            if ( !zone.contains(v) && !v.isTarget(agent.myToken.getTeam()) ) {
                if ( maintainsZone(position, v, zone)
                        && !v.hasEnemyTokens(agent.myToken.getTeam()) ) {
                    candidate = v;
                }
            }
        }
        return candidate;
    }

    /**
     * @param position: the agents position
     * @param the zone he is part of and you wish to expand
     * @return a vertex outside of the zone that is connected to 2 other
     *         frontier agents
     */
    private Vertex determineSizeExpansion ( Vertex position, Zone zone ) {
        Vertex candidate = null;
        for ( Vertex v : position.getAdjacentVertices() ) {
            if ( !zone.contains(v) && !v.isTarget(agent.myToken.getTeam()) ) {
                int det = determineConnectivity(v, zone) - 1;
                if ( candidate == null
                        || (det == 2 && !v.hasEnemyTokens(agent.myToken
                                .getTeam())) ) {
                    candidate = v;
                }
            }
        }
        return candidate;
    }

    /**
     * returns the neighbour of the actual position which has the most
     * connections to other vertices of the Zone
     * @param position
     * @param zone
     * @return
     */
    private Vertex determineBestStabPosition ( Vertex position, Zone zone ) {
        Vertex candidate = null;
        int minConnect = Integer.MIN_VALUE;
        for ( Vertex v : position.getAdjacentVertices() ) {
            int det = determineConnectivity(v, zone);
            if ( candidate == null
                    || (minConnect < determineConnectivity(v, zone)
                            && v.getTokens().isEmpty() && !v
                            .isTarget(agent.myToken.getTeam())) ) {
                candidate = v;
                minConnect = det;
            }
        }
        return candidate;
    }

    /**
     * this method tells with how many frontier agents of a zone a vertex is
     * connected when you call this method from an agents surrounding. Don't
     * forget that the result will at least be 1, due to the agent itself
     * @param vertex the position
     * @param zone the zone
     * @return int the number of calid connections to other vertices of the Zone
     */
    public int determineConnectivity ( Vertex vertex, Zone zone ) {
        LinkedList<Vertex> surround = GraphAlgorithms.getSurrounding(vertex);
        int connectivity = 0;
        for ( Vertex v : surround ) {
            if ( zone.isOnCriticalFrontier(v) && !v.getTokens().isEmpty() )
                connectivity++;
        }
        return connectivity;
    }

    /**
     * this boolean is true when a move from vertex recent to vertex candidate
     * maintains the Zone important: this method should be called on a vertex
     * recent which is part of the frontier and candidate being outside of the
     * zone most useful when both recents and candidates connectivity equal 2
     * @param recent his actual position
     * @param candidate a potential Vertex outside of the Zone
     * @return
     */
    private boolean maintainsZone ( Vertex recent, Vertex candidate, Zone zone ) {
        LinkedList<Vertex> recentsSurround = GraphAlgorithms
                .getSurrounding(recent);
        LinkedList<Vertex> candidatesSurround = GraphAlgorithms
                .getSurrounding(candidate);
        // these lists will contain the frontier-agents a vertex is connected to
        // (distance <=2)
        LinkedList<Vertex> recentsConnections = new LinkedList<Vertex>();
        LinkedList<Vertex> candidatesConnections = new LinkedList<Vertex>();
        // fill lists
        for ( Vertex v : recentsSurround ) {
            if ( zone.isOnCriticalFrontier(v) && !v.getTokens().isEmpty() )
                recentsConnections.add(v);
        }
        for ( Vertex v : candidatesSurround ) {
            if ( zone.isOnCriticalFrontier(v) && !v.getTokens().isEmpty() )
                candidatesConnections.add(v);
        }
        // check whether the candidates neighbours include the old ones
        return candidatesConnections.containsAll(recentsConnections);
    }

    /**
     * returns true if the Vertex position (which should be part of the
     * frontier) isolates vertices)
     * @param position his position
     * @param zone
     * @return
     */
    private boolean isolatesVertices ( Vertex position, Zone zone ) {
        for ( Vertex v : position.getAdjacentVertices() ) {
            if ( zone.getIsolatedVertices().contains(v) )
                return true;
        }
        return false;
    }

    public void setGraph ( Graph graph ) {
        this.graph = graph;
    }

    /**
     * @return the graph
     */
    public Graph getGraph () {
        return graph;
    }

}
