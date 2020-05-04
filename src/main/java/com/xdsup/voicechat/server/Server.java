package com.xdsup.voicechat.server;

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
    boolean AutorizationRequired = false;
    ServerSocket serverSocket;
    Logger LOGGER;

    // для каждого нового человека новый поток будет
    ArrayList<ClientConnection> clients;

    Server(int port){
        clients = new ArrayList<>();
        this.port = port;
        LOGGER = Logger.getLogger(ServerDekstop.class.getName());
        LOGGER.log(Level.INFO, "Server started");
    }

    public void run() {

        try (ServerSocket serverSocket = new ServerSocket(port)){
            this.serverSocket = serverSocket;
            LOGGER.log(Level.INFO, "Server runned, port " + port);
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
        }
    }

    // подключить нового человека
    public void connectNewClient(ClientConnection client){
        clients.add(client);
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
