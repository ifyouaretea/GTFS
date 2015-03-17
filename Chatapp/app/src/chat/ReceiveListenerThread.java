package chat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import chat.MessageBundle.messageType;

import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;

//TODO: put into receiveThread class
/**
 * Listens continuously for a connection from the server.
 * @throws IOException
 */
public class ReceiveListenerThread extends Thread{
	public static final String hostname = "128.199.73.51";
	public static final int hostport = 8091;
	public static final String ownID = "12345";
	
	public static MessageBundle RECEIVED = new MessageBundle(ownID, "", messageType.CLIENT_RECEIVED);
	
	public void run(){
		while(true){
			try{
				Socket serverConnection = new Socket(hostname, hostport);

				JsonReader serverIn = new JsonReader(serverConnection.getInputStream());
				MessageBundle message = (MessageBundle) serverIn.readObject();
				
				JsonWriter serverOut = new JsonWriter(serverConnection.getOutputStream());
				serverOut.write(ReceiveListenerThread.RECEIVED);
				
				if(((MessageBundle)serverIn.readObject()).getType() == messageType.SERVER_RECEIVED){
					switch(message.getType()){
					//TODO: implement handlers for the different message types
						case INVITE:
							break;
						case SERVER_RECEIVED:
							break;
						case TEXT:
							break;
						case TYPING:
							break;
						default:
							break;
					
					}
				}
				}catch (IOException e){
				e.printStackTrace();
			}
		}
	}
}

