package com.xdsup.voicechat.client;

import com.xdsup.voicechat.server.ServerDekstop;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class Main extends Application {
    private Stage primaryStage;
    private Socket socket;
    static Logger LOGGER;

    @Override
    public void start(Stage primaryStage){
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Chat()");
        showLoginWindow();
    }

    public void showLoginWindow(){
        try{
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/login.fxml"));
            Scene sceneLog = loader.load();
            LoginController loginController = loader.getController();
            loginController.setPrimaryStage(primaryStage);
            loginController.setApp(this);

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
            RoomController roomController = loader.getController();
            roomController.setApp(this);
            roomController.setStage(primaryStage);
            roomController.setServer(socket);
            primaryStage.setScene(sceneRoom);
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        File file = new File("src/main/resources/logger.config");
        try(FileInputStream ins = new FileInputStream(file)){ //полный путь до файла с конфигами
            LogManager.getLogManager().readConfiguration(ins);
            LOGGER = Logger.getLogger(ServerDekstop.class.getName());
        }catch (Exception ignore){
            ignore.printStackTrace();
        }
        launch(args);
    }

    public Socket getServer() {
        return socket;
    }

    public void setServer(Socket server) {
        this.socket = server;
    }
}
