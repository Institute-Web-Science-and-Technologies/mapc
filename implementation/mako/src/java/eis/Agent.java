package eis;


/**
 * @author Artur Daudrich
 * @author Michael Sewell
 */
public class Agent {

    private String serverName;
    private String team;
    private String entity;
    private String jasonName;

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
}
