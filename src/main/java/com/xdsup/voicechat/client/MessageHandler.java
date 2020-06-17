package com.xdsup.voicechat.client;

import com.xdsup.voicechat.core.Message;
import com.xdsup.voicechat.core.messages.AudioPacket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
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
        while (true){
            try {
                if(app.getServer().getInputStream().available() > 0){ // что-то пришло
                    Message message = (Message) in.readObject();
                    LOGGER.info("Message");
                    if(message.getData() instanceof AudioPacket){ // получили звук

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
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}