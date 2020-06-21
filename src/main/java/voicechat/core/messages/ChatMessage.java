package voicechat.core.messages;

import java.io.Serializable;

public class ChatMessage implements Serializable {
    private long clId;
    private String login;
    private String text;
    public ChatMessage(long chId, String text, String login){
        this.clId = clId;
        this.text = text;
        this.login = login;
    }

    public String getLogin() {
        return login;
    }

    public long getClId() {
        return clId;
    }

    public String getText() {
        return text;
    }
}
