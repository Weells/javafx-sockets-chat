package client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.List;

import domain.Message;
import domain.Message.Action;
import domain.UserFile;
import domain.UserFile.Scale;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

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
			while (!exit) {
				ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
				Message message = (Message) input.readObject();
				Action action = message.getAction();

				switch (action) {
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
		String messageText = message.getTimeStamp() + " " + message.getSender() + messageStatus + " "
				+ message.getText() + "\n";
		Platform.runLater(() -> this.textArea.getChildren().add(createMessageNode(messageText)));
	}

	public void disconnect(Message message) throws IOException {
		if (cbOnlineUsers.getValue().equals(message.getSender()))
			Platform.runLater(() -> {
				cbOnlineUsers.setValue("[Todos]");
			});

		String messageText = message.getTimeStamp() + " " + message.getSender() + messageStatus + " "
				+ message.getText() + "\n";
		Platform.runLater(() -> this.textArea.getChildren().add(createMessageNode(messageText)));
		if (message.getSender().equals(this.sender)) {
			this.socket.close();
			this.exit = true;
		}
	}

	public void receiveMessage(Message message) throws IOException {
		if (message.getAction() == Message.Action.SEND_ONE)
			messageStatus = String.format(" (mensagem para %s)", message.getRecipient());
		String messageText = message.getTimeStamp() + " " + message.getSender() + messageStatus + ": "
				+ message.getText() + "\n";
		Platform.runLater(() -> {
			this.textArea.getChildren().add(createMessageNode(messageText));
			messageStatus = "";
			if (message.getUserFile() != null) {
				createImageNode(message.getUserFile());
			}
		});
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

		return msg;
	}

	public void createImageNode(UserFile userFile){
		Platform.runLater(() -> {
			int width = 0;
			int height = 0;
			try {
			ImageView imgView = new ImageView();
			String fileName = userFile.getName();

			File output = new File(System.getProperty("user.dir") +"/cache/images/"+ fileName);
			FileOutputStream fos = new FileOutputStream(output.getAbsolutePath(), false);
			fos.write(userFile.getBytes());
			fos.flush();
			fos.close();

			Image image = new Image(new FileInputStream(output));
			if(image.getHeight() > image.getWidth()) {
				width = 200;
				height = 230;
			} else {
				width = 230;
				height = 200;
			}
			imgView.setImage(image);
			imgView.setFitWidth(width);
			imgView.setFitHeight(height);
			imgView.setOnMouseClicked(new EventHandler<Event>() {
				@Override
				public void handle(Event arg0) {
					ImageView view = new ImageView();
					view.setImage(image);
					Group root = new Group(view);
					Stage stage = new Stage();
					Scene scene = new Scene(root);
					
					Scale scale = UserFile.resizeScale(image);
					
					view.setFitWidth(scale.width());
					view.setFitHeight(scale.height());
					
					stage.setScene(scene);
					stage.setResizable(false);
					stage.setTitle("Imagem enviada por " + sender);
					stage.show();
				}
			});
			
			textArea.getChildren().add(imgView);
			} catch(IOException e) {
				e.printStackTrace();
			}
		});

	}
}
