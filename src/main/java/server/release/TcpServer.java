package server.release;

import org.apache.log4j.Logger;
import utils.message.Message;
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
    private String prefix = "[SYSTEM:] ";
    // initialization of logger
    private static final Logger log = Logger.getLogger(TcpServer.class);
    // listening port
    private static final int PORT = 3443;
    // clients list online
    private static Set<Connection> clientsOnline;
    // registered user store
    private static Map<String, Connection> regUsers;
    // server command list
    private static CommandList commandList;
    // user correspondence history
    private static List<Message> chattingHistory;
    // client socket
    private Socket clientSocket = null;
    // server socket
    private ServerSocket serverSocket = null;
    // threads launcher
    private static ExecutorService executorService = null;

    // static server initialization
    static {
        executorService = Executors.newCachedThreadPool();
        clientsOnline = new HashSet<>();
        regUsers = new HashMap<>();
        chattingHistory = new ArrayList<>();
        commandList = new CommandList();
        commandList.add("/i - Info");
        commandList.add("/world - Send in general chat");
        commandList.add("/pm - Send in personal chat");
        commandList.add("/add - Add friend");
        commandList.add("/ss - Set status");
        commandList.add("/ch - Chatting history");
        commandList.add("/f - Friends");
        commandList.add("/o - Online");
        commandList.add("/h - Help");
        commandList.add("/exit - Leave chat");
    }


    public TcpServer() {

        try {
            // create server socket on a certain port
            serverSocket = new ServerSocket(PORT);
            log.info("Server running...");
            System.out.println("Server running...");
            // run an endless listening loop
            while (true) {
                // waiting for connections
                clientSocket = serverSocket.accept();
                log.info("Client connected: " + clientSocket.getInetAddress() + " //" + clientSocket.getPort());
                // creating a thread registration instance
                Registration registration = new Registration(clientSocket, this);
                // start registration thread
                executorService.execute(registration);
            }
        }
        catch (IOException ex) {
            log.error("Server error", ex);
        }
        finally {
            executorService.shutdownNow();
            try {
                // закрываем подключение
                clientSocket.close();
                log.info("Server stopped");
                serverSocket.close();
            }
            catch (IOException ex) {
                log.error("Server stop error", ex);
            }
        }
    }

    // getting chatting history
    public List<Message> getChattingHistory(Connection connection, String to) {
        List<Message> tempArr = new ArrayList<>();
        for(Message msg : chattingHistory) {
            if((connection.getLogin().equals(msg.getFrom()) || connection.getLogin().equals(msg.getTo()))
                    && (msg.getTo().equals(to) || msg.getFrom().equals(to))) {
                tempArr.add(msg);
            }
        }
        return tempArr;
    }

    // add user to friend list
    public void addFriendForMe(String login, Connection connection) {
        if(connection.getLogin().equals(login)) {
            connection.sendMsg(prefix + "You can’t add yourself");
        } else if(regUsers.containsKey(login)) {
            connection.getFriendsList().add(regUsers.get(login));
            connection.sendMsg(prefix + "User named " + login.toUpperCase() + " has been added to your friends list");
        } else connection.sendMsg(prefix + "User not found");
    }

    // string encryption
    public static String encrypt(String st) {
        String md5Hex = DigestUtils.md5Hex(st);
        return md5Hex;
    }

    // get a list of server commands
    public String getCommands() {
        return "List of available commands:\n" + commandList.toString();
    }

    // sending xml to a specific user
    public void receiveMsgToYourself(Connection con, String msg) {
        con.sendMsg(msg);
    }

    // get user from the list of registered clients
    public Connection getCon(String login) {
        return regUsers.get(login);
    }

    // send message to all users
    public void sendMsgToAllClients(String msg) {
        for (Map.Entry<String, Connection> entry : regUsers.entrySet()) {
            entry.getValue().sendMsg(msg);
        }
    }

    // send xml to all users
    public void sendXmlMsgToAllClients(MessageXml message) {
        for (Map.Entry<String, Connection> entry : regUsers.entrySet()) {
            entry.getValue().sendXmlTo(message);
        }
    }

    // send xml to a specific user
    public void sendXmlMsgToSpecificClients(String login, MessageXml message, Connection connection) {
        if(!clientsOnline.contains(regUsers.get(login))) {
            if(!regUsers.containsKey(login)) {
                connection.sendMsg(prefix + "User named " + login.toUpperCase() + " not registered");
            } else {
                connection.sendMsg(prefix + "User named " + login.toUpperCase() + " is not online");
            }
        } else {
            regUsers.get(login).sendXmlTo(message);
            connection.sendMsg(prefix + "Message sent");
        }
    }

    // authorization check
    public boolean getAuthority(String login, String password) {
        if(regUsers.get(login).getPassword().equals(encrypt(password))) {
            clientsOnline.add(regUsers.get(login));
            Connection.incrementClientsCount();
            regUsers.get(login).status = "Online";
            log.info("User " + login.toUpperCase() + " went to the server");
            return true;
        } else {
            log.info(login.toUpperCase() + " :Wrong password");
            return false;

        }
    }

    // registration check
    public boolean checkReg(String login) {
        if (regUsers.get(login) != null) {
            return true;
        }else {
            return false;
        }
    }

    // add correspondence messages
    public void addMsg(Message message) {
        chattingHistory.add(message);
    }

    // add user
    public synchronized void addRegUser(String login, String password) {
        regUsers.put(login, new Connection(login, encrypt(password)));
        log.info("User " + login.toUpperCase() + " is registered");
    }

    // remove client from collection when leaving the chat
    public void removeClient(Connection client) {
        clientsOnline.remove(client);
    }

    // add client to online list
    public static void addClient(Connection client) {
        clientsOnline.add(client);
    }

    // get list online
    public String getOnlineList() {
        String online = "";
       for(Connection con : clientsOnline) {
           online += con.getLogin().concat(" - " + con.getStatus()).concat("\n");
       }
       return online;
    }

    public String getPrefix() {
        return prefix;
    }

    // command execution
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
            case "/ss":
                connection.setStatus();
                break;
            case "/ch":
                connection.getHistory();
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
            case "/exit":
                connection.close();
                break;
            default:
                connection.sendMsgToYourself(connection, prefix + "Unknown command");
        }
    }

    // server information
    @Override
    public String toString() {
        return prefix + "Server:\n" + serverSocket.getInetAddress() + "//" + serverSocket.getLocalPort() + "\n";
    }
}
