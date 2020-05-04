package com.xdsup.voicechat.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.Queue;

public class Connector {

    public DatagramSocket socket;
    Queue<DatagramPacket> messages;

    boolean connected;
    boolean recipientAnswered;
    // адрес отправителя
    int port;

    // адрес получателя
    InetAddress addressRecipient;
    int portRecipient;

    public Connector(){
        //this.port = port;
        connected = false;
    }

    public void connectTo(InetAddress address, int port) throws IOException{
        connectTo(address, port, "", "");
    }

    public void connectTo(InetAddress address, int port, String login, String password) throws IOException{
        this.addressRecipient = address;
        this.portRecipient = port;
        int packetNumber = 0;

    }

    public boolean isConnected() {
        return connected;
    }

    public boolean isRecipientAnswered() {
        return recipientAnswered;
    }
}
