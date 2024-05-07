package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import domain.Message;
import domain.Message.Action;
import domain.User;

public class ServerThread extends Thread {
	
	private static List<User> clientsList = new ArrayList<>();
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
		clientsList.add(new User(message.getSender(), socket));
	}
	
	public void disconnect(Message message) {
		clientsList.stream()
		.filter(user -> user.username().equals(message.getSender()))
		.findFirst()
		.ifPresent(clientsList::remove);
	}
	
	public void sendMessageToAll(Message message) throws IOException {
		for(User client : clientsList) {
			ObjectOutputStream output = new ObjectOutputStream(client.socket().getOutputStream());
			output.writeObject(message);
		}
	}
	
	public void sendMessageToOne(Message message) throws IOException {
		for(User client : clientsList) {
			if(message.getRecipient().equals(client.username())) {
				ObjectOutputStream recipientOutput = new ObjectOutputStream(client.socket().getOutputStream());
				recipientOutput.writeObject(message);
			}
		}
		if(!message.getSender().equals(message.getRecipient())) {
			Optional<User> client = clientsList.stream().filter(user -> user.username().equals(message.getSender())).findFirst();
			Integer index = clientsList.lastIndexOf(client.get());
			ObjectOutputStream senderOutput = new ObjectOutputStream(clientsList.get(index).socket().getOutputStream());
			senderOutput.writeObject(message);
		}
	}
	
	public void sendOnlineUsers() throws IOException {
		ArrayList<String> onlineUsers = new ArrayList<>();
		for(User client : clientsList) {
			onlineUsers.add(client.username());
		}
		
		Message message = new Message();
		message.setAction(Action.USERS_ONLINE);
		message.setOnlineUsers(onlineUsers);
		
		for(User client : clientsList) {
			ObjectOutputStream output = new ObjectOutputStream(client.socket().getOutputStream());
			output.writeObject(message);
		}
	}
}
