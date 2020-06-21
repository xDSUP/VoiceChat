package voicechat.server;

import voicechat.core.Message;
import voicechat.core.User;
import voicechat.core.messages.AudioPacket;
import voicechat.core.messages.CommandPacket;
import voicechat.core.messages.ConnectPacket;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientConnection extends Thread {

    // время, через которое соединение разорвется с этим кдиентом, если он не залогинился
    public static final long timeForConnect = 400000L;
    // время, через которое соединение разорвется с этим кдиентом, если он не присылает сообщения
    public static final long timeForRequest = 500000000L;
    // время подключения кличента к нашему серверу
    private final long timeConnect;
    private long timeLastPacket;
    static long idCounter = 0;

    Socket socket; // соединение с клиентом
    Server server; // для отправки аудио сообщений
    DatagramSocket datagramSocket;
    ObjectInputStream in;
    ObjectOutputStream out;

    private boolean authorized;
    private User userProfile;
    private final long chId; // уникальная айдишка клиента

    static Logger LOGGER;

    static {
        LOGGER = Logger.getLogger(ServerDekstop.class.getName());
    }

    ClientConnection(Server server, Socket clientSocket){
        this.server = server;
        socket = clientSocket;
        authorized = false;
        timeConnect = System.currentTimeMillis();
        chId = idCounter++;
    }
    // отправляет клиенту сообщение message
    void send(Message message) throws IOException {
        out.writeObject(message);
        out.flush();
    }

    public boolean canKill(){
        if(!authorized && System.currentTimeMillis() - timeConnect > timeForConnect)
            return true;
        if(authorized && System.currentTimeMillis() - timeLastPacket > timeForRequest)
            return true;
        return false;
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream()); //create object streams to/from client
            in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException ex) { //connection error, close connection
            try {
                LOGGER.log(Level.WARNING, "ERROR " + getInetAddress() + ":" + getPort(), ex);
                socket.close();
            } catch (IOException ex1) {
            }
            //stop();
        }

        try{
            while(true) {
                try {
                    if (canKill()) {
                        LOGGER.log(Level.INFO, "Client conection" + getInetAddress() + ":" + getPort()
                                + " closed<= NOT AUTH");
                        socket.close();
                        break;
                    }
                    if (socket.getInputStream().available() > 0) { // что-то пришло
                        timeLastPacket = System.currentTimeMillis();
                        Message message = (Message) in.readObject();
                        LOGGER.log(Level.INFO, "Message from " + getInetAddress() + ":" + getPort());
                        // если получили пакет подключения
                        if (message.getData() instanceof ConnectPacket) {
                            LOGGER.log(Level.INFO, "Connect packet from " + getInetAddress() + ":" + getPort());
                            ConnectPacket connectPacket = (ConnectPacket) message.getData();
                            switch (connectPacket.getStatus()){
                                case AUTH:{ // запрос на авторизацию
                                    if (server.isAutorizationRequired()) {
                                        // если нет логина или пароля
                                        if(connectPacket.getLogin().equals("") || connectPacket.getPassword().equals("")){
                                            connectPacket.reset();
                                            connectPacket.setStatus(ConnectPacket.Status.AUTHREQUIRED);
                                            send(new Message(connectPacket));
                                        }
                                        else{ // есть лог и пар
                                            // если такой пользователь есть
                                            if(server.dataBase.checkUser(connectPacket.getLogin())){
                                                // если такой пользователь уже авторизован
                                                if(server.checkUser(connectPacket.getLogin())){
                                                    connectPacket.reset();
                                                    connectPacket.setStatus(ConnectPacket.Status.USERAUTH);
                                                    send(new Message(connectPacket));
                                                }
                                                // если пароль от этого пользователя правильный
                                                else if(server.dataBase.checkPasswordByUser(
                                                        connectPacket.getLogin(),
                                                        connectPacket.getPassword())){
                                                    authorized = true;
                                                    userProfile = server.dataBase.getUser(connectPacket.getLogin());
                                                    connectPacket.reset();
                                                    connectPacket.setStatus(ConnectPacket.Status.OK);
                                                    connectPacket.setClientId(chId);
                                                    send(new Message(connectPacket));
                                                    server.addNewClient(this);
                                                    server.commandUpdateUsers();
                                                }
                                                else{ // пароль не прав
                                                    connectPacket.reset();
                                                    connectPacket.setStatus(ConnectPacket.Status.NOTCORRECT);
                                                    send(new Message(connectPacket));
                                                }
                                            }
                                            else{// такого пользователя нет в базе
                                                connectPacket.reset();
                                                connectPacket.setStatus(ConnectPacket.Status.NOTCORRECT);
                                                send(new Message(connectPacket));
                                            }
                                        }
                                        LOGGER.log(Level.INFO, "Connect packet from " + getInetAddress() +
                                                ":" + getPort());
                                    } else {
                                        authorized = true;
                                        userProfile = new User(connectPacket.getLogin());
                                        connectPacket.reset(); // подготовили для отправки назад
                                        connectPacket.setStatus(ConnectPacket.Status.OK);
                                        connectPacket.setClientId(chId);
                                        LOGGER.log(Level.INFO, "Answer: " + connectPacket.getStatus() + " to"
                                                + getInetAddress() + ":" + getPort());
                                        Message outM = new Message(connectPacket);
                                        send(outM);
                                        //сообщим всем, что кол-во кл изменилось
                                        server.addNewClient(this);
                                        server.commandUpdateUsers();
                                    }
                                    break;
                                }
                                case CONNECT:{
                                    connectPacket.reset();
                                    connectPacket.setStatus(ConnectPacket.Status.OK);
                                    send(new Message(connectPacket));
                                    break;
                                }
                                case REGISTER:{ // запрос на регистрацию
                                    if(!server.dataBase.checkUser(connectPacket.getLogin())) { // если такого польз нет
                                        LOGGER.info("Register success");
                                        server.dataBase.addNewUser(
                                                new User(
                                                        connectPacket.getLogin(),
                                                        connectPacket.getName()),
                                                connectPacket.getPassword()
                                        );
                                        connectPacket.reset();
                                        connectPacket.setStatus(ConnectPacket.Status.OK);
                                        send(new Message(connectPacket));
                                        server.dataBase.save();
                                    }
                                    else{
                                        LOGGER.info("Not registred");
                                        connectPacket.reset();
                                        connectPacket.setStatus(ConnectPacket.Status.NOTCORRECT);
                                        send(new Message(connectPacket));
                                    }
                                    break;
                                }
                            }
                        }
                        else if (message.getData() instanceof AudioPacket) {
                            LOGGER.log(Level.INFO, "Audio packet from " + getInetAddress() + ":" + getPort());
                            server.broadcastSender.sendAll(message);
                        }
                        else if (message.getData() instanceof CommandPacket){
                            CommandPacket packet = (CommandPacket)message.getData();
                            switch (packet.getCommand()){
                                case GET_USERS: {// пришел запрос на обновление
                                    packet.setData(server.getUsers());
                                    send(new Message(packet));
                                    break;
                                }
                                case CHAT:{ // пришло сообщение чата, отправлю всем
                                    server.broadcastSender.sendAll(message);
                                    break;
                                }
                                case GET_PROFILE:{
                                    String temp = (String) packet.getData();
                                    if(server.dataBase.checkUser(temp)) {
                                        packet.setData(server.dataBase.getUser(temp));
                                        send(new Message(packet));
                                    }
                                }
                            }
                        }
                    }
                }catch (StreamCorruptedException e){
                    e.printStackTrace();
                }
                catch (ClassNotFoundException e) {
                }
            }
        }catch (IOException e){ // ошибка соединения или разрыв
            LOGGER.log(Level.WARNING, "СlientConnection exception ", e);
        }
        finally {
            LOGGER.log(Level.WARNING, "СlientConnection clossed ");
            try {
                server.commandUpdateUsers();
                socket.close();
            } catch (IOException e){

            }

            stop();
        }
    }

    public InetAddress getInetAddress(){ return socket.getInetAddress(); }

    public boolean getIsAlive(){ return socket.isConnected() && !socket.isClosed(); }

    public int getPort(){
        return socket.getPort();
    }

    public long getChId() {
        return chId;
    }

    public ObjectInputStream getIn() {
        return in;
    }

    public boolean isAuthorized() {
        return authorized;
    }

    public User getUserProfile() {
        return userProfile;
    }

    public ObjectOutputStream getOut() {
        return out;
    }
}
