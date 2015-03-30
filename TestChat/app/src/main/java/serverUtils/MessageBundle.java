package serverUtils;

public class MessageBundle {
	
	//Predefined message types
	public static enum messageType{
		AUTH, TEXT, TYPING, CREATE_ROOM,INVITATION,
        ACCEPT_INVITATION, LEAVE, GROUP_EXPIRED,
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
