package eis;

/**
 * @author Artur Daudrich
 * @author Michael Sewell
 */
public class Agent {
    // private AgentLogger logger = new AgentLogger("MapAgent");
    private String serverName; // e.g. "a16"
    private String team; // teamA or teamB
    private String enemyTeam; // TODO: remove this
    private String entity; // e.g. connectionA1
    private String jasonName; // e.g. "explorer1"

    private int energy;
    private int health;
    private int maxEnergy;
    private int maxHealth;
    private Vertex position;
    private String role;
    private int strength;
    private int visRange = 1; // assume the minimal visRange by default

    // if the agent is available for zoning tasks
    private boolean availableForZoning = false;
    // a sub-state of availableForZoning: true if we're currently building a
    // zone
    private boolean buildingZone = false;
    private Zone zone;

    // the step # this agent was last inspected on
    private int inspectionStep = 0;
    private boolean disabled = false;

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

    /**
     * @param currentStep
     *            the current step
     * @return true if the agent has been inspected since a specific number of
     *         steps (currently 20)
     */
    public boolean isInspected() {
        int currentStep = MapAgent.getInstance().getStep();
        return ((inspectionStep != 0) && (currentStep - inspectionStep) < 20);
    }

    public void setInspectionStep(int inspectionStep) {
        this.inspectionStep = inspectionStep;
    }

    public Vertex getPosition() {
        return position;
    }

    public void setPosition(Vertex position) {
        this.position = position;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public Zone getZone() {
        return zone;
    }

    public void setZone(Zone zone) {
        this.zone = zone;
    }

    public String getEnemyTeam() {
        return enemyTeam;
    }

    public void setEnemyTeam(String enemyTeam) {
        this.enemyTeam = enemyTeam;
    }

    public boolean isAvailableForZoning() {
        return availableForZoning;
    }

    public void setAvailableForZoning(boolean availableForZoning) {
        this.availableForZoning = availableForZoning;
        setBuildingZone(false);
    }

    public boolean isBuildingZone() {
        return buildingZone;
    }

    public void setBuildingZone(boolean buildingZone) {
        this.buildingZone = buildingZone;
    }

    public boolean isInOurTeam() {
        return team.equalsIgnoreCase(AgentHandler.selectedTeam);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Agent [serverName=" + serverName + ", jasonName=" + jasonName + "]";
    }

}
