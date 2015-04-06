package cse.sutd.gtfs.Utils;

/**
 * Created by Francisco Furtado on 27/03/2015.
 */

public class MessageBundle {

    //Predefined message types
    public static enum messageType{
        TEXT, TYPING, INVITE, CLIENT_RECEIVED, NEW_MESSAGE,
        FETCH_NOTE, EDIT_NOTE;
    }

    private String fromID;
    private String message;
    private messageType type;
    private boolean isMine;
    public MessageBundle(String fromID, String message, messageType type, boolean mine) {
        super();
        this.fromID = fromID;
        this.message = message;
        this.type = type;
        this.isMine=mine;
    }

    public String toString(){
        return String.format("ID: %s\nMessage:%s", fromID, message);
    }

    public String getFromID() {
        return fromID;
    }

    public String getMessage() {
        return message;
    }

    public messageType getType() {
        return type;
    }

    public boolean isMine(){return isMine;}
}
