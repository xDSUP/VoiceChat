package com.xdsup.voicechat.client;

import com.xdsup.voicechat.core.Message;
import com.xdsup.voicechat.core.messages.AudioPacket;

import java.io.*;

public class AudioSenderThread extends Thread{
    Main app;
    ObjectOutputStream outputStream;

    public AudioSenderThread(Main app, ObjectOutputStream outputStream) throws IOException {
        super();
        this.app = app;
        this.outputStream = new ObjectOutputStream(app.getServer().getOutputStream());
    }

    @Override
    public void run(){
        ByteArrayInputStream in;

        while(true){ // пока комната активна
            byte[] byteArr = app.byteArrayOutputStream.toByteArray();
            if(byteArr.length == 0) // если пустой ждем дальше
                continue;
            in = new ByteArrayInputStream(byteArr);byte[] temp = new byte[AudioPacket.defailtDataLenght];
            while(in.available() >= AudioPacket.defailtDataLenght){
                try {
                    in.read(temp); // читаем
                    Main.LOGGER.info("Отправил пакет");
                    Message ap = new Message(new AudioPacket(app.getClientId(), temp));
                    outputStream.writeObject(ap);
                    outputStream.flush();
                } catch (IOException e) { // недоступно подключение
                    e.printStackTrace();
                }
            }
        }
    }
}