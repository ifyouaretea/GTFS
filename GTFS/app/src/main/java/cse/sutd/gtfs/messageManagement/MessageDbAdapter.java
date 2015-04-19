package cse.sutd.gtfs.messageManagement;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.ContactsContract;
import android.util.Log;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import cse.sutd.gtfs.GTFSClient;
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
    public static final String ROWID = "_id";
    public static final String CHATID = "chatID";
    public static final String TIMESTAMP = "timestamp";
    public static final String FROM_PHONE_NUMBER = "from_phone_number";
    public static final String PHONE_NUMBER = "phone_number";
    public static final String CHATNAME = "chatName";
    public static final String LAST_MESSAGE = "lastMessage";
    public static final String READ = "read";
    public static final String ROOM_USERS = "room_users";
    public static final String USERS = "users";
    public static final String NAME = "name";
    public static final String EXPIRY = "expiry";
    public static final String CONTACTS = "contacts";
    public static final String ISGROUP = "isGroup";
    public static final String BODY = "body";
    public static final String NOTE_CREATOR = "noteCreator";
    public static final String TITLE = "title";
    public static final String NOTES = "notes";

    private static final String TAG = "MessageDbAdapter";

    public static final String DATABASE_CREATE_NOTES =
            "create table notes (_id text primary key, "
                    + "title text not null, " +
                    "body text not null, chatID text not null, noteCreator text);";

    private static MessageDbAdapter instance;
    private static class DatabaseHelper extends SQLiteOpenHelper {
        private static final String DATABASE_CREATE_MESSAGES =
                "create table messages (_id integer primary key, "
                        + "chatID text not null, body text not null," +
                        "from_phone_number text not null, timestamp text not null, " +
                        "read integer not null);";

        private static final String DATABASE_CREATE_CHATS =
                "create table chats (_id text primary key, "
                        + "isGroup integer not null, chatName text, " +
                        "lastMessage integer not null, "+
                        "users string, expiry integer);";

        private static final String DATABASE_CREATE_CONTACTS =
                "create table contacts (_id text primary key, "
                        + "name text not null, chatID text);";

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
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS notes");
            db.execSQL("DROP TABLE IF EXISTS chats");
            db.execSQL("DROP TABLE IF EXISTS contacts");
            db.execSQL("DROP TABLE IF EXISTS messages");
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
     * @return -1 if not inserted, 1 if inserted correctly,
     * 2 if the chat been inserted has not existed previously
     */
    public int storeMessage(Map message){

        String chatID = (String) message.get(MessageBundle.CHATROOMID);
        String messageID = (String) message.get(MessageBundle.MESSAGE_ID);
        String timestamp = (String) message.get(MessageBundle.TIMESTAMP);
        String from_phone_number = (String) message.get(MessageBundle.FROM_PHONE_NUMBER);
        String body = (String) message.get(MessageBundle.MESSAGE);
        body = body.replaceAll("'", "''");
        Log.d("Database store", message.toString());

        Cursor messageExists = mDb.rawQuery(String.format("SELECT _id FROM messages WHERE " +
                "chatID='%s' AND timestamp='%s' AND from_phone_number='%s' AND body='%s';"
                ,chatID, timestamp, from_phone_number, body), null);

        //check if a copy of the message is already in the database
        if (messageExists.getCount() != 0) {
            messageExists.close();
            return -1;
        }

        boolean chatCreated = createSingleChat(message);
        if(!chatCreated) {
            String updateSQL =
                    "UPDATE chats SET " + LAST_MESSAGE + " = '" +
                            messageID +
                            "' WHERE _id = '" + chatID + "'";

            mDb.execSQL(updateSQL);
        }

        ContentValues messageValues = new ContentValues();
        messageValues.put(CHATID, chatID);
        messageValues.put(ROWID, messageID);
        messageValues.put(TIMESTAMP, timestamp);
        messageValues.put(BODY, body);
        messageValues.put(FROM_PHONE_NUMBER, from_phone_number);

        if(from_phone_number.equals(((GTFSClient)mContext.getApplicationContext()).getID()))
            messageValues.put(READ, 1);
        else
            messageValues.put(READ, 0);

        //if inserting fails, return -1
        if (mDb.insert(MESSAGES, null, messageValues) < 0)
            return -1;

        //if inserting succeeds, return 2 if the chat is new, 1 if it's not new
        return chatCreated ? 2 : 1;
    }

    public boolean createSingleChat(Map message){
        String chatID = (String) message.get(MessageBundle.CHATROOMID);
        String from_phone_number = (String) message.get(MessageBundle.FROM_PHONE_NUMBER);

        Cursor chatExists = mDb.rawQuery(String.format("SELECT _id FROM chats WHERE " +
                "_id='%s'", chatID), null);

        boolean isChatExists = chatExists.getCount() > 0;
        chatExists.close();

        if(isChatExists)
            return false;

        ContentValues chatValues= new ContentValues();

        chatValues.put(ROWID, chatID);
        chatValues.put(CHATNAME, from_phone_number);
        chatValues.put(LAST_MESSAGE, chatID);
        chatValues.put(ISGROUP, 0);
        if (mDb.insert(CHATS, null, chatValues) <= 0)
            return false;

        Object[] users = (Object[]) message.get(ROOM_USERS);
        String ownID = ((GTFSClient) mContext).getID();
        String otherUser = null;

        for(Object user: users)
            if (!ownID.equals(user)) {
                otherUser = (String) user;
                break;
            }

        try {
            mDb.execSQL(String.format("UPDATE contacts SET chat='%s' WHERE _id='%s'",
                    chatID, otherUser));
            return true;
        }catch (Exception e){
            return false;
        }
    }

    public Cursor getChatMessages(String chatID){
        return mDb.rawQuery(String.format("SELECT from_phone_number, body, " +
                "timestamp FROM messages WHERE chatID ='%s' ORDER BY _id", chatID), null);
    }

    public Cursor getChats(){
        return mDb.rawQuery("SELECT _id, chatName, isGroup FROM " +
                "chats ORDER BY lastMessage DESC", null);
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
        String users = Arrays.toString((Object[])message.get(ROOM_USERS));
        Integer isGroup = null;

        isGroup = 1;

        int expiry = Integer.parseInt((String) message.get(MessageBundle.EXPIRY));

        ContentValues chatValues = new ContentValues();
        chatValues.put(ISGROUP, isGroup);
        chatValues.put(ROWID, chatID);
        chatValues.put(CHATNAME, chatName);
        chatValues.put(USERS, users);
        chatValues.put(LAST_MESSAGE, chatID);
        chatValues.put(EXPIRY, expiry);
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

    public long deleteGroupChat(String chatID){
        return mDb.delete(CHATS, ROWID + "=" + chatID, null);
    }

    /**
     *
     * @return an array of the IDs removed
     */
    public String[] deleteExpiredChats(){
        long currentTime = System.currentTimeMillis();
        Cursor expiredChats = mDb.rawQuery("SELECT _id FROM chats WHERE expiry <= "
                + currentTime, null);
        if(expiredChats == null)
            return null;
        if (expiredChats.getCount() == 0) {
            expiredChats.close();
            return null;
        }
        expiredChats.moveToFirst();

        String[] expiredArray = new String[expiredChats.getCount()];
        int count = 0;
        do{
            expiredArray[count++] = expiredChats.getString(0);
            deleteGroupChat(expiredChats.getString(0));
        }while(expiredChats.moveToNext());
        expiredChats.close();
        return expiredArray;
    }

    public String getChatroomName(String chatID){
        Cursor result = mDb.rawQuery(String.format("SELECT chatName FROM chats WHERE _id = '%s'",
                chatID), null);
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
            String users = Arrays.toString((Object[])chatroom.get(ROOM_USERS));
            int isGroup = (Boolean) chatroom.get("group") ? 1 : 0;

            ContentValues chatValues = new ContentValues();
            chatValues.put(ROWID, chatID);
            chatValues.put(CHATNAME, chatName);
            chatValues.put(USERS, users);
            chatValues.put(LAST_MESSAGE, chatID);

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
//            putContact(phone, phoneName.get(phone));
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
}
