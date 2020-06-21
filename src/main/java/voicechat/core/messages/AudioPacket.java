package voicechat.core.messages;

import voicechat.client.AudioUtils;

import javax.sound.sampled.AudioFormat;
import java.io.*;

public class AudioPacket implements Serializable {
    public static int defailtDataLenght = 900; // длина данных звука
    // формат данных звука
    public static AudioFormat defaultFormat= AudioUtils.getAudioFormat();
    private final static long TTL = 2000; // время жизни звукового пакета

    private final long clientId; // универсальный номер каждого клиента
    private byte[] data; // данные звука
    private final long timeStamp; // время, когда отправлен пакет

    public AudioPacket(long clId, byte[] data){
        timeStamp = System.currentTimeMillis();
        this.clientId = clId;
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }

    public static AudioPacket getAudioPacket(byte[] byteArray){
        AudioPacket result = null;
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(byteArray));
            result = (AudioPacket) objectInputStream.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return result;
    }

    public byte[] getByteArray(){
        byte[] temp;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            ObjectOutputStream objectInputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectInputStream.writeObject(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return byteArrayOutputStream.toByteArray();
    }

    public long getClientId() {
        return clientId;
    }

    public long getTimeStamp() {
        return timeStamp;
    }
}
