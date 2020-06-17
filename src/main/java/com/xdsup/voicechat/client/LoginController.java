package com.xdsup.voicechat.client;

import com.xdsup.voicechat.core.Connector;
import com.xdsup.voicechat.core.Message;
import com.xdsup.voicechat.core.Utils;
import com.xdsup.voicechat.core.messages.ConnectPacket;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeView;
import javafx.stage.Stage;

import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

enum Status{
    IP_NOT_CORRECT("IP OR PORT NOT CORRECT"),
    NO_RESPONSE("SERVER NOT RESPONSE"),
    NONE("NONE"),
    OK("OK"),
    REQUIRE_LOGIN("REQUIRE LOGIN"),
    REQUIRE_LOGIN_AND_PASSWORD("REQUIRE LOGIN AND PASSWORD"),
    NO_CORRECT_LOGIN_OR_PASSWORD("LOGIN OR PASSWORD NO CORRECT");

    String text;

    Status(String text){
        this.text = text;
    }

    public String getText(){
        return text;
    }
}

public class LoginController {

    @FXML
    Button butConnect;
    @FXML
    TextField textLog;
    @FXML
    TextField textPass;
    @FXML
    TextField textServerIp;
    @FXML
    Label labStatus;
    @FXML
    TextField textPort;


    static Logger LOGGER;
    Main app;
    Stage primaryStage;

    Socket server;

    Status status = Status.NONE;

    @FXML
    private void initialize() {
        LOGGER = Logger.getLogger(Main.class.getName());
        Pattern p = Pattern.compile("(\\d+\\.?\\d*)?");
        textServerIp.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!p.matcher(newValue).matches()) textServerIp.setText(oldValue);
        });
        textPort.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!p.matcher(newValue).matches()) textPort.setText(oldValue);
        });

    }

    public void butConnectCliced() throws IOException {

        if(textPort.getText().length() == 1) {
            app.showRoomWindow();
            return;
        }

        InetAddress ip = InetAddress.getByName(textServerIp.getText());
        int port = Integer.parseInt(textPort.getText());
        //InetAddress ip = InetAddress.getLocalHost();
        System.out.println(ip.getHostAddress() +":"  +textPort.getText());

        try(Socket socket = new Socket(ip.getHostName(), Integer.parseInt(textPort.getText()))){
            ;
            if(!socket.isConnected()){
                status = Status.NO_RESPONSE;
                labStatus.setText(status.getText());
                return;
            }
            System.out.println(socket.getPort());
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream()); //create object streams to/from client
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            long time = System.currentTimeMillis();
            Message outM = new Message(
                    new ConnectPacket(textLog.getText(), textPass.getText()));
            LOGGER.info("Message Connect send");
            out.writeObject(outM);
            out.flush();
            for(;;){ // ждем ответа
                try {
                    // TODO: сохранение соединения, если пароль неправильный или нужн логин и тд
                    if(System.currentTimeMillis() - time > 1000000) break;
                    if (socket.getInputStream().available() > 0) { // что-то пришло
                        Message inM = (Message) in.readObject();
                        if(inM.getData() instanceof ConnectPacket){
                            ConnectPacket inP = (ConnectPacket) inM.getData();
                            LOGGER.info("Server answer: "+ inP.getStatus().toString());
                            switch(inP.getStatus()){
                                case OK:{
                                    status = Status.OK;
                                    app.setServer(socket);
                                    app.setClientId(inP.getClientId());
                                    app.setInputStream(in);
                                    app.setOutputStream(out);
                                    app.showRoomWindow();
                                    break;
                                }
                                case NOTCORRECT: {
                                    status = Status.NO_CORRECT_LOGIN_OR_PASSWORD;
                                    break;
                                }
                                case AUTHREQUIRED: {
                                    status = Status.REQUIRE_LOGIN_AND_PASSWORD;
                                    break;
                                }
                                default: {
                                    status = Status.NONE;
                                    break;
                                }
                            }
                            labStatus.setText(status.getText());
                        }
                        break;
                    }
                }catch (ClassNotFoundException e){
                    break;
                }
            }

        } catch (IOException e){
            status = Status.NO_RESPONSE;
            labStatus.setText(status.getText());
            LOGGER.log(Level.WARNING, "Server not runned, port " + port, e);
            server.close();
        }
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public Socket getServer() {
        return server;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Main getApp() {
        return app;
    }

    public void setApp(Main app) {
        this.app = app;
    }

    //private boolean connect
}
