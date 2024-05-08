package client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.List;

import domain.Message;
import domain.Message.Action;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.VBox;

public class ClientThread extends Thread {

	private Socket socket;
	private VBox textArea;
	private String sender;
	private ChoiceBox<String> cbOnlineUsers;
	String messageStatus = "";
	boolean exit = false;
	
	public ClientThread(Socket socket, VBox textArea, String sender, ChoiceBox<String> cbOnlineUsers) {
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
			e.printStackTrace();
		}
	}
	
	public void connect(Message message) {
		String messageText = message.getTimeStamp()
        		+ " "
        		+ message.getSender()
        		+ messageStatus
        		+ " "
        		+ message.getText() +
        		"\n";
        Platform.runLater(() -> this.textArea.getChildren().add(createMessageNode(messageText)));
    }

    public void disconnect(Message message) throws IOException {
    	if(cbOnlineUsers.getValue().equals(message.getSender()))
    		Platform.runLater(() -> { cbOnlineUsers.setValue("[Todos]"); });
    	
    	String messageText = message.getTimeStamp()
        		+ " "
        		+ message.getSender()
        		+ messageStatus
        		+ " "
        		+ message.getText() +
        		"\n";
        Platform.runLater(() -> this.textArea.getChildren().add(createMessageNode(messageText)));
        if (message.getSender().equals(this.sender)) {
            this.socket.close();
            this.exit = true;
        }
    }

    public void receiveMessage(Message message) throws IOException {
    	if(message.getAction() == Message.Action.SEND_ONE)
    		messageStatus = String.format(" (mensagem para %s)", message.getRecipient());
    		String messageText = message.getTimeStamp()
        		+ " "
        		+ message.getSender()
        		+ messageStatus
        		+ ": "
        		+ message.getText() +
        		"\n";
        Platform.runLater(() -> this.textArea.getChildren().add(createMessageNode(messageText)));
        
        Platform.runLater(() -> messageStatus="");
    }

    public void updateUsers(Message message) {
        List<String> onlineUsers = message.getOnlineUsers();
        
		Platform.runLater(() -> {
			onlineUsers.stream().filter(user -> user.equals(sender)).findFirst().map(onlineUsers::remove);
			cbOnlineUsers.getItems().clear();
			cbOnlineUsers.getItems().add("[Todos]");
			cbOnlineUsers.setValue("[Todos]");
			cbOnlineUsers.getItems().addAll(onlineUsers);
		});
    }
    
    public Button createMessageNode(String text) {
    	Button msg = new Button();
    	msg.setText(text);
    	msg.setStyle("-fx-background-color: transparent;"
    			+ "-fx-border-width: 0.5px;"
    			+ "-fx-border-color: black;"
    			+ "-fx-border-radius: 5px");
    	msg.setWrapText(true);
    	msg.setMaxSize(310, Button.USE_PREF_SIZE);
    	//msg.setBlendMode(BlendMode.EXCLUSION);
    	
    	return msg;
    }
}
