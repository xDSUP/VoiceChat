package com.xdsup.voicechat.client;

import com.xdsup.voicechat.core.Message;
import com.xdsup.voicechat.core.User;
import com.xdsup.voicechat.core.messages.ChatMessage;
import com.xdsup.voicechat.core.messages.CommandPacket;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
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
        updateChat();
    }

    @FXML
    private void changeMicroOnAction(){ // изменение галочки
        if(boxMicro.isSelected()){
            LOGGER.info("MicroOnChanged = ON");
            app.microOn = true;
        }
        else {
            LOGGER.info("MicroOnChanged = OFF");
            app.microOn = false;
        }
    }

    @FXML
    private void sendAction(){
        try {
            // отправляем сообщение с командным пакетом , где
            // лежит ник отправителя и текст сообщения
            app.getOutputStream().writeObject(
                    new Message(
                    new CommandPacket(
                            CommandPacket.Command.CHAT,
                            new ChatMessage(
                                    app.getClientId(),
                                    messageText.getText()))
            ));
            app.getOutputStream().flush();
        } catch (IOException e) { // если закрыто соедин
            e.printStackTrace();
        }
    }

    @FXML
    private void talkAction(){

    }

    @FXML
    private void changeAudioOnAction(){ // изменение галочки
        if(boxAudio.isSelected()){
            app.audioOn = true;
        }
        else{
            app.audioOn = false;
        }
    }

    public void updateRoom() {
        LOGGER.log(Level.INFO, "RoomConnt" + "UPDATE ROOM");
        TreeItem<String> root = new TreeItem<>("Server");
        treeUsers.setRoot(root);
        // TODO: получение списка людей на сервере
        Message request = new Message(
                new CommandPacket(CommandPacket.Command.GET_USERS));
        try {
            app.messageHandler.send(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void printUserList(ArrayList<User> users) {
        LOGGER.log(Level.INFO, "RoomConnt" +"PRINT USER LIST");
        TreeItem<String> root = new TreeItem<>("Server");
        for (User user: users) {
            // добавляем каждого юзера в список
            root.getChildren().add(new TreeItem<String>(user.getLogin()));
        }
        treeUsers.setRoot(root);
        treeUsers.setShowRoot(true);
    }

    public void printNewChatMessage(ChatMessage chatMessage){
        chat.appendText(chatMessage.getClId() + ": " + chatMessage.getText() + "\n");
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
