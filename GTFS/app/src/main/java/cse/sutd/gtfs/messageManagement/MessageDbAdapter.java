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

import cse.sutd.gtfs.serverUtils.MessageBundle;


/**
 * Created by Glen on 02/04/2015.
 */
public class MessageDbAdapter {

    SQLiteDatabase mDb;
    DatabaseHelper mDbHelper;
    Context mContext;

    private static final String MESSAGES = "messages";
    private static final String CHATS= "chats";
    private static final String ROWID = "_id";
    private static final String CHATID = "chatID";
    private static final String TIMESTAMP = "timestamp";
    private static final String FROM_PHONE_NUMBER = "from_phone_number";
    private static final String PHONE_NUMBER = "phone_number";
    private static final String CHATNAME = "chatName";
    private static final String LAST_MESSAGE = "lastMessage";
    private static final String USERS = "users";
    private static final String NAME = "name";
    private static final String EXPIRY = "expiry";
    private static final String CONTACTS = "contacts";
    private static final String ISGROUP = "isGroup";
    private static final String BODY = "body";
    private static final String NOTE_CREATOR = "noteCreator";
    private static final String TITLE = "title";

    private static final String TAG = "MessageDbAdapter";

    public static final String DATABASE_CREATE_NOTES =
            "create table notes (_id text primary key, "
                    + "title text not null, " +
                    "body text not null, chatID text not null, noteCreator text not null);";

    private static MessageDbAdapter instance;
    private static class DatabaseHelper extends SQLiteOpenHelper {
        private static final String DATABASE_CREATE_MESSAGES =
                "create table messages (_id integer primary key autoincrement, "
                        + "chatID text not null, body text not null," +
                        "from_phone_number text not null, timestamp text not null);";

        private static final String DATABASE_CREATE_CHATS =
                "create table chats (_id text primary key, "
                        + "isGroup integer, chatName text, " +
                        "lastMessage integer not null, "+
                        "users string, expiry integer);";

        private static final String DATABASE_CREATE_CONTACTS =
                "create table contacts (_id text primary key, "
                        + "name text);";

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
        Log.d("path", mDb.getPath());
        String chatID = (String) message.get(MessageBundle.CHATROOMID);
        String timestamp = (String) message.get(MessageBundle.TIMESTAMP);
        String from_phone_number = (String) message.get(MessageBundle.FROM_PHONE_NUMBER);
        String body = (String) message.get(MessageBundle.MESSAGE);

        Log.d("Database store", message.toString());

        Cursor messageExists = mDb.rawQuery(String.format("SELECT _id FROM messages WHERE " +
                "chatID='%s' AND timestamp='%s' AND from_phone_number='%s' AND body='%s';"
                ,chatID, timestamp, from_phone_number, body), null);

        //check if a copy of the message is already in the database
        if (messageExists.getCount() != 0) {
            messageExists.close();
            return -1;
        }

        Cursor chatExists = mDb.rawQuery(String.format("SELECT _id FROM chats WHERE " +
                "_id='%s'", chatID), null);

        boolean isChatExists = chatExists.getCount() > 0;
        chatExists.close();

        //if chat doesn't exist, create a new entry in the chats table
        //NOTE: THIS SHOULD ONLY BE USED FOR INDIVIDUAL CHATS
        //new group chats must be handled by the invitation callback function
        //createGroupChat() and should be called appropriately when the message type
        //is evaluated
        ContentValues chatValues= new ContentValues();
        if(!isChatExists){
            chatValues.put(ROWID, chatID);
            chatValues.put(CHATNAME, from_phone_number);
            chatValues.put(LAST_MESSAGE, String.valueOf(System.currentTimeMillis()));
            Log.d("Chat Values", chatValues.toString());
            mDb.insert(CHATS, null, chatValues);
        }else{
            String updateSQL =
                    "UPDATE chats SET " + LAST_MESSAGE + " = '" +
                            String.valueOf(System.currentTimeMillis()) +
                            "' WHERE _id = '"+ chatID + "'";

            mDb.execSQL(updateSQL);
            //mDb.update(CHATS, chatValues, "_id =" + chatID, null);
        }

        ContentValues messageValues = new ContentValues();
        messageValues.put(CHATID, chatID);
        messageValues.put(TIMESTAMP, timestamp);
        messageValues.put(BODY, body);
        messageValues.put(FROM_PHONE_NUMBER, from_phone_number);
        Log.d("Message Values", messageValues.toString());
        //if inserting fails, return -1
        if (mDb.insert(MESSAGES, null, messageValues) < 0)
            return -1;

