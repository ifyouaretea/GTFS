package serverUtils;

import java.io.IOException;
import java.net.Socket;

import serverUtils.MessageBundle.messageType;

import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;

public class SendMessageThread extends Thread{
	
	public static final String hostname = "128.199.73.51";
	public static final int hostport = 8091;
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
				
				serverOut.close();
				serverIn.close();
				server.close();
				break;
			}
			catch(IOException e){
				e.printStackTrace();
			}	
		}

	}
}
