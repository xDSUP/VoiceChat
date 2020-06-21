package voicechat.client;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javax.sound.sampled.*;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.logging.*;

public class ClientDesktop extends Application {
    private Stage primaryStage;
    private long clientId;
    public static Logger LOGGER;
    RoomController roomController;
    LoginController loginController;

    // для временного хранения
    ObservableList<String> servers;
    InetAddress address;
    String addressStr;
    int port;
    String login;

    //для сервера
    private Socket socket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;

    // для аудио
    boolean microOn = false;
    boolean audioOn = false;
    MessageHandler messageHandler;
    // храним аудиоканалы для быстрого поиска по ним
    HashMap<Long, AudioChannel> audioChannels;
    LocalScanner localScanner;

    @Override
    public void start(Stage primaryStage){
        LOGGER = Logger.getLogger(ClientDesktop.class.getName());
        LOGGER.setLevel(Level.INFO);


        this.primaryStage = primaryStage;
        primaryStage.setTitle("Chat()");
        servers = FXCollections.observableArrayList();
        showLoginWindow();
    }

    public void showLoginWindow(){
        try{
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/login.fxml"));
            Scene sceneLog = loader.load();
            loginController = loader.getController();
            loginController.setPrimaryStage(primaryStage);
            loginController.setApp(this);
            loginController.serversList.setItems(servers);
            LOGGER.info("LoginForm load");
            // загрузка ранее сохраненных
            if(login != null)
                loginController.textLog.setText(login);
            if(addressStr != null)
                loginController.textServerIp.setText(addressStr);
            if(port!=0)
                loginController.textPort.setText(Integer.toString(port));

            primaryStage.setScene(sceneLog);
            primaryStage.show();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    public void showRoomWindow(){
        try{
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/source.fxml"));
            Scene sceneRoom = loader.load();
            roomController = loader.getController();
            roomController.setApp(this);
            roomController.setStage(primaryStage);
            roomController.setServer(socket);


            primaryStage.setScene(sceneRoom);
            if(roomController.boxMicro.isSelected()) {
                //captureAudio();
            }
            // иниц приема
            messageHandler = new MessageHandler(this);
            messageHandler.start();
            // инициализация отправки
            Thread audioSender = new AudioSenderThread(this, outputStream);
            audioSender.start();
            // иниц вывода звука
            audioChannels = new HashMap<>();

            //roomController.updateRoom(); // обновим список людей
        }
        catch (IOException e){
            e.printStackTrace();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public void showRegisterWindow(){
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/register.fxml"));
            Scene sceneReg = loader.load();
            RegisterController registerController = loader.getController();
            registerController.setPrimaryStage(primaryStage);
            registerController.setApp(this);
            LOGGER.info("RegisterForm load");

            primaryStage.setScene(sceneReg);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        LOGGER.info("STOPPED");
        if(messageHandler != null)
            messageHandler.stop();
        if(socket != null){
            if(!socket.isClosed())
                socket.close();
        }
        if(audioChannels != null && audioChannels.size() > 0)
            audioChannels.forEach((k,v)-> v.closeAndKill());
    }

    public static void main(String[] args) {
        launch(args);
    }

    public String getLogin() {
        return login;
    }

    public Socket getServer() {
        return socket;
    }

    public void setServer(Socket server) {
        this.socket = server;
    }

    public long getClientId() {
        return clientId;
    }

    public void setClientId(long clientId) {
        this.clientId = clientId;
    }

    public ObjectOutputStream getOutputStream() {
        return outputStream;
    }

    public void setOutputStream(ObjectOutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public ObjectInputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(ObjectInputStream inputStream) {
        this.inputStream = inputStream;
    }
}
