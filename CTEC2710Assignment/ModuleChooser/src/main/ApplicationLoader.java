 
package main;

import controller.ModuleChooserController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.StudentProfile;
import view.ModuleChooserRootPane;

public class ApplicationLoader extends Application {

	private ModuleChooserRootPane view;
	
	@Override
	public void init() {
		
		StudentProfile model = new StudentProfile();
		view = new ModuleChooserRootPane();
		new ModuleChooserController(model, view);	
	}
	
	@Override
	public void start(Stage stage) throws Exception {

	    Scene scene = new Scene(view,800,600);
	    scene.getStylesheets().add(getClass().getResource("../academic.css").toExternalForm());
	    stage.setMinWidth(530);
	    stage.setMinHeight(500);
	    stage.setTitle("Course and Module Selection Tool");
	    stage.setScene(scene);
	    stage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}

}
