package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {
	@Override

	public void start(Stage primaryStage) {
		
		finestraOberta dades = finestraOberta.getInstancia();
		if (!dades.isOberta()) {
			try {
				VBox rootLogin = (VBox) FXMLLoader.load(getClass().getResource("Login.fxml"));
				Scene scene = new Scene(rootLogin, 450, 600);
				scene.getStylesheets().add(getClass().getResource("login.css").toExternalForm());
				primaryStage.setScene(scene);
				primaryStage.setTitle("Login");
				primaryStage.show();
				
				dades.setOberta(true);

				primaryStage.setOnCloseRequest(x -> {
					dades.setOberta(false);
				});

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	public static void main(String[] args) {
		launch(args);
	}
}
