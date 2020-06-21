package voicechat.server;

import voicechat.core.Message;
import voicechat.core.User;
import voicechat.core.messages.CommandPacket;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server implements Runnable{
    // айдишки для комнат
    int idForRoom = 0;
    // у сервера будет несколько комнат
    //ArrayList<Room> rooms;
    int port;
    UdpServer udpServer;
    boolean AutorizationRequired = true;
    ServerSocket serverSocket;
    public static Logger LOGGER;

    // для каждого нового человека новый поток будет
    ArrayList<ClientConnection> clients;
    // широковещательный канал
    BroadcastSender broadcastSender;
    // база данных для хранения всех логинов и паролей
    DataBase dataBase;

    Server(int port){
        clients = new ArrayList<>();
        this.port = port;
        LOGGER = Logger.getLogger(ServerDekstop.class.getName());
        LOGGER.log(Level.INFO, "Server started");
        broadcastSender = new BroadcastSender(this);
        dataBase = new DataBase();
        if(dataBase.load())
            LOGGER.info("Base loaded");
        else
            LOGGER.info("Base not loaded");
    }

    public void run() {
        udpServer = new UdpServer(port);
        udpServer.start();
        try (ServerSocket serverSocket = new ServerSocket(port)){
            this.serverSocket = serverSocket;
            LOGGER.log(Level.INFO, "Server runned, port " + port);
            broadcastSender.start();
            while (true) {
                try{
                    // ждем подключений и переводим их в новый поток для дальнейшего общения
                    Socket cl = serverSocket.accept();
                    LOGGER.log(Level.INFO, "Client conection " + cl.getInetAddress() + " " + cl.getPort());
                    ClientConnection client = new ClientConnection(this, cl);
                    connectNewClient(client);

                }catch (IOException e){
                e.printStackTrace();
                }
            }
        }catch (IOException e){
            LOGGER.log(Level.WARNING, "Server not runned, port " + port, e);
        }
        finally {
            for (ClientConnection cl:clients) {
                cl.stop();
            }
            broadcastSender.stop();
        }
    }

    public void addNewClient(ClientConnection client){
        clients.add(client);
    }

    // подключить нового человека
    public void connectNewClient(ClientConnection client){
        client.start();
    }

    public void disconnectClient(ClientConnection client){
        for (ClientConnection cl: clients) {
            if(cl == client){
                clients.remove(cl);
            }
        }
    }

    public boolean isAutorizationRequired() {
        return AutorizationRequired;
    }

    public ArrayList<User> getUsers(){
        ArrayList<User> users = new ArrayList<>();
        for (ClientConnection cl: clients) {
             if(cl.isAuthorized()){ // добавлять будем только авторизованных клиентов
                 users.add(cl.getUserProfile());
             }
        }
        return users;
    }

    public ArrayList<String> getLogins(){
        ArrayList<ClientConnection> killList = new ArrayList<>();
        ArrayList<String> users = new ArrayList<>();
        for (ClientConnection cl: clients) {
            if(cl.isAuthorized()){ // добавлять будем только авторизованных клиентов
                users.add(cl.getUserProfile().getLogin());
            }
            if(!cl.isAlive()){
                killList.add(cl);
            }
        }
        for(ClientConnection cl: killList){
            clients.remove(cl);
        }
        return users;
    }

    public void commandUpdateUsers(){
        broadcastSender.sendAll(new Message(
                new CommandPacket(
                        CommandPacket.Command.GET_USERS,
                        getLogins())
        ));
    }

    public boolean checkUser(String login){
        for (ClientConnection cl: clients) {
            if (cl.isAuthorized()) { // добавлять будем только авторизованных клиентов
                if(login.equals(cl.getUserProfile().getLogin()))
                    return true;
            }
        }
        return false;
    }
    //void addNewRoom(String title){
    //    rooms.add(new Room(idForRoom++,title));
    //}

    //void delRoom(String title){
    //    for (Room room: rooms) {
    //        if(room.title == title){
    //            rooms.remove(room);
    //        }
    //    }
    //}
}
