package voicechat.client;

import voicechat.core.Message;
import voicechat.core.messages.ConnectPacket;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Logger;


public class RegisterController {
    static Logger LOGGER = Logger.getLogger(ClientDesktop.class.getName());

    ClientDesktop app;
    Stage primaryStage;

    @FXML
    TextField textLogin;

    @FXML
    TextField textName;

    @FXML
    TextField textPass;

    @FXML
    TextField textPassConfirm;

    @FXML
    Label status;

    @FXML
    private void initialize() {
    }

    @FXML
    public void registerAction(){
        String login = textLogin.getText();
        if(login.length() < 3 || login.length() > 20) {
            status.setText("Requare login length more 3 and less 20");
            return;
        }

        String name = textName.getText();
        if(name.length() == 0){
            status.setText("Short name!");
            return;
        }

        String pass = textPass.getText();
        if(pass.length() < 6){
            status.setText("Password length < 6 !");
            return;
        }
        if(!pass.equals(textPassConfirm.getText())){
            status.setText("Passwords not equable");
            return;
        }
        //если кто-то дойдет до этого момента


        try (Socket socket = new Socket(app.address.getHostName(), app.port)){
            if (!socket.isConnected()) {
                status.setText("Not connected");
                return;
            }
            //    System.out.println(socket.getPort());
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream()); //create object streams to/from client
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            long time = System.currentTimeMillis();
            Message outM = new Message(
                    new ConnectPacket(
                            login, Integer.toString(pass.hashCode()), name
                    )
            );
            LOGGER.info("Message Connect Register send");
            out.writeObject(outM);
            out.flush();
            for (;;) { // ждем ответа
                try {
                    if (System.currentTimeMillis() - time > 1000000) {
                        status.setText("Not connected");
                        break;
                    }
                    if (socket.getInputStream().available() > 0) { // что-то пришло
                        Message inM = (Message) in.readObject();
                        if(inM.getData() instanceof ConnectPacket){ // это ответ от сервера
                            if(((ConnectPacket) inM.getData()).getStatus() == ConnectPacket.Status.OK){
                                LOGGER.info("Register success");
                                status.setText("REGISTER");
                                app.login = login;
                                app.showLoginWindow();
                            }
                            else{
                                LOGGER.info("Register not success");
                                status.setText("the login is already taken");
                            }
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

    @FXML
    public void cancelAction(){
        app.showLoginWindow();
    }

    public void setApp(ClientDesktop app) {
        this.app = app;
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
}
