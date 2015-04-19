package cse.sutd.gtfs.Objects;

/**
 * Created by Francisco Furtado on 10/04/2015.
 */
public class ChatRoom {
    private String id;
    private String name;
    private String lastmsg;
    private int isGroup;

    public ChatRoom(String id, String name, String lastmsg, int isGroup){
        this.id=id;
        this.name=name;
        this.lastmsg=lastmsg;
        this.isGroup = isGroup;
    }

    public ChatRoom(String id, String name, int isGroup){
        this.id=id;
        this.name=name;
        this.isGroup = isGroup;
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

    public int getIsGroup() {return isGroup;   }

    public void setIsGroup(int isGroup) { this.isGroup = isGroup;   }
}
