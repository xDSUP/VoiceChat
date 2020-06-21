package voicechat.client;

import voicechat.core.Message;
import voicechat.core.messages.ConnectPacket;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.*;
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
    USER_IS_AUTH("User is auth"),
    REQUIRE_LOGIN_AND_PASSWORD("REQUIRE LOGIN AND PASSWORD"),
    NO_CORRECT_LOGIN_OR_PASSWORD("LOGIN OR PASSWORD NO CORRECT"),
    SERVERS_NOT_FOUND("servers not found");

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
    @FXML
    ListView serversList;



    static Logger LOGGER;
    ClientDesktop app;
    Stage primaryStage;

    int port;
    InetAddress ip;
    String login;

    Socket server;

    Status status = Status.NONE;

    @FXML
    private void initialize() {
        LOGGER = Logger.getLogger(ClientDesktop.class.getName());
        Pattern p = Pattern.compile("(\\d+\\.?\\d*)?");
        //textServerIp.textProperty().addListener((observable, oldValue, newValue) -> {
          //  if (!p.matcher(newValue).matches()) textServerIp.setText(oldValue);
        //});
        textPort.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!p.matcher(newValue).matches()) textPort.setText(oldValue);
        });
        // получаем модель выбора элементов
        MultipleSelectionModel<String> langsSelectionModel = serversList.getSelectionModel();
        // устанавливаем слушатель для отслеживания изменений
        langsSelectionModel.selectedItemProperty().addListener(new ChangeListener<String>(){
            public void changed(ObservableValue<? extends String> changed, String oldValue, String newValue){
                String[] temp = newValue.split(" ");
                textServerIp.setText(temp[0]);
                textPort.setText(temp[1]);
            }
        });
    }

    public void butConnectCliced() throws IOException {

        if(textPort.getText().length() == 1) {
            app.showRoomWindow();
            return;
        }
        ip = InetAddress.getByName(textServerIp.getText());
        port = Integer.parseInt(textPort.getText());
        System.out.println(ip.getHostAddress() +":"  +textPort.getText());

        try{
            Socket socket = new Socket(ip.getHostName(), Integer.parseInt(textPort.getText()));
            if(!socket.isConnected()){
                status = Status.NO_RESPONSE;
                labStatus.setText(status.getText());
                return;
            }
        //    System.out.println(socket.getPort());
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream()); //create object streams to/from client
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            long time = System.currentTimeMillis();
            Message outM = new Message(
                    new ConnectPacket(textLog.getText(), Integer.toString(textPass.getText().hashCode())));
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
                                    //сохраним введенные значения в врем память
                                    saveData();
                                    break;
                                }
                                case NOTCORRECT: {
                                    status = Status.NO_CORRECT_LOGIN_OR_PASSWORD;
                                    break;
                                }
                                case USERAUTH:{
                                    status = Status.USER_IS_AUTH;
                                    break;
                                }
                                case AUTHREQUIRED: { // Такого не будет
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

    @FXML
    public void serverListMouseClicked(){

    }

    public void findAction(){
        app.localScanner = new LocalScanner(app);
        app.localScanner.run();
    }

    public void registerAction(){
        try {
            ip = InetAddress.getByName(textServerIp.getText());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        port = Integer.parseInt(textPort.getText());
        System.out.println(ip.getHostAddress() +":"  +textPort.getText());

        try (Socket socket = new Socket(ip.getHostName(), Integer.parseInt(textPort.getText()))){
            if (!socket.isConnected()) {
                status = Status.NO_RESPONSE;
                labStatus.setText(status.getText());
                return;
            }
            //    System.out.println(socket.getPort());
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream()); //create object streams to/from client
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            long time = System.currentTimeMillis();
            Message outM = new Message(new ConnectPacket(ConnectPacket.Status.CONNECT)); // пошлем сообщение о подключ
            LOGGER.info("Message Connect send");
            out.writeObject(outM);
            out.flush();
            for (;;) { // ждем ответа
                try {
                    if (System.currentTimeMillis() - time > 1000000) {
                        status = Status.NO_RESPONSE;
                        break;
                    }
                    if (socket.getInputStream().available() > 0) { // что-то пришло
                        Message inM = (Message) in.readObject();
                        if(inM.getData() instanceof ConnectPacket){ // это ответ от сервера
                            saveData();
                            app.showRegisterWindow();
                            break;
                        }
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }catch (IOException e){ // нет соединения
            e.printStackTrace();
        }
    }

    public void saveData(){
        app.port = Integer.parseInt(textPort.getText());
        app.login = textLog.getText();
        try {
            app.address = InetAddress.getByName(textServerIp.getText());
            app.addressStr = textServerIp.getText();
        } catch (UnknownHostException e) {
            e.printStackTrace();
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

    public ClientDesktop getApp() {
        return app;
    }

    public void setApp(ClientDesktop app) {
        this.app = app;
    }

    //private boolean connect
}
