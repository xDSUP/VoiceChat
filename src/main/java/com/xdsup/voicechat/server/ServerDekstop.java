package com.xdsup.voicechat.server;



import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class ServerDekstop {
    static public Server server;
    public static final int PORT = 2424;

    static Logger LOGGER;
    static {
        File file = new File("src/main/resources/logger.config");
        try(FileInputStream ins = new FileInputStream(file)){ //полный путь до файла с конфигами
            LogManager.getLogManager().readConfiguration(ins);
            LOGGER = Logger.getLogger(ServerDekstop.class.getName());
        }catch (Exception ignore){
            ignore.printStackTrace();
        }
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
