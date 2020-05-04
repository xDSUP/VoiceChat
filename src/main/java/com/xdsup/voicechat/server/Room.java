package com.xdsup.voicechat.server;


import java.net.DatagramSocket;
import java.util.ArrayList;

public class Room{
    public final int id;

    // люди в комнате
    ArrayList<ClientConnection> persons;
    // удп сокет нашей комнаты
    DatagramSocket socket;
    //
    // название комнаты
    String title;

    Room(int id, String title){
        this.id = id;
        this.title = title;
        persons = new ArrayList<>();
        id++;
    }

    // подключить к комнате нового человека
    public void connectNewPerson(ClientConnection person){
        persons.add(person);
    }

    public void disconnectPerson(ClientConnection person){
        for (ClientConnection p: persons) {
            if(p == person){
                persons.remove(p);
            }
        }
    }
}
