package cse.sutd.gtfs.Objects;

/**
 * Created by Francisco Furtado on 10/04/2015.
 */
public class ChatRooms {
    private String id;
    private String name;
    private String lastmsg;

    public ChatRooms(String id,String name, String lastmsg){
        this.id=id;
        this.name=name;
        this.lastmsg=lastmsg;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastmsg() {
        return lastmsg;
    }

    public void setLastmsg(String lastmsg) {
        this.lastmsg = lastmsg;
    }
}
