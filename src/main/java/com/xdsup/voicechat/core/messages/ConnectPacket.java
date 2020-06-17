package com.xdsup.voicechat.core.messages;

import java.io.Serializable;

public class ConnectPacket implements Serializable {
    public enum Status{ OK, NOTCORRECT, AUTHREQUIRED;}

    private String login;
    private String password;
    private Status status; // для ответа сервера
    private long clientId;

    public ConnectPacket(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public ConnectPacket(Status status) {
        this("","");
        this.status = status;
    }

    public ConnectPacket(Status status, long clientId) {
        this(status);
        this.clientId = clientId;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public Status getStatus() {
        return status;
    }

    public long getClientId() {
        return clientId;
    }

    public void setClientId(long clientId) {
        this.clientId = clientId;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void reset(){
        login = "";
        password = "";
    }
}
