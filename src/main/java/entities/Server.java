package entities;

import org.apache.commons.codec.digest.DigestUtils;
import utils.CommandList;
import utils.Connection;
import utils.Registration;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    String login = null;
    String password = null;
    // прослушиваемый порт
    static final int PORT = 3443;
    // список клиентов
    //public static ArrayList<Connection> clients2 = new ArrayList<Connection>();
    public static Set<Connection> clients = new HashSet<>();

    // сокет клиента
    Socket clientSocket = null;
    // серверный сокет
    ServerSocket serverSocket = null;
    // ответы сервера
    PrintWriter out = null;
    // прием сервера
    BufferedReader in = null;


    Map<String, Connection> regUsers = new HashMap<>();
    private static CommandList commandList;
    private static ArrayList<String> checkCommand;

    static {
        commandList = new CommandList();
        commandList.add("/i - Info");
        commandList.add("/world - Send in general chat");
        commandList.add("/pm - Send in personal chat");
        commandList.add("/add - Add friend");
        commandList.add("/f - Friends");
        commandList.add("/o - Online");
        commandList.add("/h or /? - Help");

        checkCommand = new ArrayList<>();
        for(String com : commandList) {
            checkCommand.add(com.substring(1, 2));
        }

    }


    public Server() {

        try {
            // создаём серверный сокет на определенном порту
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server started");
            // запускаем бесконечный цикл
            while (true) {
                // ожидание подключений
                clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress() + " //" + clientSocket.getPort());
                Registration registration = new Registration(clientSocket, this);
                new Thread(registration).start();
            }
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
        finally {
            try {
                // закрываем подключение
                clientSocket.close();
                System.out.println("Сервер остановлен");
                serverSocket.close();
            }
            catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void addFriendForMe(String login, Connection connection) {
        if(regUsers.containsKey(login)) {
            connection.getFriendsList().add(regUsers.get(login));
            connection.sendMsg("Пользователь " + login + "добавлен в ваш список друзей");
        } else connection.sendMsg("Пользователь не найден");
    }

    public static String encrypt(String st) {
        String md5Hex = DigestUtils.md5Hex(st);

        return md5Hex;
    }

    public String getCommands() {

        return "Список доступных команд:\n" + commandList.toString();
    }

    public void receiveMsgToYourself(Connection con, String msg) {
        con.sendMsg(msg);

    }

    public void receiveXmlMsgToYourself(Connection con, Message message) {
        con.sendXmlTo(message);

    }


    public Connection getCon(String login) {
        return regUsers.get(login);
    }

    public void sendMsgToAllClients(String msg) {
        for (Map.Entry<String, Connection> entry : regUsers.entrySet()) {
            entry.getValue().sendMsg(msg);
        }
    }

    public void sendXmlMsgToAllClients(Message message) {
        for (Map.Entry<String, Connection> entry : regUsers.entrySet()) {
            entry.getValue().sendXmlTo(message);
        }
    }



    public void sendXmlMsgToSpecificClients(String login, Message message, Connection connection) {
        if(!clients.contains(regUsers.get(login))) {
            if(!regUsers.containsKey(login)) {
                connection.sendMsg("Пользователь с ником " + login + " не зарегестрирован");
            } else {
                connection.sendMsg("Пользователь с ником " + login + " не онлайн");
            }
        } else {
            regUsers.get(login).sendXmlTo(message);
            connection.sendMsg("Сообщение отправлено");
        }
    }

    public boolean getAuthority(String login, String password) {
        if(regUsers.get(login).getPassword().equals(encrypt(password))) {
            clients.add(regUsers.get(login));
            Connection.incrementClientsCount();
            System.out.println("Пользователь " + login + " зашел на сервер");
            return true;
        } else {
            System.out.println("Неверный пароль");
            return false;

        }
    }

    // проверка регистрации
    public boolean checkReg(String login) {
        if (regUsers.get(login) != null) {
            return true;
        }else {
            return false;
        }
    }
    // добавление юзера
   synchronized public void addRegUser(String login, String password) {
        regUsers.put(login, new Connection(login, encrypt(password)));
       System.out.println("Пользователь " + login + " зарегестрирован");
    }

    // удаляем клиента из коллекции при выходе из чата
    public void removeClient(Connection client) {
        clients.remove(client);
    }

    // Добавляем клиента в список онлайн
    public static void addClient(Connection client) {
        clients.add(client);
    }

    public String getOnlineList() {
        String online = "";
       for(Connection con : clients) {
           online += con.getLogin().concat("\n");
       }
       return online;
    }

    public String checkCommand(String msg) {
        if(checkCommand.contains(msg.substring(0, 1))) {
            return msg;
        } else {
            return "Неизвестная команда";
        }
    }

    public String returnTask(String command, Connection connection) {
        switch (command) {
            case "i":
                command = this.toString();
                break;
            case "world":
                command = "world";
                break;
            case "pm":
                command = "pm";
                break;
            case "add":
                command = "add";
                break;
            case "f":
                command = connection.friendsList();
                break;
            case "o":
                command = this.getOnlineList();
                break;
            case "h":
                command = this.getCommands();
                break;
            default:
                command = "Неизвестная команда";
        }
        return command;
    }

    @Override
    public String toString() {
        return "Server:\n" + serverSocket.getInetAddress() + "//" + serverSocket.getLocalPort() + "\n";
    }
}
