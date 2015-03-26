package com.example.haductien.chatapp.JSon;

/**
 * Created by Francisco Furtado on 24/03/2015.
 */

public class MessageBundle {

    //Predefined message types
    public static enum messageType{
        TEXT, TYPING, INVITATION, ACCEPT_INVITATION,
        CLIENT_RECEIVED, NEW_MESSAGE,
        GET_NOTE, EDIT_NOTE, DELETE_NOTE,
        AUTH, CREATE_ROOM, LEAVE;
    }

    private String fromID;
    private String message;
    private messageType type;

    public MessageBundle(String fromID, String message, messageType type) {
        super();
        this.fromID = fromID;
        this.message = message;
        this.type = type;
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
}
