package voicechat.client;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Semaphore;

import javafx.application.Platform;

public class LocalScanner extends Thread {
    private static final int servPort = 35000;
    ClientDesktop app;
    private int port;
    Semaphore mutex = new Semaphore(0);

    public LocalScanner(ClientDesktop app) {
        this.app = app;
    }

    class Scanner extends Thread {
        @Override
        public void run() {
            try {
                Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            } catch (SocketException ex) {
                ClientDesktop.LOGGER.warning("Network error");
            }

            Enumeration<NetworkInterface> net = null;
            List<InetAddress> broadcastList = new ArrayList<>();
            try {
                net = NetworkInterface.getNetworkInterfaces();

                while (net.hasMoreElements()) {
                    NetworkInterface networkInterface = net.nextElement();

                    if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                        continue;
                    }
                    networkInterface.getInterfaceAddresses().stream()
                            .map(a -> a.getBroadcast())
                            .filter(Objects::nonNull)
                            .forEach(broadcastList::add);
                }
                for (InetAddress ip : broadcastList) {
                    ClientDesktop.LOGGER.info(ip.toString());
                }
            } catch (SocketException e) {
                ClientDesktop.LOGGER.warning("Not connected to any network");
            }
            ClientDesktop.LOGGER.warning("I find all");

            try (DatagramSocket socket = new DatagramSocket()) {
                //while (true)
                port = socket.getLocalPort();
                mutex.release();
                for (InetAddress ip : broadcastList) {
                    try {
                        ClientDesktop.LOGGER.info("Check " + ip.toString());
                        socket.connect(ip, servPort);
                        String request = "Are you is voice chat?";
                        DatagramPacket datagramPacket = new DatagramPacket(
                                request.getBytes(),
                                request.getBytes().length
                        );
                        socket.send(datagramPacket);
                    } catch (IOException e) { // не отпралось
                        ClientDesktop.LOGGER.info("Not sended " + ip.toString());
                        //e.printStackTrace();
                    }
                }
            } catch (SocketException e) { // не открыли
                e.printStackTrace();
            }
            ClientDesktop.LOGGER.info("Scanner close");
        }
    }

    class Receiver extends Thread {
        String serverPort;

        @Override
        public void run() {
            try {
                mutex.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ClientDesktop.LOGGER.info("Receiver started on " + port);
            boolean finded = false;
            try (ServerSocket socket = new ServerSocket(port)) {
                socket.setSoTimeout(100000);
                app.servers.clear();
                try {
                    while (true) {
                        Socket server = socket.accept(); // ждем подключений
                        DataInputStream in = new DataInputStream(server.getInputStream());
                        serverPort = null;
                        Thread.sleep(10);
                        while (in.available() > 0) { // ждем чего-нибудь
                            serverPort = Integer.toString(in.readInt());
                        }
                        in.close();
                        ClientDesktop.LOGGER.info(app.servers.size() + "Answer!!! " + server.getInetAddress() + " " + serverPort);
                        if (serverPort == null) {
                            continue;
                        }
                        finded = true;
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                app.servers.add(
                                        // это нужно чтобы убрать некрасивую палочку при выводе
                                        server.getInetAddress().toString().substring(1)
                                                + " " + serverPort);
                                //app.loginController.serversList.refresh();
                            }
                        });
                    }
                } catch (SocketTimeoutException e) {
                    ClientDesktop.LOGGER.warning("Receiver close by timeout");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) { // не открылся
                e.printStackTrace();
            }
            if(!finded){
                app.loginController.status = Status.SERVERS_NOT_FOUND;
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        app.loginController.labStatus.setText(app.loginController.status.text);
                    }
                });

            }
            ClientDesktop.LOGGER.info("Receiver close ");
        }
    }

    @Override
    public void run() {
        new Scanner().start();
        new Receiver().start();
    }
}
