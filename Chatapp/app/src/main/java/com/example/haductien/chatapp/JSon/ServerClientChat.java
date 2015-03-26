package com.example.haductien.chatapp.JSon;

/**
 * Created by Francisco Furtado on 26/03/2015.
 */
import com.cedarsoftware.util.io.JsonWriter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerClientChat {
    public static final String hostname = "localhost"; //= "128.199.73.51";
    public static final int hostport = 8091;
    public static final String ownID = "12345";

    //TODO: remove main method
    public static void main(String[] args) throws IOException{
        ServerSocket server = new ServerSocket(hostport);
        Socket client = new Socket(hostname, hostport);

        Socket serverSideClient = server.accept();

        JsonWriter jOut = new JsonWriter(client.getOutputStream());
//		JsonReader jIn = new JsonReader(serverSideClient.getInputStream());


        jOut.write(new MessageBundle(ownID, "hi", MessageBundle.messageType.TEXT));
        jOut.flush();

        BufferedReader in = new BufferedReader(new InputStreamReader(serverSideClient.getInputStream()));

        int line = -1;
        while((line = in.read()) != -1)
            System.out.print((char) line);

//		System.out.println(((MessageBundle) jIn.readObject()).getType());
    }
}
