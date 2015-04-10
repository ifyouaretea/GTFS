package cse.sutd.gtfs.messageManagement;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.sql.SQLException;
import java.util.Arrays;
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
    private static final String BODY= "body";
    private static final String CHATNAME = "chatName";
    private static final String LAST_MESSAGE = "lastMessage";
    private static final String USERS = "users";
    private static final String TAG = "MessageDbAdapter";

    private static MessageDbAdapter instance;
    private static class DatabaseHelper extends SQLiteOpenHelper {
        private static final String DATABASE_CREATE_MESSAGES =
                "create table messages (_id integer primary key autoincrement, "
                        + "chatID text not null, body text not null," +
                        "from_phone_number text not null, timestamp text not null);";

        private static final String DATABASE_CREATE_CHATS =
                "create table chats (_id text primary key, "
                        + "chatName text not null, lastMessage integer not null, "
                        + "users string not null, expiry integer);";

        private static final String DATABASE_NAME = "data";

        private static final int DATABASE_VERSION = 2;

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE_MESSAGES);
            db.execSQL(DATABASE_CREATE_CHATS);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS notes");
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
        if (messageExists.getCount() != 0)
            return -1;

        Cursor chatExists = mDb.rawQuery(String.format("SELECT _id FROM chats WHERE " +
                "_id='%s'", chatID), null);

        boolean isChatExists = chatExists.getCount() > 0;

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
                "timestamp FROM messages WHERE chatID ='%s'", chatID), null);
    }

    public Cursor getChats(){
        return mDb.rawQuery(String.format("SELECT _id, chatName, lastMessage FROM " +
                "chats"), null);
    }

    public long createGroupChat(Map message){
        Log.d("Chat creation", message.toString());
        String chatID = (String) message.get(CHATID);
        String chatName = (String) message.get(CHATNAME);

        ContentValues chatValues = new ContentValues();
        chatValues.put(ROWID, chatID);
        chatValues.put(CHATNAME, chatName);
        chatValues.put(USERS, Arrays.toString((String[])message.get(USERS)));
        return mDb.insert(CHATS, null, chatValues);
    }

    public long deleteGroupChat(String chatID){
        return mDb.delete(CHATS, ROWID + "=" + chatID, null);
    }
}
