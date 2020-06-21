package voicechat.core.messages;

import java.io.Serializable;

public class ConnectPacket implements Serializable {
    public enum Status{
        OK, // сервер получил
        NOTCORRECT,  // некоректный логин или пароль
        AUTHREQUIRED, // требуется логин и пароль
        USERAUTH, // пользователь уже здесь
        CONNECT, // для проверки доступности сервера
        AUTH, // запрос от клиента на авторизацию
        REGISTER // запрос от клиента на регистрацию
    }
    private String name;
    private String login;
    private String password;
    private Status status; // для ответа сервера
    private long clientId;
    // только для авторизации
    public ConnectPacket(String login, String password) {
        status = Status.AUTH;
        this.login = login;
        this.password = password;
    }
    // только для запроса на регистрацию
    public ConnectPacket(String login, String password, String name) {
        status = Status.REGISTER;
        this.login = login;
        this.password = password;
        this.name = name;
    }
    // когда нужен только статус
    public ConnectPacket(Status status) {
        this("","");
        this.status = status;
    }
    // обнуляет все поля в пакете
    public void reset(){
        login = "";
        password = "";
        name = "";
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

    public String getName() {
        return name;
    }
}
