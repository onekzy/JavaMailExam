package server.serverEntities;

import utils.message.impl.MessageXml;
import server.release.TcpServer;
import org.xml.sax.SAXException;
import utils.parser.impl.JaxbParser;
import utils.factory.MessageFactory;
import utils.factory.ParserProvider;

import javax.xml.bind.JAXBException;
import java.io.*;
import java.net.Socket;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Connection implements Runnable {
    // экземпляр нашего сервера
    private TcpServer server = null;
    // список друзей
    private Set<Connection> friends = new HashSet<>();
    // клиентский сокет
    private Socket clientSocket = null;
    // количество клиентов онлайн
    private static volatile int clients_count = 0;

    String login, password;

    BufferedReader in = null;
    BufferedWriter out = null;
    StringReader stringReader = null;
    JaxbParser jaxbParser = null;



    public Connection(String login, String password) {
        incrementClientsCount();
        this.login = login;
        this.password = password;
        TcpServer.addClient(this);
    }

    public void run() {
        try {
            // сервер отправляет приветственное сообщение
            while (true) {
                sendMsgToAll("Новый участник вошёл в чат!");
                sendMsgToAll("Клиентов в чате = " + clients_count + "\n");
                sendMsgToYourself(this, server.getCommands());
                break;
            }

            // цикл обработки входящих сообщений
            while (true) {
                String line = in.readLine();
                if(this.isNotEmptyLine(line)) {
                    if(this.isCommandLine(line)) {
                        // выполнение команды
                        executeTask(line, this);
                    } else {
                        sendMsgToYourself(this, "Команда должна начинаться с '/'");
                }
            }
        }
    }
        catch (IOException ex) {
            ex.printStackTrace();
        }
        finally {
            this.close();
        }
    }

    //выполнение команды на сервере
    public void executeTask(String command, Connection connection) {
        server.executeTask(command, connection);
    }

    // добавление пользователя в список друзей
    public void addFriend() {
        server.receiveMsgToYourself(this, "Введите логин пользователя, которого ходить добавить в список друзей");
        try {
            server.addFriendForMe(in.readLine(), this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // отправка xml сообщения конкретному пользователю
    public void sendXmlMsgToSpecificClients() {
        MessageXml xmlMsg = null;
        try {
            xmlMsg = this.buildXmlToSpecificUser();
            server.sendXmlMsgToSpecificClients(xmlMsg.getTo(), xmlMsg, this);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // отправка xml сообщения всем пользователям
    public void sendXmlMsgToAllClients() {
        MessageXml xmlMsg = null;
        try {
            xmlMsg = this.buildXmlToAll();
            server.sendXmlMsgToAllClients(xmlMsg);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // проверка является ли строка командой
    public boolean isCommandLine(String line) {
        return String.valueOf(line.charAt(0)).equals("/");
    }

    // проверка не пустая ли строка
    public boolean isNotEmptyLine(String line) {
        return line != null && !line.equals("") && !line.equals(" ") && !line.equals("/");
    }

    // отправка строкового сообщени всем пользователям
    public void sendMsgToAll(String msg) {
        server.sendMsgToAllClients(msg);
    }

    // отправка сообщения конкретному пользователю (ex. вызывающему Connection)
    public void sendMsgToYourself(Connection connection, String msg) {
        server.receiveMsgToYourself(connection, msg);
    }

    // получаем список друзей
    public String friendsList(){
        String line = "";
        Iterator<Connection> iterator = friends.iterator();
        if(!friends.isEmpty()) {
            while (iterator.hasNext()) {
                Connection connection = iterator.next();
                line += connection.login.concat("\n");
            }
        } else {
            line = "Нет друзей";
        }
            return line;

    }

    // создание объекта MessageXml для отправки всем пользователям
    public MessageXml buildXmlToAll() throws IOException {
        MessageXml message = MessageFactory.newXmlMessage();
        message.setTo("All");
        initialMsgSetup(message);
        return message;

    }

    // создание объекта MessageXml для отправки конкретному пользователю
    public MessageXml buildXmlToSpecificUser() throws IOException {
        MessageXml message = MessageFactory.newXmlMessage();
        server.receiveMsgToYourself(this, "Введите адресата сообщения");
        message.setTo(in.readLine());
        initialMsgSetup(message);
        return message;
    }

    // первичная настройка объекта MessageXml
    public void initialMsgSetup(MessageXml message) throws IOException {
        message.setFrom(login);
        server.receiveMsgToYourself(this, "Введите заголовок сообщения");
        message.setTitle(in.readLine());
        server.receiveMsgToYourself(this, "Введите тему сообщения");
        message.setSubject(in.readLine());
        server.receiveMsgToYourself(this, "Введите сообщение");
        message.setBody(in.readLine());
    }

    // отправка Xml сообщения
    public void sendXmlTo(MessageXml message) {
        try {
            StringWriter stringWriter = new StringWriter();
            jaxbParser = ParserProvider.newJaxbParser();
            jaxbParser.saveObject(stringWriter, message);
            out.write(stringWriter.toString());
            out.newLine();
            out.flush();
        } catch (JAXBException | IOException | SAXException e) {
            e.printStackTrace();
        }
    }

    // отправка строкового сообщения
    public void sendMsg(String msg) {
        try {
            out.write(msg + "\r\n");
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // клиент выходит из чата
    public void close() {
        // удаляем клиента из списка
        server.removeClient(this);
        decrementClientsCount();
        server.sendMsgToAllClients("Клиентов в чате = " + clients_count);
    }

    // первичная настройка соединения
    public void onSetup() throws IOException {
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        stringReader = new StringReader(in.readLine());
    }

    public Set<Connection> getFriendsList() {
        return friends;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {return password;}

    public void setServer(TcpServer server) {
        this.server = server;
    }

    public void setClientSocket(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public static void incrementClientsCount() {
        clients_count++;
    }

    public static void decrementClientsCount() {
        if(clients_count >= 1)
            clients_count--;
    }
}
