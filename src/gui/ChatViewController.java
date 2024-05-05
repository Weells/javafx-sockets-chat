package gui;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFileChooser;

import client.ClientThread;
import domain.Message;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import server.Server.ConnectionResponse;

public class ChatViewController implements Initializable {

	@FXML
	private Button btnFileUpload;

	@FXML
	private Button btnSendMessage;

	@FXML
	private Label labelChat;

	@FXML
	private ChoiceBox<String> cbOnlineUsers;

	@FXML
	private TextField txtMessageField;

	@FXML
	private TextArea txtArea;

	@FXML
	private Label labelFileName;

	private Socket socket;
	private String sender;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		cbOnlineUsers.getItems().add("[Todos]");
		cbOnlineUsers.setValue("[Todos]");
	}

	public ConnectionResponse ConnectUser(String sender, String server, Integer ip) throws IOException {
		this.sender = sender;

		try {
			socket = new Socket(server, ip);
			ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());

			// Enviando a primeira mensagem informando conexão (apenas para passar o nome do
			// cliente)
			Message message = new Message();
			message.setSender(sender);
			message.setText("entrou no chat!");
			message.setAction(Message.Action.CONNECT);

			ClientThread client = new ClientThread(socket, txtArea, sender, cbOnlineUsers);
			client.setName("Thread Cliente " + sender);
			client.start();
			output.writeObject(message);

			if (this.cbOnlineUsers.getItems().contains(sender)) {
				throw new IllegalStateException();
			}

		} catch (IOException e) {
			return ConnectionResponse.ERROR;
		} catch (IllegalStateException e) {
			socket.close();
			return ConnectionResponse.USERNAME_IN_USE;
		}
		return ConnectionResponse.CONNECTED;
	}

	public void onSendMessage() {
		try {
			Message message = new Message();
			message.setSender(this.sender);
			message.setText(this.txtMessageField.getText());
			message.setAction(Message.Action.SEND);

			if (cbOnlineUsers.getSelectionModel().getSelectedItem() != "[Todos]") {
				message.setAction(Message.Action.SEND_ONE);
				message.setRecipient((String) cbOnlineUsers.getSelectionModel().getSelectedItem());
			}

			// Saída de Dados do Cliente
			ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
			output.writeObject(message);

			this.txtMessageField.setText("");

		} catch (IOException e) {
			Logger.getLogger(ChatViewController.class.getName()).log(Level.SEVERE, null, e);
		}
	}

	public void sendMessageOnEnter(KeyEvent e) {
		if (e.getCode() == KeyCode.ENTER) {
			this.onSendMessage();
		}
	}

	public void onExit() {
		try {
			Message message = new Message();
			message.setSender(sender);
			message.setText("saiu do chat!");
			message.setAction(Message.Action.DISCONNECT);

			ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
			output.writeObject(message);
		} catch (IOException e) {
			Logger.getLogger(ChatViewController.class.getName()).log(Level.SEVERE, null, e);
		}
	}

	public void uploadFile() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setCurrentDirectory(new File("C://Documents"));
		int response = fileChooser.showSaveDialog(null);
		if (response == JFileChooser.APPROVE_OPTION) {
			File file = new File(fileChooser.getSelectedFile().getAbsolutePath());
			labelFileName.setText(file.getName());
		}
	}

	public Label getUser() {
		return this.labelChat;
	}

	public ChoiceBox<String> getCbOnlineUsers() {
		return this.cbOnlineUsers;
	}

}
