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
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;

public class Client_Controller {

    @FXML
    private VBox client_messageContainer;

    @FXML
    private Button client_sendImagebtn;

    @FXML
    private Button client_sendFilebtn;

    @FXML
    private Button client_sendbtn;

    @FXML
    private TextField client_textfield;

    @FXML
    private TextField client_textfield2;
    @FXML
    private ScrollPane scrollPane;
    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private String clientName;

    public void initialize() {new Thread(() -> {
            try {
                socket = new Socket("localhost", 4000);
//                socket = serverSocket.accept();

                dataInputStream = new DataInputStream(socket.getInputStream());
                dataOutputStream = new DataOutputStream(socket.getOutputStream());

                Platform.runLater(() -> {
                    clientName = client_textfield2.getText().trim();
                    if (clientName.isEmpty()) {
                        clientName = "thushini";
                    }
                    try {
                        dataOutputStream.writeUTF("name");
                        dataOutputStream.writeUTF(clientName);
                        dataOutputStream.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                while (true) {
                    String type = dataInputStream.readUTF();

                    if ("text".equals(type)) {
                        String message = dataInputStream.readUTF();
                        Platform.runLater(() -> {
                            TextArea textMessage = new TextArea(message);
                            textMessage.setWrapText(true);
                            textMessage.setEditable(false);
                            client_messageContainer.getChildren().add(textMessage);
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
                            client_messageContainer.getChildren().add(imageView);
                        });
//                    } else if ("File".equals(type)) {
//                        String filename = dataInputStream.readUTF();
//                        int fileLength = dataInputStream.readInt();
//                        byte[] fileBytes = new byte[fileLength];
//
//                    }

                } else if ("file".equals(type)) {
                        String fileName = dataInputStream.readUTF();
                        int fileLength = dataInputStream.readInt();
                        byte[] fileBytes = new byte[fileLength];
                        dataInputStream.readFully(fileBytes);

                        File receivedFile = new File("desktop/" + fileName);
                        receivedFile.getParentFile().mkdirs();
                        try (FileOutputStream fileOutputStream = new FileOutputStream(receivedFile)) {
                            fileOutputStream.write(fileBytes);
                        }

                        Platform.runLater(() -> {
                            TextArea fileMessage = new TextArea("File received: " + fileName + " (saved in 'desktop' folder)");
                            fileMessage.setWrapText(true);
                            fileMessage.setEditable(false);
                            client_messageContainer.getChildren().add(fileMessage);
                        });
                    }
//                } catch (UnknownHostException e) {
//                throw new RuntimeException(e);
//            } catch (FileNotFoundException e) {
//                throw new RuntimeException(e);
//            } catch (IOException e) {
//                throw new RuntimeException(e);
            }
    } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void client_sendImagebtn_OnAction(ActionEvent actionEvent) throws IOException {
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
            client_messageContainer.getChildren().add(imageView);
        }
    }

    public void client_sendFilebtn_OnAction(ActionEvent actionEvent) throws IOException {
        FileChooser fileChooser = new FileChooser();
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            byte[] fileBytes = Files.readAllBytes(selectedFile.toPath());
              dataOutputStream.writeUTF("file");
                 dataOutputStream.writeUTF(selectedFile.getName());
               dataOutputStream.writeInt(fileBytes.length);
            dataOutputStream.write(fileBytes);
            dataOutputStream.flush();

            TextArea fileMessage = new TextArea("You sent a file:" + selectedFile.getName());
            fileMessage.setWrapText(true);
            fileMessage.setEditable(false);
            client_messageContainer.getChildren().add(fileMessage);
        }
    }

    public void client_sendbtn_OnAction(ActionEvent actionEvent) throws IOException {
        String message = client_textfield.getText().trim();

        if (!message.isEmpty()) {
            dataOutputStream.writeUTF("text");
            dataOutputStream.writeUTF(clientName + ":" + message);
            dataOutputStream.flush();

            TextArea textMessage = new TextArea("Client: " + message);
            textMessage.setWrapText(true);
            textMessage.setEditable(false);
            client_messageContainer.getChildren().add(textMessage);

            client_textfield.clear();
        }
    }
}