        //if inserting succeeds, return 2 if the chat is new, 1 if it's not new
        return isChatExists ? 1 : 2;
    }

    public Cursor getChatMessages(String chatID){
        return mDb.rawQuery(String.format("SELECT from_phone_number, body, " +
                "timestamp FROM messages WHERE chatID ='%s' ORDER BY _id", chatID), null);
    }

    public Cursor getChats(){
        return mDb.rawQuery("SELECT _id, chatName, lastMessage, isGroup FROM " +
                "chats", null);
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
        Integer isGroup = null;

        if((message.get(MessageBundle.TYPE)).equals
                (MessageBundle.messageType.ROOM_INVITATION.toString()))
            isGroup = 1;
        else if((message.get(MessageBundle.TYPE)).equals
                (MessageBundle.messageType.SINGLE_ROOM_INVITATION.toString()))
            isGroup = 0;

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
        Log.d("Contact creation", phoneNum);
        ContentValues chatValues = new ContentValues();
        chatValues.put(ROWID, phoneNum);
        chatValues.put(NAME, contactName);
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

    public String getChatIDForUser(String userID){
        Cursor result = mDb.rawQuery("SELECT _id FROM chats WHERE isGroup = 1 " +
                "AND users LIKE '%" + userID + "%'", null);
        if(result.getCount() != 1) {
            result.close();
            return null;
        }
        result.moveToFirst();
        String returnValue = result.getString(0);
        result.close();
        return returnValue;
    }

    public void importChatrooms(Map message){
        Object[] chatrooms = (Object[])message.get(MessageBundle.CHATROOMS);
        for(Object chatroomO : chatrooms){
            Map chatroom = (Map) chatroomO;

            String chatID = (String) chatroom.get(MessageBundle.CHATROOMID);
            String chatName = (String) chatroom.get(MessageBundle.CHATROOM_NAME);
            String users = Arrays.toString((Object[])chatroom.get(USERS));

            ContentValues chatValues = new ContentValues();
            chatValues.put(ROWID, chatID);
            chatValues.put(CHATNAME, chatName);
            chatValues.put(USERS, users);
            chatValues.put(LAST_MESSAGE, chatID);
            chatValues.put(ISGROUP, 1);
            mDb.insert(CHATS, null, chatValues);
        }
    }

    public void importUsers(Map message){
        Object[] users =  (Object[])message.get(MessageBundle.USERS);
        if (users == null)
            return;
        Cursor phones = mContext.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        phones.moveToFirst();
        HashMap<String,String> phoneName = new HashMap<String,String>();
        while (phones.moveToNext()){
            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)).trim();
            phoneNumber = phoneNumber.replaceAll("\\s","");
            phoneNumber = phoneNumber.replaceAll(" ","");
            phoneNumber = phoneNumber.replace("+65", "");
            phoneNumber = phoneNumber.replaceAll("\\D", "");
            String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)).trim();
            if (phoneNumber.length() >= 8) {
                phoneName.put(phoneNumber,name);
            }
        }
        phones.close();
        for(Object  user : users){
            String phone = (String) ((Map)user).get(PHONE_NUMBER);
            long pass = putContact(phone, phoneName.get(phone));
            Log.d("pass",String.valueOf(pass));
            Log.d("Number/Name",phone+","+phoneName.get(phone));
        }
    }

    public void importNotes(Map message){
        mDb.execSQL("DROP TABLE IF EXISTS notes");
        mDb.execSQL(DATABASE_CREATE_NOTES);
        Object[] notes = (Object[]) message.get(MessageBundle.NOTES);
        if (notes == null)
            return;
        for(Object note : notes)
            createNote((Map) note);
    }

    public long createNote (Map message){
        String chatID = (String) message.get(MessageBundle.CHATROOMID);
        String noteID = (String) message.get(MessageBundle.NOTE_ID);
        String note_title = (String) message.get(MessageBundle.NOTE_TITLE);
        String note_text = (String) message.get(MessageBundle.NOTE_TEXT);
        String note_creator = (String) message.get(MessageBundle.NOTE_CREATOR);

        ContentValues chatValues = new ContentValues();
        chatValues.put(ROWID, noteID);
        chatValues.put(CHATID, chatID);
        chatValues.put(TITLE, note_title);
        chatValues.put(BODY, note_text);
        chatValues.put(NOTE_CREATOR, note_creator);

        return mDb.insert(CHATS, null, chatValues);
    }


}
