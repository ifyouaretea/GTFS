package cse.sutd.gtfs.messageManagement;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cse.sutd.gtfs.Activities.Messaging.MainActivity;
import cse.sutd.gtfs.GTFSClient;
import cse.sutd.gtfs.R;
import cse.sutd.gtfs.serverUtils.MessageBundle;


/**
 * Created by Glen on 02/04/2015.
 */
public class MessageDbAdapter {

    SQLiteDatabase mDb;
    DatabaseHelper mDbHelper;
    Context mContext;

    public static final String MESSAGES = "messages";
    public static final String CHATS= "chats";
    public static final String EVENTS= "events";
    public static final String ROWID = "_id";
    public static final String CHATID = "chatID";
    public static final String TIMESTAMP = "timestamp";
    public static final String FROM_PHONE_NUMBER = "from_phone_number";
    public static final String PHONE_NUMBER = "phone_number";
    public static final String CHATNAME = "chatName";
    public static final String LAST_MESSAGE = "lastMessage";
    public static final String READ = "read";
    public static final String USERS = "users";
    public static final String NAME = "name";
    public static final String EXPIRY = "expiry";
    public static final String CONTACTS = "contacts";
    public static final String ISGROUP = "isGroup";
    public static final String BODY = "body";
    public static final String NOTE_CREATOR = "noteCreator";
    public static final String TITLE = "title";
    public static final String NOTES = "notes";
    public static final String TAGS = "tags";

    public static final String EVENT_NAME = "event_name";
    public static final String EVENT_ID = "event_id";
    public static final String EVENT_DATE = "event_datetime";
    public static final String VOTES = "votes";
    public static final String DELETED = "deleted";

    private static final String TAG = "MessageDbAdapter";

    public static final String DATABASE_CREATE_NOTES =
            "create table notes (_id text primary key, "
                    + "title text not null, " +
                    "body text not null, chatID text not null, noteCreator text);";

    private static final String DATABASE_CREATE_EVENTS =
            "create table events (_id text primary key, "
                    + "eventName text not null, eventDate text not null, chatID text," +
                    " votes text);";

    private static MessageDbAdapter instance;
    private static class DatabaseHelper extends SQLiteOpenHelper {
        private static final String DATABASE_CREATE_MESSAGES =
                "create table messages (_id integer primary key, "
                        + "chatID text not null, body text not null," +
                        "from_phone_number text not null, timestamp text not null, " +
                        "read integer not null, tags text);";

        private static final String DATABASE_CREATE_CHATS =
                "create table chats (_id text primary key, "
                        + "isGroup integer not null, chatName text, " +
                        "lastMessage integer not null, "+
                        "users text, expiry integer, deleted integer not null);";

        private static final String DATABASE_CREATE_CONTACTS =
                "create table contacts (_id text primary key, "
                        + "name text not null, chatID text);";

        private static final String DATABASE_CREATE_EVENTS =
                "create table events (_id text primary key, "
                        + "eventName text not null, event_datetime text not null, chatID text," +
                        " votes text);";

        private static final String DATABASE_NAME = "data";

