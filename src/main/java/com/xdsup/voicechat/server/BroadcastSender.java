package com.xdsup.voicechat.server;

import com.xdsup.voicechat.core.Message;
import com.xdsup.voicechat.core.Utils;
import com.xdsup.voicechat.core.messages.AudioPacket;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayDeque;
import java.util.logging.Level;

public class BroadcastSender extends Thread{

    ArrayDeque<Message> queue;
    Server server;

    public BroadcastSender(Server server){
        super();
        this.server = server;
        queue = new ArrayDeque<>();
    }

    public void sendAll(Message message){
        queue.add(message);
    }

    @Override
    public void run() {
        server.LOGGER.log(Level.INFO, "Brodcast Run");
        broadcastrun:
        while(true) {
            if (queue.isEmpty()) { // если нечего отправить
                Utils.sleep(10);
                continue ;
            }
            Message temp = queue.pop();
            for (ClientConnection client : server.clients) {
                if(temp.getData() instanceof AudioPacket)                {
                    AudioPacket aP = (AudioPacket) temp.getData();
                    // проверка, чтобы не отправить пакет тому кл, кот его прислал
                    if (client.getChId() != aP.getClientId()) {
                        try {
                            client.send(temp);
                        } catch (IOException e) { // не отправилось
                            e.printStackTrace();
                        }
                    }
                }
                else { // не аудио
                    try {
                        client.send(temp);
                    } catch (IOException e) { // не отправилось
                        e.printStackTrace();
                    }
                }

            }

        }
    }
}

