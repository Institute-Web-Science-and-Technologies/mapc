package eis;

public class Agent {

	private String name;
	private String team;
	private String entity;
	private String type;
	
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
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public void print() {
		System.out.println("[" +team +"]Name: " + name + " Entity: " +entity + " Type: " + type);
	}
}
