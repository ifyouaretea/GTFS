package chat;

import java.io.IOException;
import java.net.Socket;

import chat.MessageBundle.messageType;

import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;

public class SendMessageThread extends Thread{
	
	private MessageBundle message;
	
	public SendMessageThread(MessageBundle message){
		this.message = message;
	}
	
	public void run(){
		
		while(true){
			try{
				Socket server = new Socket(hostname, hostport);
				server.setSoTimeout(10000); //attempts to send to server every 10 seconds
				
				JsonWriter serverOut = new JsonWriter(server.getOutputStream());
				JsonReader serverIn = new JsonReader(server.getInputStream());
	
				serverOut.write(message);
				serverOut.flush();
				
				if(((MessageBundle) serverIn.readObject()).getType() == messageType.SERVER_RECEIVED){
					serverOut.close();
					serverIn.close();
					server.close();
					break;
				}}
			catch(IOException e){
				e.printStackTrace();
			}	
		}

	}
}
