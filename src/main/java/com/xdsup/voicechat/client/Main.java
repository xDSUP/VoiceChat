package com.xdsup.voicechat.client;

import com.xdsup.voicechat.core.Message;
import com.xdsup.voicechat.core.VoiceChatDesktop;
import com.xdsup.voicechat.core.messages.AudioPacket;
import com.xdsup.voicechat.server.ServerDekstop;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javax.sound.sampled.*;
import java.io.*;
import java.net.DatagramSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import static com.xdsup.voicechat.client.AudioUtils.getAudioFormat;

public class Main extends Application {
    private Stage primaryStage;
    private long clientId;
    public static Logger LOGGER;

    //для сервера
    private Socket socket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;

    // для аудио
    boolean microOn;
    boolean audioOn;
    ByteArrayOutputStream byteArrayOutputStream;
    ByteArrayInputStream byteArrayInputStream;
    AudioFormat audioFormat;
    TargetDataLine targetDataLine;
    AudioInputStream audioInputStream;
    SourceDataLine sourceDataLine;
    MessageHandler messageHandler;
    // храним аудиоканалы для быстрого поиска по ним
    HashMap<Long, AudioChannel> audioChannels;

    @Override
    public void start(Stage primaryStage){
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Chat()");
        showLoginWindow();
    }

    public void showLoginWindow(){
        try{
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/login.fxml"));
            Scene sceneLog = loader.load();
            LoginController loginController = loader.getController();
            loginController.setPrimaryStage(primaryStage);
            loginController.setApp(this);

            primaryStage.setScene(sceneLog);
            primaryStage.show();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    public void showRoomWindow(){
        try{
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/source.fxml"));
            Scene sceneRoom = loader.load();
            RoomController roomController = loader.getController();
            roomController.setApp(this);
            roomController.setStage(primaryStage);
            roomController.setServer(socket);

            primaryStage.setScene(sceneRoom);
            if(roomController.boxMicro.isSelected()) {
                captureAudio();
            }

            // иниц приема
            messageHandler = new MessageHandler(this);
            //messageHandler.start();
            // инициализация отправки
            //Thread audioSender = new AudioSenderThread(this, outputStream);
            //audioSender.start();
            // иниц вывода звука
            audioChannels = new HashMap<>();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        messageHandler.stop();

    }

    public static void main(String[] args) {

        File file = new File("src/main/resources/logger.config");
        try(FileInputStream ins = new FileInputStream(file)){ //полный путь до файла с конфигами
            LogManager.getLogManager().readConfiguration(ins);
            LOGGER = Logger.getLogger(ServerDekstop.class.getName());
        }catch (Exception ignore){
            ignore.printStackTrace();
        }
        launch(args);
    }

    //Этот метод захватывает аудио
    // с микрофона и сохраняет
    // в объект ByteArrayOutputStream
    public void captureAudio(){
        try{
            LOGGER.info("CAPTURE AUDIO = ON");
            //Установим все для захвата
            audioFormat = getAudioFormat();
            DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
            targetDataLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
            targetDataLine.open(audioFormat);
            targetDataLine.start();

            //Создаем поток для захвата аудио
            // и запускаем его
            //он будет работать
            //пока включен микрофон
            Thread captureThread = new Thread(new CaptureThread());
            captureThread.start();
        } catch (Exception e) {
            System.out.println(e);
            System.exit(0);
        }
    }

    //Этот метод проигрывает аудио
    // данные, которые были сохранены
    // в ByteArrayInputStream
    public void playAudio() {
        try{
            //Устанавливаем всё
            //для проигрывания
            LOGGER.info("PLAY AUDIO = ON");
            byte audioData[] = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream.reset();

            InputStream byteArrayInputStream = new ByteArrayInputStream(audioData);
            AudioFormat audioFormat = getAudioFormat();
            audioInputStream = new AudioInputStream(byteArrayInputStream, audioFormat,
                    audioData.length/audioFormat.getFrameSize());
            DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, audioFormat);
            sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
            sourceDataLine.open(audioFormat);
            sourceDataLine.start();

            //Создаем поток для проигрывания
            // данных и запускаем его
            // он будет работать пока
            // все записанные данные не проиграются

            Thread playThread = new Thread(new PlayThread());
            playThread.start();
        } catch (Exception e) {
            System.out.println(e);
            System.exit(0);
        }
    }

    //Внутренний класс для захвата
// данных с микрофона
    class CaptureThread extends Thread{
        final static double amplification = 1.0;
        byte tempBuffer[] = new byte[10000];
        public void run(){
            microOn = true;
            try{
                while(microOn){
                    while(targetDataLine.available() >= AudioPacket.defailtDataLenght){
                        byte[] buff = new byte[AudioPacket.defailtDataLenght];
                        // минимизация задержки
                        while(targetDataLine.available() >= AudioPacket.defailtDataLenght)
                            targetDataLine.read(buff, 0, buff.length);
                        // проверка громкости
                        long tot = 0;
                        for (int i = 0; i < buff.length; i++) {
                            buff[i] *= amplification;
                            tot += Math.abs(buff[i]);
                        }
                        tot *= 2.5;
                        tot /= buff.length;
                        //create and send packet
                        if (tot == 0) {//send empty packet
                            //LOGGER.info("Слишком тихо, пошлю пустое");
                            //m = new Message(new AudioPacket(clientId, null));
                        }
                        else{
                            //TODO: сжимать звук
                            LOGGER.info("Послал нормальное звук сообщение");
                            Message m = new Message(new AudioPacket(clientId, buff));
                            outputStream.writeObject(m);
                            outputStream.flush();
                        }
                    }
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                targetDataLine.stop();
                targetDataLine.close();
            }
            LOGGER.info("CAPTURE AUDIO = OFF");
        }
    }
    //===================================//

    //Внутренний класс  для
// проигрывания пришедших аудио данных
    class PlayThread extends Thread{
        byte tempBuffer[] = new byte[10000];

        public void run(){
            try{
                int cnt;
                // цикл пока не вернется -1

                while (audioOn){
                    while((cnt = audioInputStream.read(tempBuffer, 0, tempBuffer.length)) != -1){
                        if(cnt > 0){
                            //Пишем данные во внутренний
                            // буфер канала
                            // откуда оно передастся
                            // на звуковой выход
                            sourceDataLine.write(tempBuffer, 0, cnt);
                        }
                    }
                }
                sourceDataLine.drain();
                sourceDataLine.close();
            }catch (Exception e) {
                System.out.println(e);
                System.exit(0);
            }
            LOGGER.info("PLAY AUDIO = OFF");
        }
    }

    public Socket getServer() {
        return socket;
    }

    public void setServer(Socket server) {
        this.socket = server;
    }

    public long getClientId() {
        return clientId;
    }

    public void setClientId(long clientId) {
        this.clientId = clientId;
    }

    public ObjectOutputStream getOutputStream() {
        return outputStream;
    }

    public void setOutputStream(ObjectOutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public ObjectInputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(ObjectInputStream inputStream) {
        this.inputStream = inputStream;
    }
}
