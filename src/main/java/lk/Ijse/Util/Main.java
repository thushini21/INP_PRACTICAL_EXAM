package lk.Ijse.Util;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
       /* stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/view/Server.fxml"))));
        stage.setTitle("Login Form");
        stage.centerOnScreen();
        stage.show();*/

        try {
            stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/view/Server.fxml"))));
            stage.setTitle("Server Form");
            stage.centerOnScreen();
            stage.show();

            Stage stage2 = new Stage();

            stage2.setScene(new Scene(FXMLLoader.load(getClass().getResource("/view/Client.fxml"))));
            stage2.setTitle("Client Form");
            stage2.centerOnScreen();
            stage2.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

/*
 try {
         pane1.getChildren().clear();
            pane1.getChildren().add(FXMLLoader.load(getClass().getResource("fxml1.fxml")));

        pane2.getChildren().clear();
            pane2.getChildren().add(FXMLLoader.load(getClass().getResource("fxml2.fxml")));
        } catch (IOException ex) {
        System.out.print(ex);
        }*/
