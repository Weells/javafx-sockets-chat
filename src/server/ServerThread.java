package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import domain.Message;
import domain.Message.Action;

public class ServerThread extends Thread {
	
	private static Map<String, Socket> clientsMap = new HashMap<>();
	private Socket socket;
	
	public ServerThread(Socket s) {
		this.socket = s;
	}
	
	@Override
	public void run() {
		boolean exit = false;
		try {
			while(!exit) {
				ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
				Message message = (Message) input.readObject();
				Action action = message.getAction();
				message.setTimeStamp(new Date());
				
				switch(action) {
				case CONNECT:
					connect(message);
					sendMessageToAll(message);
					sendOnlineUsers();
					break;
				case DISCONNECT:
					disconnect(message);
					sendMessageToAll(message);
					sendOnlineUsers();
					break;
				case SEND:
					sendMessageToAll(message);
					break;
				case SEND_ONE:
					sendMessageToOne(message);
					break;
				default:
					break;
				}
			}
		} catch(IOException | ClassNotFoundException e) {
			Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, e);
		}
	}
	
	public void connect(Message message) {
		clientsMap.put(message.getSender(), socket);
	}
	
	public void disconnect(Message message) {
		clientsMap.remove(message.getSender());
	}
	
	public void sendMessageToAll(Message message) throws IOException {
		for(Map.Entry<String, Socket> client : clientsMap.entrySet()) {
			ObjectOutputStream output = new ObjectOutputStream(client.getValue().getOutputStream());
			output.writeObject(message);
		}
	}
	
	public void sendMessageToOne(Message message) throws IOException {
		for(Map.Entry<String, Socket> client : clientsMap.entrySet()) {
			if(message.getRecipient().equals(client.getKey())) {
				ObjectOutputStream recipientOutput = new ObjectOutputStream(client.getValue().getOutputStream());
				recipientOutput.writeObject(message);
			}
		}
		if(!message.getSender().equals(message.getRecipient())) {
			ObjectOutputStream senderOutput = new ObjectOutputStream(clientsMap.get(message.getSender()).getOutputStream());
			senderOutput.writeObject(message);
		}
	}
	
	public void sendOnlineUsers() throws IOException {
		ArrayList<String> onlineUsers = new ArrayList<>();
		for(Map.Entry<String, Socket> client : clientsMap.entrySet()) {
			onlineUsers.add(client.getKey());
		}
		
		Message message = new Message();
		message.setAction(Action.USERS_ONLINE);
		message.setOnlineUsers(onlineUsers);
		
		for(Map.Entry<String, Socket> client : clientsMap.entrySet()) {
			ObjectOutputStream output = new ObjectOutputStream(client.getValue().getOutputStream());
			output.writeObject(message);
		}
	}
}
