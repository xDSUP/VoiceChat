package com.xdsup.voicechat.client;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.Socket;
import java.util.logging.Logger;

public class RoomController {
    static Logger LOGGER = Logger.getLogger(Main.class.getName());

    Main app;
    Stage stage;

    Socket server;

    @FXML
    Button butTalk;
    @FXML
    Button butSend;
    @FXML
    TextArea chat;
    @FXML
    TextArea messageText;
    @FXML
    CheckBox boxAudio;
    @FXML
    CheckBox boxMicro;
    @FXML
    TreeView treeUsers;

    @FXML
    private void initialize() {
        updateRoom();
        updateChat();
    }

    private void updateRoom(){
        TreeItem<String> root = new TreeItem<>("Server");
        treeUsers.setRoot(root);
        // TODO: получение списка людей на сервере
    }

    private void updateChat(){
        chat.clear();
        // TODO: получение истории чата с сервера
        chat.setText("");
    }

    public void setServer(Socket socket) { server = socket; }

    public void setApp(Main app) { this.app = app; }

    public void setStage(Stage stage) { this.stage = stage; }
}