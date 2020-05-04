package com.xdsup.voicechat.server;

import com.xdsup.voicechat.core.Message;
import com.xdsup.voicechat.core.messages.ConnectPacket;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientConnection extends Thread {

    // время, через которое соединение разорвется с этим кдиентом, если он не залогинился
    public static final long timeForConnect = 100000L;
    // время подключения кличента к нашему серверу
    private final long timeConnect;
    static long idCounter = 0;

    Socket socket; // соединение с клиентом
    Server server; // для отправки аудио сообщений
    ObjectInputStream in;
    ObjectOutputStream out;

    private boolean authorized;
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
            while(true){
                try {
                    if(!authorized && System.currentTimeMillis() - timeConnect > timeForConnect){
                        LOGGER.log(Level.INFO, "Client conection" + getInetAddress() + ":" + getPort()
                                + " closed<= NOT AUTH");
                        socket.close();
                        break;
                    }
                    if(socket.getInputStream().available() > 0){ // что-то пришло
                        Message message = (Message) in.readObject();
                        LOGGER.log(Level.INFO, "Message from " + getInetAddress() + ":" + getPort());
                        // если получили пакет подключения
                        if(message.getData() instanceof ConnectPacket){
                            LOGGER.log(Level.INFO, "Connect packet from " + getInetAddress() + ":" + getPort());
                            // Если на сервере требуется авторизация
                            if(server.isAutorizationRequired()){
                                // TODO: процесс авторизации на сервере
                            }
                            else {
                                authorized = true;
                                ConnectPacket outP = (ConnectPacket) message.getData();
                                outP.reset(); // подготовили для отправки назад
                                outP.setStatus(ConnectPacket.Status.OK);
                                LOGGER.log(Level.INFO, "Answer: " + outP.getStatus()+ " to" + getInetAddress() + ":" + getPort());
                                Message outM = new Message(outP);
                                out.writeObject(outM);
                                out.flush();
                            }
                        }
                    }
                }catch (ClassNotFoundException e){
                }
            }
        }catch (IOException e){ // ошибка соединения или разрыв
            LOGGER.log(Level.WARNING, "СlientConnection exception ", e);
        }
        finally {
            try {
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
}