        private static final int DATABASE_VERSION = 2;

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE_MESSAGES);
            db.execSQL(DATABASE_CREATE_CHATS);
            db.execSQL(DATABASE_CREATE_CONTACTS);
            db.execSQL(DATABASE_CREATE_NOTES);
            db.execSQL(DATABASE_CREATE_EVENTS);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS notes");
            db.execSQL("DROP TABLE IF EXISTS chats");
            db.execSQL("DROP TABLE IF EXISTS contacts");
            db.execSQL("DROP TABLE IF EXISTS messages");
            db.execSQL("DROP TABLE IF EXISTS events");
            onCreate(db);
        }
    }

    public static MessageDbAdapter getInstance(Context context){
        if(instance == null)
            instance = new MessageDbAdapter(context.getApplicationContext());
        return instance;
    }
    private MessageDbAdapter(Context context){mContext = context;}

    public MessageDbAdapter open() throws SQLException{
        mDbHelper = new DatabaseHelper(mContext);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close(){mDbHelper.close();}

    /**
     *
     * @param message
     * @return -1 if not inserted, 1 if inserted correctly
     */
    public int storeMessage(Map message){

        String chatID = (String) message.get(MessageBundle.CHATROOMID);
        String messageID = (String) message.get(MessageBundle.MESSAGE_ID);
        String timestamp = (String) message.get(MessageBundle.TIMESTAMP);
        String from_phone_number = (String) message.get(MessageBundle.FROM_PHONE_NUMBER);
        String body = (String) message.get(MessageBundle.MESSAGE);
        String tags = (String) message.get(MessageBundle.TAGS);

        body = body.replaceAll("'", "''");
        Log.d("Database store", message.toString());

        Cursor messageExists = mDb.rawQuery(String.format("SELECT _id FROM messages " +
                "WHERE _id='%s';", messageID), null);

        //check if a copy of the message is already in the database
        if (messageExists.getCount() != 0) {
            messageExists.close();
            return -1;
        }

        Cursor groupExists = mDb.rawQuery(String.format("SELECT _id FROM chats " +
                "WHERE _id='%s';", chatID), null);

        //check if a copy of the message is already in the database
        if (groupExists.getCount() < 1) {
            groupExists.close();
            return -1;
        }

        String updateSQL = "UPDATE chats SET " + LAST_MESSAGE + " = '" + messageID +
                "' WHERE _id = '" + chatID + "'";
        mDb.execSQL(updateSQL);

        ContentValues messageValues = new ContentValues();
        messageValues.put(CHATID, chatID);
        messageValues.put(ROWID, messageID);
        messageValues.put(TIMESTAMP, timestamp);
        messageValues.put(BODY, body);
        messageValues.put(FROM_PHONE_NUMBER, from_phone_number);
        messageValues.put(TAGS, tags);
        if(from_phone_number.equals(((GTFSClient)mContext.getApplicationContext()).getID()))
            messageValues.put(READ, 1);
        else
            messageValues.put(READ, 0);

        //if inserting fails, return -1
        if (mDb.insert(MESSAGES, null, messageValues) < 0)
            return -1;

        return 1;
    }

    public boolean createSingleChat(Map message){
        String chatID = (String) message.get(MessageBundle.CHATROOMID);
        String from_phone_number = (String) message.get(MessageBundle.FROM_PHONE_NUMBER);

        Cursor chatExists = mDb.rawQuery(String.format("SELECT _id FROM chats WHERE " +
                "_id='%s'", chatID), null);

        boolean isChatExists = chatExists.getCount() > 0;
        chatExists.close();

        Object[] users = (Object[]) message.get(USERS);
        if(!isChatExists) {
            ContentValues chatValues = new ContentValues();
            chatValues.put(ROWID, chatID);
            chatValues.put(USERS, Arrays.toString(users));
            chatValues.put(CHATNAME, from_phone_number);
            chatValues.put(LAST_MESSAGE, chatID);
            chatValues.put(ISGROUP, 0);
            chatValues.put(DELETED, 0);

            if (mDb.insert(CHATS, null, chatValues) <= 0)
                return false;
        }

        String ownID = ((GTFSClient) mContext).getID();
        String otherUser = null;

        for(Object user: users)
            if (!ownID.equals(user)) {
                otherUser = (String) user;
                break;
            }
        try {

            mDb.execSQL(String.format("UPDATE contacts SET chatID='%s' WHERE _id='%s'",
                    chatID, otherUser));
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public Cursor getChatMessages(String chatID){
        return mDb.rawQuery(String.format("SELECT from_phone_number, body, " +
                "timestamp, tags FROM messages WHERE chatID ='%s' ORDER BY _id", chatID), null);
    }

    public Cursor getUserForGroup(String chatID){
        return mDb.rawQuery(String.format("SELECT users FROM chats WHERE _id ='%s'", chatID), null);
    }

    public Cursor getChats(){
        return mDb.rawQuery("SELECT _id, chatName, isGroup FROM " +
                "chats ORDER BY lastMessage DESC", null);
    }

    public Cursor getUndeletedChats(){
        return mDb.rawQuery("SELECT _id, chatName, isGroup FROM " +
                "chats WHERE deleted= 0 ORDER BY lastMessage DESC ", null);
    }

    public Cursor getContacts(){
        return mDb.rawQuery("SELECT _id, name FROM " +
                "contacts", null);
    }

    public Cursor getContact(String phoneNumber){
        return mDb.rawQuery("SELECT _id, name FROM " +
                "contacts WHERE _id = '"+phoneNumber+"'", null);
    }

    public long createGroupChat(Map message){
        String chatID = (String) message.get(MessageBundle.CHATROOMID);
        String chatName = (String) message.get(MessageBundle.CHATROOM_NAME);
        String users = Arrays.toString((Object[])message.get(USERS));
        if(users == null)
            return -1;
        if(users.length() < 1)
            return -1;

        Integer isGroup = null;

        isGroup = 1;

        Long expiry = (Long) message.get(MessageBundle.EXPIRY);

        ContentValues chatValues = new ContentValues();
        chatValues.put(ISGROUP, isGroup);
        chatValues.put(ROWID, chatID);
        chatValues.put(CHATNAME, chatName);
        chatValues.put(USERS, users);
        chatValues.put(LAST_MESSAGE, chatID);
        chatValues.put(EXPIRY, expiry);
        chatValues.put(DELETED, 0);
        return mDb.insert(CHATS, null, chatValues);
    }



    public long putContact(String phoneNum, String contactName){
        Cursor searchChats = mDb.rawQuery("SELECT _id from chats WHERE users LIKE " +
                "'%" + phoneNum + "%'", null);

        String chatID = null;
        if(searchChats != null)
            if(searchChats.getCount() > 0){
                searchChats.moveToFirst();
                chatID = searchChats.getString(0);
                searchChats.close();
            }

        ContentValues chatValues = new ContentValues();
        chatValues.put(ROWID, phoneNum);
        chatValues.put(NAME, contactName);
        chatValues.put(CHATID, chatID);
        try {
            return mDb.insert(CONTACTS, null, chatValues);
        }catch (SQLiteConstraintException e) {
            return 0;
        }
    }

    public void deleteGroupChat(String chatID){
        mDb.execSQL(String.format("UPDATE chats SET deleted=1 WHERE _id='%s'", chatID));
    }

    /**
     * @return an array of the IDs removed
     */
    public String[] deleteExpiredChats(){
        long currentTime = System.currentTimeMillis();
        Cursor expiredChats = mDb.rawQuery("SELECT _id FROM chats WHERE deleted = 0 AND expiry <> 0"
                +" AND expiry <= "+ currentTime, null);
        if(expiredChats == null)
            return null;
        if (expiredChats.getCount() == 0) {
            expiredChats.close();
            return new String[]{};
        }
        expiredChats.moveToFirst();

        String[] expiredArray = new String[expiredChats.getCount()];
        int count = 0;
        do{
            expiredArray[count++] = expiredChats.getString(0);
            deleteGroupChat(expiredChats.getString(0));
        }while(expiredChats.moveToNext());
        expiredChats.close();

        GTFSClient client = ((GTFSClient) mContext.getApplicationContext());
        List<String> applicationExpiredList = client.getExpiredChatList();

        for(String expiredChat : expiredArray)
            applicationExpiredList.add(getChatroomName(expiredChat));
        String body = "";

        for(String expiredChat: applicationExpiredList)
            body += expiredChat + "\n";
        body = body.substring(0, body.length()-1);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(mContext)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Chats have expired")
                .setContentText(body)
                .setContentIntent(PendingIntent.getActivity(mContext.getApplicationContext()
                        , 0, new Intent(client, MainActivity.class),
                        PendingIntent.FLAG_ONE_SHOT))
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(body))
                .setPriority(Notification.PRIORITY_HIGH);

        NotificationManager mNotificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, mBuilder.build());

        return expiredArray;
    }

    public String getChatroomName(String chatID){
        Cursor result = mDb.rawQuery(String.format("SELECT chatName, isGroup" +
                        " FROM chats WHERE _id = '%s'",
                chatID), null);
        if (result == null)
            return null;

        if(result.getCount() < 1) {
            result.close();
            return null;
        }

        result.moveToFirst();
        String returnValue = result.getString(0);
        boolean isGroup = result.getInt(1) == 1;
        result.close();
        if (isGroup)
            return returnValue;
        else{
            Cursor usernameResult = mDb.rawQuery(String.format("SELECT name" +
                            " FROM contacts WHERE chatID = '%s'",
                    chatID), null);
            if (usernameResult== null)
                return null;

            if(usernameResult.getCount() < 1) {
                usernameResult.close();
                return null;
            }
            usernameResult.moveToFirst();
            returnValue = usernameResult.getString(0);
            usernameResult.close();
            return returnValue;
        }
    }

    public boolean isGroup(String chatID){
        Cursor result = mDb.rawQuery(String.format("SELECT isGroup FROM chats " +
                "WHERE _id = '%s'", chatID),null);
        if (result == null)
            return false;
        if (result.getCount() < 1) {
            result.close();
            return false;
        }

        result.moveToFirst();
        int isGroup = result.getInt(0);
        result.close();
        return isGroup == 1;
    }

    public boolean isChatDeleted(String chatID){
        Cursor result = mDb.rawQuery("SELECT _id FROM chats WHERE deleted = 1",null);
        if(result.getCount() < 1) {
            result.close();
            return false;
        }

        boolean isDeleted = false;
        result.moveToFirst();
        do {
            if (result.getString(0).equals(chatID)){
                isDeleted = true;
                break;
            }
        }while(result.moveToNext());
        result.close();
        return isDeleted;
    }

    public String getChatroomID(String chatName){
        Cursor result = mDb.rawQuery(String.format("SELECT _id FROM chats WHERE chatName = '%s'",
                chatName), null);
        if(result.getCount() < 1)
            return null;
        result.moveToFirst();
        String returnValue = result.getString(0);
        return returnValue;
    }



/*    public String getChatIDForUser(String userID){
        Cursor result = mDb.rawQuery("SELECT _id FROM chats WHERE isGroup = 0 " +
                "AND users LIKE '%" + userID + "%'", null);
        if(result.getCount() != 1) {
            result.close();
            return null;
        }
        result.moveToFirst();
        String returnValue = result.getString(0);
        result.close();
        return returnValue;
    }*/

    public String getChatIDForUser(String userID){
        Cursor result = mDb.rawQuery("SELECT chatID FROM contacts WHERE _id = " +
                "'" + userID + "'", null);

        if(result.getCount() != 1) {
            result.close();
            return null;
        }
        result.moveToFirst();
        String returnValue = result.getString(0);
        result.close();
        return returnValue;
    }

    public void clearRead(String chatID){
        mDb.execSQL(String.format("UPDATE messages SET read=1 WHERE chatID='%s'", chatID));
    }

    public long getUnreadCount(String chatID){
        Cursor unreadMessages = mDb.rawQuery(String.format("SELECT read " +
                "FROM messages WHERE read = 0 AND chatID ='%s'", chatID), null);
        long returnValue = unreadMessages.getCount();
        unreadMessages.close();
        return returnValue;
    }

/*
    public String getUsername(String chatID){
        Cursor userCursor = mDb.rawQuery(String.format("SELECT users FROM chats WHERE" +
                " _id ='%s'", chatID), null);
        if (userCursor == null)
            return null;
        if (userCursor.getCount() < 1){
            userCursor.close();
            return null;
        }
        userCursor.moveToFirst();

        String userList = userCursor.getString(0);
        userList = userList.replaceAll(((GTFSClient) mContext.getApplicationContext()).getID(), "");
        String phoneNumber = userList.replaceAll("\\D", "");
        userCursor.close();

        Cursor usernameCursor = mDb.rawQuery(String.format("SELECT name FROM contacts WHERE" +
                " _id ='%s'", phoneNumber), null);
        if (usernameCursor == null)
            return null;
        if (usernameCursor.getCount() < 1){
            usernameCursor.close();
            return null;
        }
        usernameCursor.moveToFirst();
        String username = usernameCursor.getString(0);
        usernameCursor.close();
        return username;
    }*/


    public String getUsername(String chatID){
        Cursor userCursor = mDb.rawQuery(String.format("SELECT name FROM contacts WHERE" +
                " chatID ='%s'", chatID), null);
        if (userCursor == null)
            return null;
        if (userCursor.getCount() < 1){
            userCursor.close();
            return null;
        }
        userCursor.moveToFirst();
        String username = userCursor.getString(0);
        userCursor.close();
        return username;
    }

    public String getUsernameFromNumber(String phoneNumber){
        Cursor userCursor = mDb.rawQuery(String.format("SELECT name FROM contacts WHERE" +
                " _id ='%s'", phoneNumber), null);
        if (userCursor == null)
            return null;
        if (userCursor.getCount() < 1){
            userCursor.close();
            return null;
        }
        userCursor.moveToFirst();
        String username = userCursor.getString(0);
        userCursor.close();
        return username;
    }

    public void importChatrooms(Map message){
        Object[] chatrooms = (Object[])message.get(MessageBundle.CHATROOMS);
        for(Object chatroomO : chatrooms){
            Map chatroom = (Map) chatroomO;

            Log.d("Importing chatroom", chatroom.toString());
            String chatID = (String) chatroom.get(MessageBundle.CHATROOMID);
            String chatName = (String) chatroom.get(MessageBundle.CHATROOM_NAME);
            String users = Arrays.toString((Object[])chatroom.get(USERS));
            int isGroup = (Boolean) chatroom.get("group") ? 1 : 0;
            long expiry = Long.parseLong((String) chatroom.get(MessageBundle.EXPIRY));

            ContentValues chatValues = new ContentValues();
            chatValues.put(ROWID, chatID);
            chatValues.put(CHATNAME, chatName);
            chatValues.put(USERS, users);
            chatValues.put(LAST_MESSAGE, chatID);
            chatValues.put(DELETED, 0);
            chatValues.put(EXPIRY, expiry);
            chatValues.put(ISGROUP, isGroup);
            mDb.insert(CHATS, null, chatValues);
        }
    }

    public void importUsers(Map message){
        Object[] users =  (Object[])message.get(MessageBundle.USERS);
        if (users == null)
            return;
/*
        Cursor phones = mContext.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        phones.moveToFirst();
        HashMap<String,String> phoneName = new HashMap<>();
        do{
            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)).trim();
            phoneNumber = phoneNumber.replace("+65", "");
            phoneNumber = phoneNumber.replaceAll("\\D", "");
            String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)).trim();
            if (phoneNumber.length() >= 8) {
                phoneName.put(phoneNumber,name);
            }
        }while(phones.moveToNext());

        phones.close();*/

        for(Object  user : users){
            String username = (String) ((Map)user).get(MessageBundle.USERNAME);
            String phoneNumber = (String) ((Map)user).get(PHONE_NUMBER);
            putContact(phoneNumber, username);
        }
    }

    public void importNotes(Map message){
        mDb.execSQL("DROP TABLE IF EXISTS notes");
        mDb.execSQL(DATABASE_CREATE_NOTES);
        Object[] notes = (Object[]) message.get(MessageBundle.NOTES);
        if (notes == null)
            return;
        String chatID = (String) message.get(MessageBundle.CHATROOMID);
        for(Object note : notes)
            createNote(chatID, (Map) note);
    }

    public long createNote (String chatID, Map message){
        Log.d("Note insertion", message.toString());
        String noteID = (String) message.get(MessageBundle.NOTE_ID);
        String note_title = (String) message.get(MessageBundle.NOTE_TITLE);
        String note_text = (String) message.get(MessageBundle.NOTE_TEXT);
        String note_creator = (String) message.get(MessageBundle.NOTE_CREATOR);

        ContentValues noteValues = new ContentValues();
        noteValues.put(ROWID, noteID);
        noteValues.put(CHATID, chatID);
        noteValues.put(TITLE, note_title);
        noteValues.put(BODY, note_text);
        noteValues.put(NOTE_CREATOR, note_creator);

        return mDb.insert(NOTES, null, noteValues);
    }

    public String getLatestMessage(String chatID){
        Cursor result = mDb.rawQuery(String.format("SELECT messages.body FROM messages INNER JOIN "
                        +"chats ON chats.lastMessage = messages._id WHERE chats._id ='%s'",
                chatID), null);
        if (result == null)
            return null;

        result.moveToFirst();

        if(result.getCount() > 0) {
            String returnValue = result.getString(0);
            result.close();
            return returnValue;
        }

        result.close();
        return null;
    }


    public String[] getNoteTitleBody(String noteID){
        Cursor titleBody = mDb.rawQuery(String.format(
                "SELECT title, body FROM notes WHERE _id  = '%s'", noteID), null);
        if (titleBody == null)
            return null;
        if (titleBody.getCount() < 1){
            titleBody.close();
            return null;
        }
        titleBody.moveToFirst();
        String[] returnValue = new String[2];

        returnValue[0] = titleBody.getString(0);
        returnValue[1] = titleBody.getString(1);
        titleBody.close();
        return returnValue;
    }

    public Cursor getNoteIDTitleBody(String chatID){
        return mDb.rawQuery(String.format("SELECT _id, title, body FROM notes WHERE chatID = '%s'",
                chatID), null);
    }

    public List<String> getTagsForChat(String chatID){
        Cursor allChatTags = mDb.rawQuery(String.format("SELECT tags FROM messages WHERE chatID = " +
                "'%s' AND" + " tags IS NOT NULL", chatID), null);

        if(allChatTags == null)
            return null;
        if(allChatTags.getCount() < 1) {
            allChatTags.close();
            return null;
        }
        allChatTags.moveToFirst();
        Set<String> rawTags = new HashSet<>();
        String tag;
        do{
            tag = allChatTags.getString(0);
            if(!rawTags.contains(tag))
                rawTags.add(tag);
        }while(allChatTags.moveToNext());

        List<String> tags = new LinkedList<>();
        for(String tagArray: rawTags){
            for (String t: tagArray.replaceAll("\\[", "").replaceAll("\\]", "").split(","))
                tags.add(t);
        }
        return tags;
    }
    public void importEvents(Map message){
        mDb.execSQL("DROP TABLE IF EXISTS events");
        mDb.execSQL(DATABASE_CREATE_EVENTS);
        Object[] events = (Object[])message.get(MessageBundle.EVENTS);
        for(Object event : events){
            Map eventMap = (Map) event;
            insertEvent(eventMap);
        }
    }

    public void insertEvent(Map event){
        String id = (String) event.get(MessageBundle.EVENT_ID);
        String eventName = (String) event.get(MessageBundle.EVENT_DATETIME);
        String chatID = (String) event.get(MessageBundle.CHATROOMID);
        String eventDate = (String) event.get(MessageBundle.EVENT_DATETIME);
        String[] votes = (String[]) event.get(MessageBundle.VOTES);

        ContentValues eventValues = new ContentValues();
        eventValues.put(ROWID, id);
        eventValues.put(CHATID, chatID);
        eventValues.put(EVENT_NAME, eventName);
        eventValues.put(VOTES, Arrays.toString(votes));
        eventValues.put(EVENT_DATE, eventDate);
//        eventValues.put(HAS_VOTED, 0);
        mDb.insert(EVENTS, null, eventValues);
    }

/*    public void castVote (String eventID){
        mDb.execSQL(String.format("UPDATE events SET hasVoted=1 WHERE _id='%s'",
                eventID));
    }*/

    /*public Cursor getUnvotedEventIDNameDate (String chatID){
        return mDb.rawQuery(String.format("SELECT _id, eventName, " +
                "eventDate FROM events WHERE chatID='%s'",  chatID),null);
    }*/

    public String getEventName(String eventID){
        Cursor result = mDb.rawQuery(String.format("SELECT eventName FROM " +
                "events WHERE _id ='%s'",eventID), null);
        if(result == null)
            return null;
        if(result.getCount() < 1){
            result.close();
            return null;
        }
        result.moveToFirst();
        String eventName = result.getString(0);
        result.close();
        return eventName;
    }
}
