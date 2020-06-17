package com.xdsup.voicechat.client;

import com.xdsup.voicechat.core.Utils;
import com.xdsup.voicechat.core.messages.AudioPacket;

import javax.sound.sampled.*;
import javax.xml.transform.Source;
import java.util.ArrayDeque;
import java.util.ArrayList;

// канал куда воспроизводятся для каждого
public class AudioChannel extends Thread{
    Main app;
    private long chId; //уникальный айди выдает сервер
    private ArrayDeque<AudioPacket> queue = new ArrayDeque<>(); // то что будем проигрывать
    private int lastSoundPacketLen = AudioPacket.defailtDataLenght;
    private long lastPacketTime = System.currentTimeMillis();

    // true - если давно не было сообщений
    public boolean canKill(){
        return System.currentTimeMillis() - lastPacketTime > 5000000000L;
    }

    public void closeAndKill() {
        Main.LOGGER.info("AudioChanel "+ chId +" removed and stopped");
        app.audioChannels.remove(chId);
        if (speaker != null) {
            speaker.close();
        }
        stop();
    }

    private SourceDataLine speaker = null; // поток вывода звука

    public AudioChannel(Main app, long clId){
        super();
        this.app = app;
        chId = clId;
    }

    public void addAudioPacket(AudioPacket audioPacket){
        queue.add(audioPacket);
    }

    @Override
    public void run() {
        try{
            Main.LOGGER.info("AudioChanel started " + chId);
            AudioFormat af = AudioPacket.defaultFormat;
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, af);
            speaker = (SourceDataLine) AudioSystem.getLine(info);
            speaker.open(af);
            speaker.start();

            for (;;){
                if(queue.isEmpty()) {// нечего проигрывать
                    Utils.sleep(10);
                    continue;
                }
                if(canKill()){ // если нет долго от этого клиента сообщений
                    // прекращаем слушать сообщения
                    break;
                }
                if(!app.audioOn){ // если звук выключен, чистим все что приходило раньше.
                    queue.clear();
                    Utils.sleep(10);
                    continue;
                }
                else {
                    lastPacketTime = System.currentTimeMillis();
                    AudioPacket audioPacket = queue.pop();
                    //TODO: расжатие аудио
                    byte[] dataToPlay = audioPacket.getData();
                    lastSoundPacketLen = dataToPlay.length;
                    speaker.write(dataToPlay, 0, dataToPlay.length);
                }
            }
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
        finally {
            closeAndKill();
        }
    }
}
