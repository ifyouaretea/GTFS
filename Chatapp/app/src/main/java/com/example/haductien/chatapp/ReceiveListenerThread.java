package com.example.haductien.chatapp;

/**
 * Created by Francisco Furtado on 24/03/2015.
 */


import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;
import com.example.haductien.chatapp.MessageBundle.messageType;

import java.io.IOException;
import java.net.Socket;

//TODO: put into receiveThread class
/**
 * Listens continuously for a connection from the server.
 * @throws IOException
 */
public class ReceiveListenerThread extends Thread{
    public static final String hostname = "128.199.73.51";
    public static final int hostport = 8091;
    public static final String ownID = MyApplication.PROFILE_ID;

    public static MessageBundle RECEIVED = new MessageBundle(ownID, "", messageType.CLIENT_RECEIVED);

    public void run(){
        while(true){
            try{
                Socket serverConnection = new Socket(hostname, hostport);

                JsonReader serverIn = new JsonReader(serverConnection.getInputStream());
                MessageBundle message = (MessageBundle) serverIn.readObject();

                JsonWriter serverOut = new JsonWriter(serverConnection.getOutputStream());
                serverOut.write(ReceiveListenerThread.RECEIVED);

                serverConnection.close();
                serverIn.close();
                serverOut.close();

                switch(message.getType()){
                    //TODO: implement handlers for the different message types
                    case CLIENT_RECEIVED:
                        break;
                    case EDIT_NOTE:
                        break;
                    case FETCH_NOTE:
                        break;
                    case INVITE:
                        break;
                    case NEW_MESSAGE:
                        break;
                    case TEXT:
                        break;
                    case TYPING:
                        break;
                    default:
                        break;

                }

            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }
}

