package com.xdsup.voicechat.client;

import com.xdsup.voicechat.core.Message;
import com.xdsup.voicechat.core.Utils;
import com.xdsup.voicechat.core.messages.AudioPacket;

import javax.sound.sampled.*;
import java.io.*;

import static com.xdsup.voicechat.client.AudioUtils.getAudioFormat;

public class AudioSenderThread extends Thread{
    public static double amplification = 1.0;
    Main app;
    ObjectOutputStream outputStream;
    private TargetDataLine mic;

    public AudioSenderThread(Main app, ObjectOutputStream outputStream) throws LineUnavailableException {
        super();
        this.app = app;
        //this.outputStream = new ObjectOutputStream(app.getServer().getOutputStream());
        this.outputStream = outputStream;
        //open microphone line, an exception is thrown in case of error
        AudioFormat af = AudioPacket.defaultFormat;
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, null);
        mic = (TargetDataLine) (AudioSystem.getLine(info));
        mic.open(af);
        mic.start();
    }

    @Override
    public void run(){
        Main.LOGGER.info("CAPTURE AUDIO THREAD START");
        try{
            while(true){
                if(!app.microOn){
                    Utils.sleep(10);
                    continue;
                }
                while(mic.available() >= AudioPacket.defailtDataLenght){
                    byte[] buff = new byte[AudioPacket.defailtDataLenght];
                    // минимизация задержки
                    while(mic.available() >= AudioPacket.defailtDataLenght)
                        mic.read(buff, 0, buff.length);
                    // проверка громкости
                    long tot = 0;
                    for (int i = 0; i < buff.length; i++) {
                        buff[i] *= amplification;
                        tot += Math.abs(buff[i]);
                    }
                    tot *= 2.5;
                    tot /= buff.length;
                    //create and send packet
                    if (tot == 0) {//send empty packet
                        //LOGGER.info("Слишком тихо, пошлю пустое");
                        //m = new Message(new AudioPacket(clientId, null));
                    }
                    else{
                        //TODO: сжимать звук
                        //LOGGER.info("Послал нормальное звук сообщение");
                        Message m = new Message(new AudioPacket(app.getClientId(), buff));
                        outputStream.writeObject(m);
                        outputStream.flush();
                    }
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            mic.stop();
            mic.close();
        }
        Main.LOGGER.info("CAPTURE AUDIO = OFF");
    }
}