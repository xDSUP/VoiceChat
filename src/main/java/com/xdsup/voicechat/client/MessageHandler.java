package com.xdsup.voicechat.client;

import com.xdsup.voicechat.core.Message;
import com.xdsup.voicechat.core.User;
import com.xdsup.voicechat.core.messages.AudioPacket;
import com.xdsup.voicechat.core.messages.ChatMessage;
import com.xdsup.voicechat.core.messages.CommandPacket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.xdsup.voicechat.client.Main.LOGGER;

class MessageHandler extends Thread{
    ObjectInputStream in;
    ObjectOutputStream out;
    Main app;

    public MessageHandler(Main app){
        super();
        this.app = app;
        out = app.getOutputStream();
        in = app.getInputStream();
    }

    public void send(Message message) throws IOException {
        out.writeObject(message);
        out.flush();
    }

    @Override
    public void run() {
        LOGGER.info("MessageHandler Run");
        while (true){
            try {
                if(app.getServer().getInputStream().available() > 0){ // что-то пришло
                    Message message = (Message) in.readObject();
                    if(message.getData() instanceof AudioPacket){ // получили звук
                        LOGGER.info("AudioMessage");
                        if(!app.audioOn) // если звук выключен, мы его не обрабатываем
                            continue;
                        AudioPacket audioPacket = (AudioPacket) message.getData();
                        Long key = audioPacket.getClientId();
                        if(app.audioChannels.containsKey(key)) { // если есть подходящий поток
                            app.audioChannels.get(key).addAudioPacket(audioPacket);
                        }
                        else{ // если подходящего потока нет, создадим
                            LOGGER.info("Audio channel " + key + " created");
                            AudioChannel audioChannel = new AudioChannel(app, key);
                            audioChannel.addAudioPacket(audioPacket);
                            app.audioChannels.put(key, audioChannel);
                            audioChannel.start();
                        }
                    }
                    else if(message.getData() instanceof CommandPacket){
                        LOGGER.info("CommandMessage");
                        CommandPacket packet = (CommandPacket)message.getData();
                        switch (packet.getCommand()){
                            case GET_USERS: {// пришело обновлене с сервера
                                LOGGER.log(Level.INFO, "ComandPack " + "GET_USERS");
                                if(packet.getData() != null) {
                                    ArrayList<User> users = (ArrayList<User>) packet.getData();
                                    app.roomController.printUserList(users);
                                }
                                break;
                            }
                            case CHAT:{ // пришло новое сообщение в чат
                                LOGGER.log(Level.INFO, "ComandPack " + "CHAT");
                                if(packet.getData()!= null){
                                    ChatMessage cM = (ChatMessage) packet.getData();
                                    app.roomController.printNewChatMessage(cM);
                                }
                                break;
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}