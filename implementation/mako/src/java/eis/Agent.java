package eis;

/**
 * @author Artur Daudrich
 * @author Michael Sewell
 */
public class Agent {

    private String serverName;
    private String team; // teamA or teamB
    private String enemyTeam;
    private String entity; // e.g. connectionA1
    private String jasonName;

    private int energy;
    private int health;
    private int maxEnergy;
    private int maxHealth;
    private Vertex node;
    private String role;
    private int strength;
    private int visRange;

    private boolean isInspected = false;

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String name) {
        this.serverName = name;
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

    public String getJasonName() {
        return jasonName;
    }

    public void setJasonName(String type) {
        this.jasonName = type;
    }

    public String getEnemyTeam() {
        return enemyTeam;
    }

    public void setEnemyTeam(String enemyTeam) {
        this.enemyTeam = enemyTeam;
    }

    public int getEnergy() {
        return energy;
    }

    public void setEnergy(int energy) {
        this.energy = energy;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public int getMaxEnergy() {
        return maxEnergy;
    }

    public void setMaxEnergy(int maxEnergy) {
        this.maxEnergy = maxEnergy;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
    }

    public Vertex getNode() {
        return node;
    }

    public void setNode(Vertex node) {
        this.node = node;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public int getStrength() {
        return strength;
    }

    public void setStrength(int strength) {
        this.strength = strength;
    }

    public int getVisRange() {
        return visRange;
    }

    public void setVisRange(int visRange) {
        this.visRange = visRange;
    }

    public boolean isInspected() {
        return isInspected;
    }

    public void setInspected(boolean isInspected) {
        this.isInspected = isInspected;
    }
}
