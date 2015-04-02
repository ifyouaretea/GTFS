package serverUtils;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class MessageBundle {
	
	//Predefined message types
	public static enum messageType{
		AUTH, TEXT, TYPING, CREATE_ROOM,INVITATION,
        ACCEPT_INVITATION, LEAVE, GROUP_EXPIRED,
		FETCH_NOTE, EDIT_NOTE;
	}

    private Map<String, String> messageMap;

    public static final String SESSION_TOKEN= "session_token";
    public static final String FROM_PHONE_NUMBER = "from_phone_number";
    public static final String TYPE = "type";
    public static final String USERNAME = "username";
    public static final String CHATROOMID = "chatroom_id";
    public static final String MESSAGE = "message";
    public static final String NOTEID = "noteID";
    public static final String TO_PHONE_NUMBER = "to_phone_number";
    public static final String TIMESTAMP= "timestamp";
    public static final String STATUS = "status";
    public static final String VALID_STATUS = "1";

	public MessageBundle(String fromNumber, String sessionToken, messageType type) {
		super();
		messageMap = new HashMap<>();
        messageMap.put(FROM_PHONE_NUMBER, fromNumber);
        messageMap.put(SESSION_TOKEN, sessionToken);
        messageMap.put(TYPE, type.toString());
        putTimestamp();
	}

    public String putUsername(String username){
        return messageMap.put(USERNAME, username);
    }

    public String putChatroomID(String chatRoomID){
        return messageMap.put(CHATROOMID, chatRoomID);
    }

    public String putTimestamp(){
        Calendar c = Calendar.getInstance();
        StringBuilder sb = new StringBuilder();
        sb.append(c.get(Calendar.HOUR_OF_DAY) + ":");
        sb.append(c.get(Calendar.MINUTE) + ":");
        sb.append(c.get(Calendar.SECOND));

        return messageMap.put(TIMESTAMP, sb.toString());
    }
    public String putMessage(String message){
        return messageMap.put(MESSAGE, message);
    }

    public String putNoteID(String noteID){
        return messageMap.put(NOTEID, noteID);
    }

    public String putToPhoneNumber(String toPhoneNumber){
        return messageMap.put(TO_PHONE_NUMBER, toPhoneNumber);
    }

    public Map getMessage(){
        return this.messageMap;
    }
}
