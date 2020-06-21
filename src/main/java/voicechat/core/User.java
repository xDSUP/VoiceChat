package voicechat.core;

import java.io.Serializable;

public class User implements Serializable {
    private String login;
    private String name;

    public User(String login){
        this.login = login;
    }

    public User(String login, String name){
        this.login = login;
        this.name = name;
    }

    public String getLogin() {
        return login;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLogin(String login) {
        this.login = login;
    }
}

