package gui;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import client.ClientThread;
import domain.Message;
import domain.UserFile;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import server.Server.ConnectionResponse;

//Controller responsável pela interação do usuário com a interface do chat
public class ChatViewController implements Initializable {

	@FXML
	private Button btnFileUpload;

	@FXML
	private Button btnSendMessage;

	@FXML
	private Label labelUser;
	
	@FXML
	private Label labelUserId;

	@FXML
	private ChoiceBox<String> cbOnlineUsers;

	@FXML
	private TextField txtMessageField;

	@FXML
	private VBox vBoxArea;

	@FXML
	private Label labelFileName;
	
	@FXML
	private Label labelDeleteFile;

	private Socket socket;
	private String sender;
	private UserFile userFile;
	private JFileChooser fileChooser;
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		cbOnlineUsers.getItems().add("[Todos]");
		cbOnlineUsers.setValue("[Todos]");
	}

	//Realização da conexão do usuário com o servidor
	public ConnectionResponse ConnectUser(String sender, String server, Integer ip) throws IOException {
		this.sender = sender;

		try {
			socket = new Socket(server, ip);
			ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
			
			//Envia uma mensagem de conexão para o servidor e aos usuários
			Message message = new Message();
			message.setSender(sender);
			message.setText("entrou no chat!");
			message.setAction(Message.Action.CONNECT);

			//Cria uma instância para o usuário que irá manipular a interface e manter conexão com o servidor
			ClientThread client = new ClientThread(socket, vBoxArea, sender, cbOnlineUsers);
			client.setName("Thread Cliente " + sender);
			client.start();
			output.writeObject(message);
		
		} catch (IOException e) {
			return ConnectionResponse.ERROR;
		}
		return ConnectionResponse.CONNECTED;
	}

	//Método de envio de mensagens
	public void onSendMessage() {
		if(!this.txtMessageField.getText().isEmpty() || userFile != null) {
			try {
				Message message = new Message();
				message.setSender(this.sender);
				message.setText(this.txtMessageField.getText());
				message.setAction(Message.Action.SEND);

				//Se um usuário escolhe outro usuário na lista de visiblidade, a mensagem será privada
				if (cbOnlineUsers.getSelectionModel().getSelectedItem() != "[Todos]") {
					message.setAction(Message.Action.SEND_ONE);
					message.setRecipient((String) cbOnlineUsers.getSelectionModel().getSelectedItem());
				}
				if (userFile != null)
					message.setUserFile(userFile);

				// Saída de dados do usuário
				ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
				output.writeObject(message);

				this.txtMessageField.setText("");
				this.labelFileName.setText("");
				this.labelDeleteFile.setVisible(false);
				this.userFile = null;

			} catch (IOException e) {
				Logger.getLogger(ChatViewController.class.getName()).log(Level.SEVERE, null, e);
			}
		}
	}

	//Método para envio de mensagens pressionando enter
	public void sendMessageOnEnter(KeyEvent e) {
		if (e.getCode() == KeyCode.ENTER) {
			this.onSendMessage();
		}
	}

	//Método que exibe para os usuários quando alguém se desconecta
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

	//Método responsável por possibilitar a adição de imagens às mensagens
	public void uploadFile() throws IOException {
		fileChooser = new JFileChooser();
		
		//Define o recebimento somente de arquivos de imagem
		FileFilter filter = new FileNameExtensionFilter("Image files", ImageIO.getReaderFileSuffixes());
		fileChooser.addChoosableFileFilter(filter);
		fileChooser.setAcceptAllFileFilterUsed(false);
		
		fileChooser.setCurrentDirectory(new File("C://Users//" + System.getProperty("user.home") + "//Desktop//Documents"));
		int response = fileChooser.showSaveDialog(null);
		if (response == JFileChooser.APPROVE_OPTION) {
			File file = new File(fileChooser.getSelectedFile().getAbsolutePath());
			userFile = new UserFile(file);
			labelFileName.setText(file.getName());
			labelDeleteFile.setVisible(true);
		}
	}

	//Método que limpa o seletor de mídia
	public void deleteFile() {
		this.userFile = null;
		this.labelFileName.setText("");
		this.fileChooser.setCurrentDirectory(null);
		this.labelDeleteFile.setVisible(false);
	}
	
	public Label getUser() {
		return this.labelUser;
	}
	
	public Label getUserId() {
		return this.labelUserId;
	}

}
