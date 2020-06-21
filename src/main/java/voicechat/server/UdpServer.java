package voicechat.server;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;

public class UdpServer extends Thread{
    int port;
    public UdpServer(int port) {
        super();
        this.port = port;
    }

    @Override
    public void run() {
        Server.LOGGER.info("UdpServer RUn");
        try(DatagramSocket socket = new DatagramSocket(35000)){
            byte[] buffer = new byte[10000];
            DatagramPacket request = new DatagramPacket(buffer, buffer.length);
            while (true){
                try {
                    // ждем пакеты
                    socket.receive(request);

                    byte[] data = request.getData();
                    String str = new String(data);
                    Server.LOGGER.info("Packet request: " + str);
                    String goodRequest = "Are you is voice chat?";
                    String req = str.substring(0,goodRequest.length());
                    if(!req.equals(goodRequest)){
                        continue;
                    }
                    else { // это наш клиент, ща захватим)
                        // открываем соединение
                        Server.LOGGER.info(request.getAddress() + " "+ request.getPort() +" is find ME!!!!");
                        try(Socket sock = new Socket(request.getAddress(), request.getPort())){
                            if(sock.isConnected()){
                                Server.LOGGER.info("OPEN TCP witch cl and close");
                                DataOutputStream out = new DataOutputStream(sock.getOutputStream());
                                out.writeInt(port);
                                out.flush();
                                out.close();
                            }
                            else
                                Server.LOGGER.info("Not open tcp");
                        }
                        catch (IOException e) { // не удалось чет
                            Server.LOGGER.info("Error NOt open tcp");
                            e.printStackTrace();
                        }
                    }
                }
                catch (IOException e) { // соедин закрылось
                    e.printStackTrace();
                }
            }
        } catch (SocketException e) { // не удалось открыть
            e.printStackTrace();
        }
    }
}
