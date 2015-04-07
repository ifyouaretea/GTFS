package obsolete;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import serverUtils.MessageBundle;

import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;

public class DemoChat {
	
	public static final String hostname = "localhost";
	public static final int hostport = 4004;
	public static final String ownID = "12345";
	
	public static final String IDrequestHeader = "ID_Request";

	public static void main(String[] args) throws IOException{
		String otherID = "1289";
		ServerSocket serverSocket = new ServerSocket(hostport);
		
		Socket socket = new Socket(hostname, hostport);
		Socket client = serverSocket.accept();
		
		JsonWriter jOut = new JsonWriter(client.getOutputStream());
		JsonReader jIn = new JsonReader(socket.getInputStream());
		
		Object obj = JsonReader.jsonToJava("{\"username\":\"sy\",\"session_token\":\"asdfadsf\",\"from_phone_number\":82238071,\"type\":{\"name\":\"AUTH\"}}");
		jOut.write(obj);
		jOut.flush();
		
		PrintWriter pw = new PrintWriter(client.getOutputStream());
		pw.write("\n");
		pw.flush();
		
		System.out.println((MessageBundle) JsonReader. jIn.readObject());
		//BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		
		//System.out.println(reader.readLine());
	}
	
}
