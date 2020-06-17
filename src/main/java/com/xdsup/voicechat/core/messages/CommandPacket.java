package com.xdsup.voicechat.core.messages;

import java.io.Serializable;


/* используется для пересылки команд между кл и сервером
от кл исходит команда + могут быть какие-то данные
от сервера в ответ приходит таже команда и в данных
результат этой команды.
 */
public class CommandPacket implements Serializable {
    public enum Command{GET_USERS, GET_PROFILE, CHAT, REGISTER};
    private Command command; // текущая команда.
    private Object data;

    public CommandPacket(Command command){
        this(command, null);
    }

    public CommandPacket(Command command, Object data){
        this.command = command;
        this.data = data;
    }

    public Command getCommand() {
        return command;
    }

    public void setCommand(Command command) {
        this.command = command;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
