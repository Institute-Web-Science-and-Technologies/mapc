package eis;


/**
 * @author Artur Daudrich
 * @author Michael Sewell
 */
public class Agent {

    private String name;
    private String team;
    private String entity;
    private String internalName;

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

    public String getInternalName() {
        return internalName;
    }

    public void setInternalName(String type) {
        this.internalName = type;
    }
}
