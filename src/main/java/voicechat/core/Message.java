package voicechat.core;

import java.io.Serializable;

public class Message implements Serializable {

    public final long timeStamp; // время, когда отправлен пакет
    public final static long TTL = 2000; // время жизни сообщения
    public final Object data; // данные сообщения

    public Message(Object data) {
        this.timeStamp = System.currentTimeMillis();
        this.data = data;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public long getTTL() { return TTL; }

    public Object getData() {
        return data;
    }
}
