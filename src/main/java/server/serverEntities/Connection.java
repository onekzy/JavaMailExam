package server.serverEntities;

import org.apache.log4j.Logger;
import utils.message.Message;
import server.release.TcpServer;
import org.xml.sax.SAXException;
import utils.message.impl.Command;
import utils.message.impl.Details;
import utils.message.impl.MessageXml;
import utils.parser.impl.JaxbParser;
import utils.factory.MessageFactory;
import utils.factory.ParserProvider;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.*;
import java.net.Socket;
import java.util.*;

public class Connection implements Runnable {
    // initialization of logger
    private static final Logger log = Logger.getLogger(Connection.class);
    // user status
    public String status;
    // server prefix
    String prefix;
    // server instance
    private TcpServer server = null;
    // friend list
    private Set<Connection> friends = new HashSet<>();
    // client socket
    private Socket clientSocket = null;
    // number of clients online
    private static volatile int clients_count = 0;

    String login, password;

    BufferedReader in = null;
    BufferedWriter out = null;
    StringReader stringReader = null;
    // parser xml messages
    JaxbParser jaxbParser = null;



    public Connection(String login, String password) {
        incrementClientsCount();
        this.status = "Online";
        this.login = login;
        this.password = password;
        TcpServer.addClient(this);
    }

    public void run() {
        try {
            // the server sends a welcome message
            while (true) {
                sendMsgToAll(getSystemXml("New member entered the chat!"));
                sendMsgToAll(getSystemXml("Clients in chat = " + clients_count));
                sendMsgToYourself(this, getSystemXml(server.getCommands()));
                break;
            }

            while (true) {
                String line = in.readLine();
                Command command = parseMsg(line);
                if(command.getCode() == 1) {
                    server.sendXmlMsgToAllClients(command);
                } else if(command.getCode() == 000) {
                    sendMsgToYourself(this, getSystemXml("Unknown command"));
                } else {
                    server.executeTask(command, this);
                }
        }
    } catch (IOException ex) {
            log.error("Message reception error", ex);
        }
        finally {
            this.close();
        }
    }


    // send xml message
    public void sendXmlTo(Command command) {
        try {
            StringWriter stringWriter = new StringWriter();
            jaxbParser = ParserProvider.newJaxbParser();
            jaxbParser.saveObject(stringWriter, command);
            out.write(stringWriter.toString());
            out.newLine();
            out.flush();
        } catch (IOException ex) {
            log.error("Error sending message", ex);
        } catch (JAXBException ex) {
            log.error("Marshalling error", ex);
        } catch (SAXException ex) {
            log.error("Error parser initialization", ex);
        }
    }

    public String getSystemXml(String message) {
        return server.getSystemXml(message);
    }

    public Command parseMsg(String raw) {
        return server.parseMsg(raw);
    }


    public void sendMsg(String string) {
        try {
            out.write(string);
            out.newLine();
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // add user to friend list
    public void addFriend() {
        sendMsgToYourself(this, getSystemXml("Enter the login name of the user who will be added to the friend list"));
        try {
            server.addFriendForMe(server.parseMsgBody(in.readLine()), this);
        } catch (IOException ex) {
            log.error("Message reception error", ex);
        }
    }



    // send a string message to all users
    public void sendMsgToAll(String msg) {
        server.sendMsgToAllClients(msg);
    }

    // sending a message to a specific user
    public void sendMsgToYourself(Connection connection, String msg) {
        server.receiveMsgToYourself(connection, msg);
    }

    // get a friend list
    public String friendsList(){
        String line = "";
        Iterator<Connection> iterator = friends.iterator();
        if(!friends.isEmpty()) {
            while (iterator.hasNext()) {
                Connection connection = iterator.next();
                line += connection.login.concat(" - " + connection.getStatus()).concat("***");
            }
        } else {
            line = "No friends";
        }
            return line;

    }


    // get a chatting history
    public void getHistory() {
        sendMsgToYourself(this, getSystemXml("Enter the name of the user with whom you want to see the chatting history"));
        String to = null;
        try {
            to = server.parseMsgBody(in.readLine());
        } catch (IOException ex) {
            log.error("Message reception error", ex);
        }
        List<Command> tempArr = server.getChattingHistory(this, to);
        if(tempArr.isEmpty()) {
            sendMsgToYourself(this, getSystemXml("Chatting history is empty"));
        } else {
            for (int i = 0; i < tempArr.size(); i++) {
                this.sendXmlTo(tempArr.get(i));
            }
        }
    }


    // client exits chat
    public void close() {
        // delete the client from the list online
        server.removeClient(this);
        this.status = "Offline";
        decrementClientsCount();
        server.sendMsgToAllClients(prefix + "Clients in chat = " + clients_count);
        try {
            clientSocket.close();
        } catch (IOException ex) {
            log.error("Connection close error", ex);
        }
    }

    // primary connection setup
    public void onSetup() throws IOException {
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        stringReader = new StringReader("");
        prefix = server.getPrefix();
    }
    public void setStatus() {
        sendMsgToYourself(this, getSystemXml("Enter status:"));
        String status = null;
        try {
            status = server.parseMsgBody(in.readLine());
        } catch (IOException ex) {
            log.error("Message reception error", ex);
        }
        this.status = status;
        sendMsgToYourself(this, getSystemXml("Your new status: " + this.getStatus()));
    }

    public String getStatus() {
        return status;
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

    // увеличить число клиентов онлайн
    public static void incrementClientsCount() {
        clients_count++;
    }

    // уменьшить число клиентов онлайн
    public static void decrementClientsCount() {
        if(clients_count >= 1)
            clients_count--;
    }
}
