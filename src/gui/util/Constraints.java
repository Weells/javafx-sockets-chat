package gui.util;

import javafx.scene.control.TextField;

public class Constraints {

	//Regra que permite somente a inserção de valores inteiros em um TextField
	public static void setTextFieldInteger(TextField txt) {
		txt.textProperty().addListener((obs, oldValue, newValue) -> {
			if (newValue != null && !newValue.matches("\\d*")) {
				txt.setText(oldValue);
			}
		});
	}
	
	//Regra que permite somente a inserção de pontos e números em um TextField
	public static void setTextFieldFormat(TextField txt) {
		txt.textProperty().addListener((obs, oldValue, newValue) -> {
			if (newValue != null && !newValue.matches("(?:\\.|[0-9])*")) {
				txt.setText(oldValue);
			}
		});
	}
}
