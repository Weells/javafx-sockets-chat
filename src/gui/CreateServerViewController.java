package gui;

import java.io.IOException;
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
import server.Server.ConnectionResponse;

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
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		Constraints.setTextFieldInteger(txtServerPort);
	}
	
	public void createConnection() {
		if(txtServerPort.getText().isEmpty()) {
			labelError.setText("");
		} else {
			int port = Integer.valueOf(txtServerPort.getText());
			Server server = new Server();
			ConnectionResponse response = server.initializeServer(port);
			
			switch(response) {
			case ERROR:
				labelError.setText("Erro ao criar servidor");
				break;
			case ADDRESS_IN_USE:
				labelError.setText("Esta porta já está em uso");
				break;
			default:
				break;
			}
		}
	}
	
	public void onBtnLoginDialog(ActionEvent event) throws IOException {
		Parent root = FXMLLoader.load(getClass().getResource("/gui/LoginView.fxml"));
		Stage stage = (Stage) ((Node)event.getSource()).getScene().getWindow();
		Scene scene = new Scene(root);
		stage.hide();
		stage.setScene(scene);
		stage.show();
	}
}
