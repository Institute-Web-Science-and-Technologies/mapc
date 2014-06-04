package graph;

import java.util.HashSet;

import eis.Agent;

/**
 * This class models a vertex of a graph.
 * 
 * @author Sergey Dedukh
 * @author Miriam Koelle
 * @author Michael Ruster
 */
public class Vertex {

	private String id;
	private int value = 1;
	private boolean isVisited = false;
	private boolean isProbed = false;
	private HashSet<Agent> teamAgents;
	private HashSet<Agent> enemyAgents;
	private TeamEnum zoneTeam;

	Vertex(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getValue() {
		return value;
	}

	public TeamEnum getZoneTeam() {
		return zoneTeam;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public boolean isVisited() {
		return isVisited;
	}

	public void setVisited(boolean isVisited) {
		this.isVisited = isVisited;
	}

	public boolean isProbed() {
		return isProbed;
	}

	public void setProbed(boolean isProbed) {
		this.isProbed = isProbed;
	}

	public HashSet<Agent> getTeamAgents() {
		return teamAgents;
	}

	public void setTeamAgents(HashSet<Agent> teamAgents) {
		this.teamAgents = teamAgents;
	}

	public HashSet<Agent> getEnemyAgents() {
		return enemyAgents;
	}

	public void setEnemyAgents(HashSet<Agent> enemyAgents) {
		this.enemyAgents = enemyAgents;
	}

	public void setZoneTeam(TeamEnum zoneTeam) {
		this.zoneTeam = zoneTeam;
	}

	public TeamEnum getOccupyingTeam() {
		return null;
	}
}
