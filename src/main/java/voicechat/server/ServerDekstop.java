package voicechat.server;



import java.io.File;
import java.io.FileInputStream;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class ServerDekstop {
    static public Server server;
    public static final int PORT = 2424;

    static Logger LOGGER;
    static {
        LOGGER = Logger.getLogger(ServerDekstop.class.getName());
        LOGGER.setLevel(Level.INFO);
    }

    static {
        //server = new Server(PORT);
        //server.addNewRoom("Общая комната");
    }

    public static void main(String args[]){
        try{
            server = new Server(PORT);
            //server.addNewRoom("Общая комната");
            Thread serverThread = new Thread(server);
            serverThread.start();
        }
        catch (Exception e){
            System.out.println(e.toString());
            //throw new Exception(e);
        }

    }
}
