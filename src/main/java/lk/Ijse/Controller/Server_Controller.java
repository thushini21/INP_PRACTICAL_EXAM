package lk.Ijse.Controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;

public class Server_Controller {

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private VBox server_messageContainer;

    @FXML
    private Button server_sendImagebtn;

    @FXML
    private Button server_sendbtn;

    @FXML
    private Button server_sendFilebtn;

    @FXML
    private TextField server_textfield;

    private ServerSocket serverSocket;
    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;

    public void initialize() {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(4000);
                socket = serverSocket.accept();

                dataInputStream = new DataInputStream(socket.getInputStream());
                dataOutputStream = new DataOutputStream(socket.getOutputStream());

                while (true) {
                    String type = dataInputStream.readUTF();

                    if ("text".equals(type)) {
                        String message = dataInputStream.readUTF();
                        Platform.runLater(() -> {
                            TextArea textMessage = new TextArea("Client: " + message);
                            textMessage.setWrapText(true);
                            textMessage.setEditable(false);
                            server_messageContainer.getChildren().add(textMessage);
                        });

                    } else if ("image".equals(type)) {
                        int length = dataInputStream.readInt();
                        byte[] imageBytes = new byte[length];
                        dataInputStream.readFully(imageBytes);
                        Image image = new Image(new ByteArrayInputStream(imageBytes));
                        Platform.runLater(() -> {
                            ImageView imageView = new ImageView(image);
                            imageView.setFitWidth(200);
                            imageView.setPreserveRatio(true);
                            server_messageContainer.getChildren().add(imageView);
                        });

                    } else if ("file".equals(type)) {
                        String fileName = dataInputStream.readUTF();
                        int fileLength = dataInputStream.readInt();
                        byte[] fileBytes = new byte[fileLength];
                        dataInputStream.readFully(fileBytes);

                        File savedFile = new File("desktop/" + fileName);
                        savedFile.getParentFile().mkdirs();
                        try (FileOutputStream fileOutputStream = new FileOutputStream(savedFile)) {
                            fileOutputStream.write(fileBytes);
                        }

                        Platform.runLater(() -> {
                            TextArea fileMessage = new TextArea("File received: " + fileName + " (saved in 'server_files' folder)");
                            fileMessage.setWrapText(true);
                            fileMessage.setEditable(false);
                            server_messageContainer.getChildren().add(fileMessage);
                        });
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    void server_sendImagebtn_OnAction(ActionEvent event) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            byte[] imageBytes = Files.readAllBytes(selectedFile.toPath());

            dataOutputStream.writeUTF("image");
            dataOutputStream.writeInt(imageBytes.length);
            dataOutputStream.write(imageBytes);
            dataOutputStream.flush();

            Image image = new Image(selectedFile.toURI().toString());
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(200);
            imageView.setPreserveRatio(true);
            server_messageContainer.getChildren().add(imageView);
        }
    }

    @FXML
    void server_sendbtn_OnAction(ActionEvent event) throws IOException {
        String message = server_textfield.getText();

        dataOutputStream.writeUTF("text");
        dataOutputStream.writeUTF(message);
        dataOutputStream.flush();

        TextArea textMessage = new TextArea("Server: " + message);
        textMessage.setWrapText(true);
        textMessage.setEditable(false);
        server_messageContainer.getChildren().add(textMessage);

        server_textfield.clear();
    }

    @FXML
    void server_sendFilebtn_OnAction(ActionEvent actionEvent) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All Files", "*.*"));
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            String fileName = selectedFile.getName();
            byte[] fileBytes = Files.readAllBytes(selectedFile.toPath());

            dataOutputStream.writeUTF("file");
            dataOutputStream.writeUTF(fileName);
            dataOutputStream.writeInt(fileBytes.length);
            dataOutputStream.write(fileBytes);
            dataOutputStream.flush();


            Platform.runLater(() -> {
                TextArea fileMessage = new TextArea("File sent: " + fileName);
                fileMessage.setWrapText(true);
                fileMessage.setEditable(false);
                server_messageContainer.getChildren().add(fileMessage);
            });
        }
    }
}
