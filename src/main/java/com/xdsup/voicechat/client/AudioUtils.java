package com.xdsup.voicechat.client;

import javax.sound.sampled.AudioFormat;

public class AudioUtils {
    //Этот метод создает и возвращает
    // объект AudioFormat

    public static AudioFormat getAudioFormat(){
        float sampleRate = 8000.0F;
        //8000,11025,16000,22050,44100
        int sampleSizeInBits = 16;
        //8,16
        int channels = 1;
        //1,2
        boolean signed = true;
        //true,false
        boolean bigEndian = false;
        //true,false
        return new AudioFormat(
                sampleRate,
                sampleSizeInBits,
                channels,
                signed,
                bigEndian);
    }
}
