package com.xdsup.voicechat.server;

import com.xdsup.voicechat.core.Message;
import com.xdsup.voicechat.core.messages.AudioPacket;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayDeque;
import java.util.logging.Level;

public class BroadcastSender extends Thread{

    ArrayDeque<AudioPacket> queue;
    Server server;

    public BroadcastSender(Server server){
        super();
        this.server = server;
        queue = new ArrayDeque<>();
    }

    public void addAudioPacket(AudioPacket aP){
        queue.add(aP);
    }

    @Override
    public void run() {
        server.LOGGER.log(Level.INFO, "Brodcast Run");
        broadcastrun:
        while(true){
            if(!queue.isEmpty()){ // если нужно что-то отправить
                AudioPacket temp = queue.pop();
                for (ClientConnection client: server.clients) {
                    // проверка, чтобы не отправить пакет тому кл, кот его прислал
                    if(client.getId() != temp.getClientId()){
                        try {
                            client.send(new Message(temp));
                        } catch (IOException e) { // не отправилось
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}

