package com.xdsup.voicechat.core.messages;

import java.io.Serializable;

public class ConnectPacket implements Serializable {
    public enum Status{ OK, NOTCORRECT, AUTHREQUIRED;}

    private String login;
    private String password;
    private Status status; // для ответа сервера

    public ConnectPacket(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public ConnectPacket(Status status) {
        this("","");
        this.status = status;
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

    public void setStatus(Status status) {
        this.status = status;
    }

    public void reset(){
        login = "";
        password = "";
    }
}
