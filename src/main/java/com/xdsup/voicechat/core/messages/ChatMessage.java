package com.xdsup.voicechat.core.messages;

import java.io.Serializable;

public class ChatMessage implements Serializable {
    private long clId;
    private String text;
    public ChatMessage(long chId, String text){
        this.clId = clId;
        this.text = text;
    }

    public long getClId() {
        return clId;
    }

    public String getText() {
        return text;
    }
}
