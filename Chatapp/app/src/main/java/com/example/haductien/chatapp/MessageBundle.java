package com.example.haductien.chatapp;

/**
 * Created by Francisco Furtado on 24/03/2015.
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
