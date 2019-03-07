package utils;

import entities.Message;
import entities.Server;
import org.xml.sax.SAXException;
import parser.impl.JaxbParser;

import javax.xml.bind.JAXBException;
import java.io.*;
import java.net.Socket;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

public class Connection implements Runnable {
    // экземпляр нашего сервера
    private Server server = null;

    private Set<Connection> friends = new HashSet<>();

    private static final String HOST = "localhost";
    private static final int PORT = 3443;
    // клиентский сокет
    private Socket clientSocket = null;
    // количество клиента в чате, статичное поле
    private static volatile int clients_count = 0;
    // login
    String login = null;
    String password = null;
    BufferedReader in = null;
    BufferedWriter out = null;
    StringReader stringReader = null;
    //StringWriter stringWriter = null;
    //Message message = null;
    JaxbParser jaxbParser = null;



    public Connection(String login, String password) {
        incrementClientsCount();
        this.login = login;
        this.password = password;
        Server.addClient(this);
    }
    // Переопределяем метод run(), который вызывается когда
    // мы вызываем new Thread(client).start();

    public void run() {
        try {
            while (true) {
                // сервер отправляет сообщение
                server.sendMsgToAllClients("Новый участник вошёл в чат!");
                server.sendMsgToAllClients("Клиентов в чате = " + clients_count + "\n");
                server.receiveMsgToYourself(this, server.getCommands());
                break;
            }

            while (true) {
                String line = in.readLine();
                if(line != null && !line.equals("") && !line.equals(" ") && !line.equals("/")) {
                    if(String.valueOf(line.charAt(0)).equals("/")) {
                        String command = server.checkCommand(line.substring(1));
                        String taskResult = server.returnTask(command, this);
                        if(taskResult.equals("world")) {
                            Message xmlMsg = buildXmlToAll();
                            server.sendXmlMsgToAllClients(xmlMsg);
                        }else if(taskResult.equals("pm")) {
                            Message xmlMsg = buildXmlToSpecificUser();
                            server.sendXmlMsgToSpecificClients(xmlMsg.getTo(), xmlMsg, this);
                        }else if(taskResult.equals("add")) {
                            server.receiveMsgToYourself(this, "Введите логин пользователя, которого ходить добавить в список друзей");
                            server.addFriendForMe(in.readLine(), this);

                        } else {
                            server.receiveMsgToYourself(this, taskResult);
                        }
                    } else {
                        server.receiveMsgToYourself(this, "Команда должна начинаться с '/'");
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

    public Set<Connection> getFriendsList() {
        return friends;
    }

    public String getLogin() {
        return login;
    }

    public static void incrementClientsCount() {
        clients_count++;
    }

    public static void decrementClientsCount() {
        if(clients_count >= 1)
        clients_count--;
    }

    public Message buildXmlToAll() throws IOException {
        Message message = new Message();
        message.setFrom(login);
        message.setTo("All");
        server.receiveMsgToYourself(this, "Введите заголовок сообщения");
        message.setTitle(in.readLine());
        server.receiveMsgToYourself(this, "Введите тему сообщения");
        message.setSubject(in.readLine());
        server.receiveMsgToYourself(this, "Введите сообщение");
        message.setBody(in.readLine());
        return message;

    }

    public Message buildXmlToSpecificUser() throws IOException {
        Message message = new Message();
        message.setFrom(login);
        server.receiveMsgToYourself(this, "Введите адресата сообщения");
        message.setTo(in.readLine());
        server.receiveMsgToYourself(this, "Введите заголовок сообщения");
        message.setTitle(in.readLine());
        server.receiveMsgToYourself(this, "Введите тему сообщения");
        message.setSubject(in.readLine());
        server.receiveMsgToYourself(this, "Введите сообщение");
        message.setBody(in.readLine());
        return message;
    }

    public void sendXmlTo(Message message) {
        try {
            StringWriter stringWriter = new StringWriter();
            jaxbParser = new JaxbParser();
            jaxbParser.saveObject(stringWriter, message);
            out.write(stringWriter.toString());
            out.newLine();
            out.flush();
        } catch (JAXBException | IOException | SAXException e) {
            e.printStackTrace();
        }

    }

    public void sendMsg(String msg) {
        try {
            out.write(msg + "\r\n");
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public String getPassword() {
        return password;
    }

    // клиент выходит из чата
    public void close() {
        // удаляем клиента из списка
        server.removeClient(this);
        decrementClientsCount();
        server.sendMsgToAllClients("Клиентов в чате = " + clients_count);
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public void setClientSocket(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public void onSetup() throws IOException {
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        stringReader = new StringReader(in.readLine());
        //stringWriter = new StringWriter();
    }
}
