package server.release;

import utils.message.impl.MessageXml;
import org.apache.commons.codec.digest.DigestUtils;
import server.Server;
import server.serverUtils.CommandList;
import server.serverEntities.Connection;
import server.serverEntities.Registration;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TcpServer implements Server {
    private String login = null;
    private String password = null;
    // прослушиваемый порт
    public static final int PORT = 3443;
    // список клиентов онлайн
    private static Set<Connection> clientsOnline;
    // хранилище зарегистрированых пользователей
    private static Map<String, Connection> regUsers;
    // список команд сервера
    private static CommandList commandList;
    // сокет клиента
    private Socket clientSocket = null;
    // серверный сокет
    private ServerSocket serverSocket = null;
    // ответы сервера
    private PrintWriter out = null;
    // прием сервера
    private BufferedReader in = null;
    // запускатр потоков
    private static ExecutorService executorService = null;

    // статическая инициализация сервера
    static {
        executorService = Executors.newCachedThreadPool();
        clientsOnline = new HashSet<>();
        regUsers = new HashMap<>();
        commandList = new CommandList();
        commandList.add("/i - Info");
        commandList.add("/world - Send in general chat");
        commandList.add("/pm - Send in personal chat");
        commandList.add("/add - Add friend");
        commandList.add("/f - Friends");
        commandList.add("/o - Online");
        commandList.add("/h - Help");
    }


    public TcpServer() {

        try {
            // создаём серверный сокет на определенном порту
            serverSocket = new ServerSocket(PORT);
            System.out.println("server running...");

            // запускаем бесконечный цикл прослушивания
            while (true) {
                // ожидание подключений
                clientSocket = serverSocket.accept();
                System.out.println("ClientConsole connected: " + clientSocket.getInetAddress() + " //" + clientSocket.getPort());
                // создание потока регистрации
                Registration registration = new Registration(clientSocket, this);
                // запуск потока регистрации
                executorService.execute(registration);
            }
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
        finally {
            try {
                // закрываем подключение
                executorService.shutdown();
                clientSocket.close();
                System.out.println("Сервер остановлен");
                serverSocket.close();
            }
            catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    // добавление пользователя в список друзей
    public void addFriendForMe(String login, Connection connection) {
        if(regUsers.containsKey(login)) {
            connection.getFriendsList().add(regUsers.get(login));
            connection.sendMsg("Пользователь " + login + " добавлен в ваш список друзей");
        } else connection.sendMsg("Пользователь не найден");
    }

    // шифрование строки
    public static String encrypt(String st) {
        String md5Hex = DigestUtils.md5Hex(st);
        return md5Hex;
    }

    // получение списка команд сервера
    public String getCommands() {
        return "Список доступных команд:\n" + commandList.toString();
    }

    // отправка xml конкретному пользователю
    public void receiveMsgToYourself(Connection con, String msg) {
        con.sendMsg(msg);
    }

    // получение пользователя из списка зарегистрированых клиентов
    public Connection getCon(String login) {
        return regUsers.get(login);
    }

    // отправка сообщения всем пользователям
    public void sendMsgToAllClients(String msg) {
        for (Map.Entry<String, Connection> entry : regUsers.entrySet()) {
            entry.getValue().sendMsg(msg);
        }
    }

    // отправка xml всем пользователям
    public void sendXmlMsgToAllClients(MessageXml message) {
        for (Map.Entry<String, Connection> entry : regUsers.entrySet()) {
            entry.getValue().sendXmlTo(message);
        }
    }

    // отравка xml конкретному пользователю
    public void sendXmlMsgToSpecificClients(String login, MessageXml message, Connection connection) {
        if(!clientsOnline.contains(regUsers.get(login))) {
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

    // проверка авторизации
    public boolean getAuthority(String login, String password) {
        if(regUsers.get(login).getPassword().equals(encrypt(password))) {
            clientsOnline.add(regUsers.get(login));
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
        clientsOnline.remove(client);
    }

    // добавляем клиента в список онлайн
    public static void addClient(Connection client) {
        clientsOnline.add(client);
    }

    // получение списка пользователей онлайн
    public String getOnlineList() {
        String online = "";
       for(Connection con : clientsOnline) {
           online += con.getLogin().concat("\n");
       }
       return online;
    }

    // выполнение команды
    public void executeTask(String command, Connection connection) {
        switch (command) {
            case "/i":
                connection.sendMsgToYourself(connection, this.toString());
                break;
            case "/world":
                connection.sendXmlMsgToAllClients();
                break;
            case "/pm":
                connection.sendXmlMsgToSpecificClients();
                break;
            case "/add":
                connection.addFriend();
                break;
            case "/f":
                connection.sendMsgToYourself(connection, connection.friendsList());
                break;
            case "/o":
                connection.sendMsgToYourself(connection, this.getOnlineList());
                break;
            case "/h":
                connection.sendMsgToYourself(connection, this.getCommands());
                break;
            default:
                connection.sendMsgToYourself(connection, "Неизвестная команда");
        }
    }

    @Override
    public String toString() {
        return "server:\n" + serverSocket.getInetAddress() + "//" + serverSocket.getLocalPort() + "\n";
    }
}
