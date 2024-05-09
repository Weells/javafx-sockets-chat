package gui;

import java.io.IOException;
import java.net.BindException;
import java.net.URL;
import java.util.ResourceBundle;

import gui.util.Constraints;
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
import javafx.stage.Stage;
import server.Server;

public class CreateServerViewController implements Initializable {

	@FXML
	private TextField txtServerIp;

	@FXML
	private TextField txtServerPort;

	@FXML
	private Button btnCreateServer;

	@FXML
	private Button btnLoginDialog;

	@FXML
	private Label labelError;

	@FXML
	private Label labelWarn1;
	@FXML
	private Label labelWarn2;
	@FXML
	private Label labelWarn3;
	@FXML
	private Label labelWarn4;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		Constraints.setTextFieldInteger(txtServerPort);
	}

	public void createConnection() {
		if (txtServerPort.getText().isEmpty()) {
			labelError.setText("");
		} else {
			try {
				int port = Integer.valueOf(txtServerPort.getText());
				Server server = new Server();
				server.initializeServer(port);
			} catch (BindException e) {
				labelError.setText("Erro ao criar servidor");
			} catch (IOException e) {
				labelError.setText("Esta porta já está em uso");
			}
		}
	}

	public void onBtnLoginDialog(ActionEvent event) throws IOException {
		Parent root = FXMLLoader.load(getClass().getResource("/gui/LoginView.fxml"));
		Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
		Scene scene = new Scene(root);
		stage.hide();
		stage.setScene(scene);
		stage.show();
	}
}
