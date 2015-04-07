package obsolete;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;

public class JsonTest {
//	public static final String hostname = "localhost"; //= "128.199.73.51";
	public static final String hostname = "128.199.73.51";
	public static final int hostport = 8091;
	public static final String ownID = "12345";
	
	public static void main(String[] args) throws IOException {
		Socket client = new Socket(hostname, hostport);
		
		
		JsonWriter jOut = new JsonWriter(client.getOutputStream());
		JsonReader jIn = new JsonReader(client.getInputStream(), true);
		
		Object obj = JsonReader.jsonToJava("{\"username\":\"sy\",\"session_token\":\"asdfadsf\",\"from_phone_number\":82238071,\"type\":{\"name\":\"AUTH\"}}");

		jOut.write(obj);
		jOut.flush();

		Map map = (Map) jIn.readObject();
		System.out.println(map);
		
	}
}
