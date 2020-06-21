package voicechat.server;

import voicechat.core.User;

import java.io.*;
import java.util.HashMap;

public class DataBase implements Serializable {
    private class Elem implements Serializable{
        User user;
        String password;
        Elem(User user, String password){
            this.user = user;
            this.password = password;
        }
    }
    private HashMap<String, Elem> dataBase;

    public DataBase(){
        dataBase = new HashMap<>();
    }

    // создает нового пользователя
    public void addNewUser(User user, String password){
        dataBase.put(
                user.getLogin(),
                new Elem(user, password)
        );
    }

    public User getUser(String login){
        return dataBase.get(login).user;
    }

    // вернет результат сравнения паролей
    public boolean checkPasswordByUser(String login, String pass){
        Elem temp = dataBase.get(login);
        return temp.password.equals(pass);
    }

    // проверяет наличие пользователя в базе
    // true - если есть такой
    public boolean checkUser(String login){
        return dataBase.containsKey(login);
    }

    // сохраняет бд в файл
    public void save(){
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream("/database"))){
            objectOutputStream.writeObject(dataBase);
            objectOutputStream.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean load(){
        try (ObjectInputStream objectOutputStream = new ObjectInputStream(new FileInputStream("/database"))){
            dataBase = (HashMap<String, Elem>)objectOutputStream.readObject();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
