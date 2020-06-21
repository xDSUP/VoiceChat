package voicechat.client;

import voicechat.core.Message;
import voicechat.core.User;
import voicechat.core.messages.ChatMessage;
import voicechat.core.messages.CommandPacket;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RoomController {
    static Logger LOGGER = Logger.getLogger(ClientDesktop.class.getName());

    ClientDesktop app;
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
        MultipleSelectionModel<TreeItem<String>> treeSelectionModel = treeUsers.getSelectionModel();
        // устанавливаем слушатель для отслеживания изменений
        treeSelectionModel.selectedItemProperty().addListener(new ChangeListener<TreeItem<String>>(){
            public void changed(ObservableValue<? extends TreeItem<String>> changed, TreeItem<String> oldValue, TreeItem<String> newValue){
                try {
                    app.messageHandler.send(
                            new Message(
                                new CommandPacket(CommandPacket.Command.GET_PROFILE,
                                    newValue.getValue()
                                )
                            )
                    );
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
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
                                    messageText.getText(),
                                    app.getLogin())
                        )
                    )
            );
            app.getOutputStream().flush();
            messageText.clear();
        } catch (IOException e) { // если закрыто соедин
            e.printStackTrace();
        }
    }

    public void printUserProfile(User profile){
        // загрузка сцены профиля
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/profile.fxml"));
            Scene sceneProfile = loader.load();
            ProfileController profileController = loader.getController();
            profileController.textLogin.setText(profile.getLogin());
            profileController.labTitleLogin.setText(profile.getLogin());
            profileController.textName.setText(profile.getName());
            if(app.login.equals(profile.getLogin())) { // если это наш профиль
                profileController.butEdit.setVisible(true);
            }

            Stage windowProfile = new Stage();
            windowProfile.initModality(Modality.APPLICATION_MODAL);
            windowProfile.initOwner(stage);
            windowProfile.setTitle("Profile: " + profile.getLogin());
            windowProfile.setScene(sceneProfile);
            windowProfile.show();
        } catch (IOException e) {
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

    public void printUserList(ArrayList<String> users) {
        LOGGER.log(Level.INFO, "RoomConnt" + "PRINT USER LIST");
        TreeItem<String> root = new TreeItem<>("Server");
        for (String user : users) {
            // добавляем каждого юзера в список
            root.getChildren().add(new TreeItem<String>(user));
        }
        treeUsers.setShowRoot(false);
        treeUsers.setRoot(root);
    }

    public void printNewChatMessage(ChatMessage chatMessage){
        chat.appendText(chatMessage.getLogin() + ": " + chatMessage.getText() + "\n");
    }

    private void updateChat(){
        chat.clear();
        // TODO: получение истории чата с сервера
        chat.setText("");
    }

    public void setServer(Socket socket) { server = socket; }

    public void setApp(ClientDesktop app) { this.app = app; }

    public void setStage(Stage stage) { this.stage = stage; }
}
