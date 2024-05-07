package gui;

import java.io.IOException;
import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;

import gui.util.Constraints;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import server.Server.ConnectionResponse;

public class LoginViewController implements Initializable {

	@FXML
	private TextField txtServerIp;

	@FXML
	private TextField txtServerPort;

	@FXML
	private TextField txtUsername;

	@FXML
	private Button btnConnect;

	@FXML
	private Button btnCreateConnection;

	@FXML
	private Label labelError;

	private ChatViewController chatController;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		Constraints.setTextFieldFormat(txtServerIp);
		Constraints.setTextFieldInteger(txtServerPort);
	}

	public void onConnectionButtonAction(ActionEvent event) throws IOException {
		if (txtUsername.getText().isEmpty() || txtServerIp.getText().isEmpty() || txtServerPort.getText().isEmpty()) {
			labelError.setText("Preencha todos os campos");

		} else {
			String user = txtUsername.getText();
			String id = createId().toString();
			String userId = String.format("%s <%s>", user, id);
			String server = txtServerIp.getText();
			Integer ip = Integer.valueOf(txtServerPort.getText());

			FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/ChatView.fxml"));
			Parent window = (AnchorPane) loader.load();
			chatController = loader.<ChatViewController>getController();

			ConnectionResponse response = chatController.ConnectUser(userId, server, ip);
			Platform.runLater(() -> {
				switch (response) {
				case CONNECTED:
					chatController.getUser().setText(user);
					chatController.getUserId().setText("ID: " + id + "");

					Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
					Scene scene = new Scene(window);
					stage.hide();
					stage.setScene(scene);
					stage.setTitle(server + ":" + ip);
					stage.setOnHiding(e -> {
						chatController.onExit();
					});
					stage.show();
					break;
				case ERROR:
					labelError.setText("Erro ao conectar, tente novamente");
					break;
				default:
					break;

				}
			});

		}
	}

	public void onCreateConnectionAction(ActionEvent event) throws IOException {
		Parent root = FXMLLoader.load(getClass().getResource("/gui/CreateServerView.fxml"));
		Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
		stage.hide();
		Scene scene = new Scene(root);
		stage.setScene(scene);
		stage.show();
	}

	public Integer createId() {
		Random rand = new Random();
		return rand.nextInt(1000, 2000);
	}
}
