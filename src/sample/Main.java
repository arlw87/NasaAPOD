package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Main Class the entry point to the JavaFX program
 *  @author Andrew White
 *  @version 1
 */

public class Main extends Application {

    //The dimension of the GUI window
    protected final static int WINDOW_WIDTH = 1500;
    protected final static int WINDOW_HEIGHT = 1000;

    @Override
    public void start(Stage primaryStage) throws Exception{

        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        Scene aScene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
        primaryStage.setScene(aScene);
        primaryStage.show();

    }


    public static void main(String[] args) {
        launch(args);
    }
}
