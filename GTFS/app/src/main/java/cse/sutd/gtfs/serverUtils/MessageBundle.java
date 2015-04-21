package cse.sutd.gtfs.serverUtils;

import java.util.Map;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by Glen on 02/04/2015.
 */
public class MessageBundle {

    //Predefined message types
    public static enum messageType{
        AUTH, TEXT,TEXT_RECEIVED, TYPING, CREATE_SINGLE_ROOM, CREATE_ROOM,ROOM_INVITATION,
        SINGLE_ROOM_INVITATION,ACCEPT_INVITATION, LEAVE, GROUP_EXPIRED, GET_USERS,
        FETCH_NOTE, EDIT_NOTE, GET_ROOMS, CREATE_NOTE, GET_NOTES;
    }

    private Map<String, String> messageMap;
    private List<String> tags;

    public static final String SESSION_TOKEN= "session_token";
    public static final String FROM_PHONE_NUMBER = "from_phone_number";
    public static final String TYPE = "type";
    public static final String USERNAME = "username";
    public static final String CHATROOMID = "chatroom_id";
    public static final String CHATROOM_NAME = "chatroom_name";
    public static final String MESSAGE = "message";
    public static final String MESSAGE_ID = "message_id";
    public static final String NOTE_ID = "note_id";
    public static final String NOTE_TITLE = "note_title";
    public static final String NOTE_TEXT = "note_text";
    public static final String NOTE_CREATOR = "note_creator";
    public static final String NOTES = "notes";
    public static final String TO_PHONE_NUMBER = "to_phone_number";
    public static final String USERS = "users";
    public static final String TIMESTAMP= "timestamp";
    public static final String STATUS = "status";
    public static final String EXPIRY = "expiry";
    public static final String CHATROOMS = "chatrooms";
    public static final String TAGS = "tags";
    public static final String VALID_STATUS = "1";

//    private String time;

    public MessageBundle(String fromNumber, String sessionToken, messageType type) {
        super();
        messageMap = new HashMap<>();
        messageMap.put(FROM_PHONE_NUMBER, fromNumber);
        messageMap.put(SESSION_TOKEN, sessionToken);
        messageMap.put(TYPE, type.toString());
        if(type == messageType.CREATE_ROOM || type == messageType.CREATE_SINGLE_ROOM )
            messageMap.put(EXPIRY, "0"  );
        if(type == messageType.TEXT)
            tags = new ArrayList<>();
        putTimestamp();
    }

    public String putUsername(String username){
        return messageMap.put(USERNAME, username);
    }

    public String putChatroomID(String chatRoomID){
        return messageMap.put(CHATROOMID, chatRoomID);
    }

    private String putTimestamp(){
        Calendar c = Calendar.getInstance();
        String timeStamp = String.format("%02d:%02d", c.get(Calendar.HOUR_OF_DAY),
                c.get(Calendar.MINUTE));
        return messageMap.put(TIMESTAMP, timeStamp);
    }

    public String putTimestamp(String timestamp){
        return messageMap.put(TIMESTAMP, timestamp);
    }

    public String putMessage(String message){
        return messageMap.put(MESSAGE, message);
    }

    public String putNoteID(String noteID){
        return messageMap.put(NOTE_ID, noteID);
    }

    public String putToPhoneNumber(String toPhoneNumber){
        return messageMap.put(TO_PHONE_NUMBER, toPhoneNumber);
    }

    public String putUsers(String[] users){
        return messageMap.put(USERS, Arrays.toString(users));
    }

    public String putChatroomName(String chatroomName){
        return messageMap.put(CHATROOM_NAME, chatroomName);
    }

    public String putExpiry(TimeUnit timeUnit, long duration){
        return messageMap.put(EXPIRY, String.valueOf(System.currentTimeMillis() +
                timeUnit.toMillis(duration)));
    }

    public String putNoteText(String noteText){
        return messageMap.put(NOTE_TEXT, noteText);
    }

    public String putNoteTitle(String noteTitle){
        return messageMap.put(NOTE_TITLE, noteTitle);
    }

    public String putTag(String tag){
        tags.add(tag);
        return messageMap.put(TAGS, tags.toString());
    }

    public String putParsedTags(String tags){
        return messageMap.put(TAGS, tags);
    }

    public Map getMessage(){
        return this.messageMap;
    }

//    public String getTime() {
//        return time;
//    }

//    public void setTime(Calendar time) {
//        this.time = (new SimpleDateFormat("HH:mm")).format(time.getTime());
//    }
}
