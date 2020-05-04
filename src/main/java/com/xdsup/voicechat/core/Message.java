package com.xdsup.voicechat.core;

import java.io.Serializable;

public class Message implements Serializable {

    private long timeStamp; // время, когда отправлен пакет
    private final static long TTL = 2000;

    private final Object data; // данные сообщения

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
