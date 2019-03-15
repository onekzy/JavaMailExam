package server.release;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;
import utils.factory.ParserProvider;
import utils.message.Message;
import org.apache.commons.codec.digest.DigestUtils;
import server.Server;
import server.serverUtils.CommandList;
import server.serverEntities.Connection;
import server.serverEntities.Registration;
import utils.message.impl.Command;
import utils.message.impl.Details;
import utils.message.impl.MessageXml;
import utils.parser.impl.JaxbParser;

import javax.xml.bind.JAXBException;
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
    private static List<Command> chattingHistory;
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
        } catch (SAXException e) {
            e.printStackTrace();
        } finally {
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
    public List<Command> getChattingHistory(Connection connection, String to) {
        List<Command> tempArr = new ArrayList<>();
        for(Command cmd : chattingHistory) {
            if((connection.getLogin().equals(cmd.getDetails().getMessage().getFrom()) || connection.getLogin().equals(cmd.getDetails().getMessage().getTo()))
                    && (cmd.getDetails().getMessage().getTo().equals(to) || cmd.getDetails().getMessage().getFrom().equals(to))) {
                tempArr.add(cmd);
            }
        }
        return tempArr;
    }

    // add user to friend list
    public void addFriendForMe(String login, Connection connection) {
        if(connection.getLogin().equals(login)) {
            connection.sendMsg(getSystemXml("You can’t add yourself"));
        } else if(regUsers.containsKey(login)) {
            connection.getFriendsList().add(regUsers.get(login));
            connection.sendMsg(getSystemXml("User named " + login.toUpperCase() + " has been added to your friends list"));
        } else connection.sendMsg(getSystemXml("User not found"));
    }

    // string encryption
    public static String encrypt(String st) {
        String md5Hex = DigestUtils.md5Hex(st);
        return md5Hex;
    }

    // get a list of server commands
    public String getCommands() {
        return "List of available commands:" + commandList.toString();
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

    // sending xml to a specific user
    public void receiveMsgToYourself(Connection con, String s) {
        con.sendMsg(s);
    }

    // send xml to all users
    public void sendXmlMsgToAllClients(Command command) {
        for (Map.Entry<String, Connection> entry : regUsers.entrySet()) {
            entry.getValue().sendXmlTo(command);
        }
    }

    public String getSystemXml(String message)  {

        StringWriter stringWriter = new StringWriter();
        Command command = new Command();
        Details details = new Details();
        MessageXml messageXml = new MessageXml();
        command.setCode(777);
        messageXml.setFrom("[System]");
        messageXml.setBody(message);
        details.setMessage(messageXml);
        command.setDetails(details);
        try {
            JaxbParser jaxbParser = ParserProvider.newJaxbParser();
            jaxbParser.saveObject(stringWriter, command);
        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
        return stringWriter.toString();
    }

    public Command parseMsg(String raw) {
        Command command = null;
        StringReader stringReader = new StringReader(raw);
        try {
            JaxbParser jaxbParser = ParserProvider.newJaxbParser();
            command = (Command) jaxbParser.getObject(stringReader, Command.class);
        }catch (JAXBException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
        return command;
    }


    // send xml to a specific user
    public void sendXmlMsgToSpecificClients(String login, Command command, Connection connection) {
        if(!clientsOnline.contains(regUsers.get(login))) {
            if(!regUsers.containsKey(login)) {
                connection.sendMsg(getSystemXml("User named " + login.toUpperCase() + " not registered"));
            } else {
                connection.sendMsg(getSystemXml("User named " + login.toUpperCase() + " is not online"));
            }
        } else {
            regUsers.get(login).sendXmlTo(command);
            addMsg(command);
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
    public void addMsg(Command command) {
        chattingHistory.add(command);
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
           online += con.getLogin().concat(" - " + con.getStatus()).concat("***");
       }
       return online;
    }

    public String getPrefix() {
        return prefix;
    }

    public String parseMsgBody(String raw) {
        Command command = parseMsg(raw);

        return command.getDetails().getMessage().getBody().trim();
    }

    // command execution
    public void executeTask(Command command, Connection connection) {
        switch (command.getCode()) {
            case 11: //info DONE!
                connection.sendMsg(getSystemXml(this.toString()));
                break;
            case 12: //world

                break;
            case 13: //pm DONE!
                this.sendXmlMsgToSpecificClients(command.getDetails().getMessage().getTo(), command, connection);
                break;
            case 14: //add DONE!
                connection.addFriend();
                break;
            case 15: //ss DONE!
                connection.setStatus();
                break;
            case 16: //ch DONE!
                connection.getHistory();
                break;
            case 17: //f DONE!
                connection.sendMsg(getSystemXml(connection.friendsList()));
                break;
            case 18: //o DONE!
                connection.sendMsg(getSystemXml(this.getOnlineList()));
                break;
            case 19: //h DONE!
                connection.sendMsg(getSystemXml(this.getCommands()));
                break;
            case 21: //exit
                connection.close();
                break;
        }
    }

    // server information
    @Override
    public String toString() {
        return "Server:" + serverSocket.getInetAddress() + "//" + serverSocket.getLocalPort();
    }
}
