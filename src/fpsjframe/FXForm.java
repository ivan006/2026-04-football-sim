package fpsjframe;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class FXForm extends Application {
	@Override
	public void start(Stage stage) {
		VBox root = new VBox(10);
		root.setAlignment(Pos.CENTER);

		TextField name = new TextField();
		name.setPromptText("Name");

		TextField email = new TextField();
		email.setPromptText("Email");

		Button btn = new Button("Submit");

		root.getChildren().addAll(name, email, btn);

		stage.setScene(new Scene(root, 300, 200));
		stage.setTitle("JavaFX Form");
		stage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}