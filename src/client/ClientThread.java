package client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.List;

import domain.Message;
import domain.Message.Action;
import javafx.application.Platform;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;

public class ClientThread extends Thread {

	private Socket socket;
	private TextArea textArea;
	private String sender;
	private ChoiceBox<String> cbOnlineUsers;
	boolean exit = false;
	String messageStatus = "";
	
	public ClientThread(Socket socket, TextArea textArea, String sender, ChoiceBox<String> cbOnlineUsers) {
		this.socket = socket;
		this.textArea = textArea;
		this.sender = sender;
		this.cbOnlineUsers = cbOnlineUsers;
	}
	
	@Override
	public void run() {
		try {
			while(!exit) {
				ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
				Message message = (Message) input.readObject();
				Action action = message.getAction();
				
				switch(action) {
					case CONNECT:
						connect(message);
						break;
					case DISCONNECT:
						disconnect(message);
						break;
					case SEND:
						receiveMessage(message);
						break;
					case SEND_ONE:
						receiveMessage(message);
						break;
					case USERS_ONLINE:
						updateUsers(message);
						break;
					default:
						break;
				}
			}
		} catch (IOException | ClassNotFoundException e) {
			
		}
	}
	
	public void connect(Message message) {
        Platform.runLater(() -> this.textArea.appendText(message.getTimeStamp()
        		+ " "
        		+ message.getSender()
        		+ " "
        		+ message.getText()
        		+ "\n"));
    }

    public void disconnect(Message message) throws IOException {
    	if(cbOnlineUsers.getValue().equals(message.getSender()))
    		Platform.runLater(() -> { cbOnlineUsers.setValue("[Todos]"); });
    	
        Platform.runLater(() -> this.textArea.appendText(message.getTimeStamp()
        		+ " "
        		+ message.getSender()
        		+ " "
        		+ message.getText()
        		+ "\n"));
        if (message.getSender().equals(this.sender)) {
            this.socket.close();
            this.exit = true;
        }
    }

    public void receiveMessage(Message message) throws IOException {
    	if(message.getAction() == Message.Action.SEND_ONE)
    		messageStatus = String.format(" (mensagem para %s)", message.getRecipient());
        Platform.runLater(() -> this.textArea.appendText(message.getTimeStamp()
        		+ " "
        		+ message.getSender()
        		+ messageStatus
        		+ ": "
        		+ message.getText() +
        		"\n"));
        
        Platform.runLater(() -> messageStatus="");
    }

    public void updateUsers(Message message) {
        List<String> onlineUsers = message.getOnlineUsers();

        Platform.runLater(() -> {
        	for(Object user : cbOnlineUsers.getItems().toArray()) {
        		if(!onlineUsers.contains(user) && !user.equals("[Todos]")) 
        			cbOnlineUsers.getItems().remove((String) user);
        	}
        	for(String user : onlineUsers) {
        		if(!cbOnlineUsers.getItems().contains(user) && !user.equals(this.sender))
        			cbOnlineUsers.getItems().add(user);
        	}
        });

    }
}
