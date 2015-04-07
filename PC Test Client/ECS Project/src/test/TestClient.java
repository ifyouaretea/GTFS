package test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

import serverUtils.MessageBundle;

public class TestClient {

	public static void main(String[] args) throws IOException{
		final String ownID = "12345";

		NetworkThread networkThread;

		networkThread = new NetworkThread();
		networkThread.start();

		MessageBundle createBundle = new MessageBundle("81572260", "asdsd",
				MessageBundle.messageType.CREATE_ROOM);
		createBundle.putUsers(new String[] {"81572260", "82238071"});
		createBundle.putChatroomName("testtest");
		
		System.out.println(createBundle.getMessage().toString());
		
		System.out.println("Press enter to send");
		
		
		System.in.read();
		

		networkThread.addMessageToQueue(createBundle.getMessage());
		
		MessageBundle textBundle = new MessageBundle("81572260", "asdsd",
				MessageBundle.messageType.TEXT);

		textBundle.putMessage("HI BRAH");
		textBundle.putToPhoneNumber("82238071");
		textBundle.putChatroomID("BF43F36CA3D484D186BE9178A9674E14F470AC56");

		networkThread.addMessageToQueue(textBundle.getMessage());

	}
}
