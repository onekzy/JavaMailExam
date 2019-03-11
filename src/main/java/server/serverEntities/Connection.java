package server.serverEntities;

import org.apache.log4j.Logger;
import utils.message.Message;
import utils.message.impl.MessageXml;
import server.release.TcpServer;
import org.xml.sax.SAXException;
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
                sendMsgToAll(prefix + "New member entered the chat!");
                sendMsgToAll(prefix + "Clients in chat = " + clients_count + "\n");
                sendMsgToYourself(this, server.getCommands());
                break;
            }

            // input messages loop
            while (true) {
                String line = in.readLine();
                if(this.isNotEmptyLine(line)) {
                    if(this.isCommandLine(line)) {
                        // command execution
                        executeTask(line, this);
                    } else {
                        sendMsgToYourself(this,  prefix + "The command must start with '/'");
                    }
            }
        }
    } catch (IOException ex) {
            log.error("Message reception error", ex);
        }
        finally {
            this.close();
        }
    }

    //command execution on the server
    public void executeTask(String command, Connection connection) {
        server.executeTask(command, connection);
    }

    // add user to friend list
    public void addFriend() {
        server.receiveMsgToYourself(this, prefix + "Enter the login name of the user who will be added to the friend list");
        try {
            server.addFriendForMe(in.readLine(), this);
        } catch (IOException ex) {
            log.error("Message reception error", ex);
        }
    }

    // send xml messages to a specific user
    public void sendXmlMsgToSpecificClients() {
        MessageXml xmlMsg;
        try {
            xmlMsg = this.buildXmlToSpecificUser();
            server.sendXmlMsgToSpecificClients(xmlMsg.getTo(), xmlMsg, this);
            server.addMsg(xmlMsg);
        } catch (IOException ex) {
            log.error("Receive error while creating message", ex);
        }

    }

    // send xml messages to all users
    public void sendXmlMsgToAllClients() {
        MessageXml xmlMsg;
        try {
            xmlMsg = this.buildXmlToAll();
            server.sendXmlMsgToAllClients(xmlMsg);
        } catch (IOException ex) {
            log.error("Receive error while creating message", ex);
        }

    }

    // check if the string is a command
    public boolean isCommandLine(String line) {
        return String.valueOf(line.charAt(0)).equals("/");
    }

    // check if the string is not empty
    public boolean isNotEmptyLine(String line) {
        return line != null && !line.equals("") && !line.equals(" ") && !line.equals("/");
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
                line += connection.login.concat(" - " + connection.getStatus()).concat("\n");
            }
        } else {
            line = prefix + "No friends";
        }
            return line;

    }

    // creating a MessageXml object to send to all users
    public MessageXml buildXmlToAll() throws IOException {
        MessageXml message = MessageFactory.newXmlMessage();
        message.setTo("All");
        initialMsgSetup(message);
        return message;

    }

    // creating a MessageXml object to send to a specific user
    public MessageXml buildXmlToSpecificUser() throws IOException {
        MessageXml message = MessageFactory.newXmlMessage();
        server.receiveMsgToYourself(this, prefix + "Enter the recipient of the message");
        message.setTo(in.readLine());
        initialMsgSetup(message);
        return message;
    }

    // primary setup of the MessageXml object
    public void initialMsgSetup(MessageXml message) throws IOException {
        GregorianCalendar cal = new GregorianCalendar();
        XMLGregorianCalendar xmlCalendar = null;
        try {
            xmlCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
        } catch (DatatypeConfigurationException ex) {
            log.error("Error getting current time", ex);
        }
        message.setFrom(login);
        server.receiveMsgToYourself(this, prefix + "Enter a message title");
        message.setTitle(in.readLine());
        server.receiveMsgToYourself(this, prefix + "Enter a message subject");
        message.setSubject(in.readLine());
        server.receiveMsgToYourself(this, prefix + "Enter a message");
        message.setBody(in.readLine());
        message.setDate(xmlCalendar);
    }

    // get a chatting history
    public void getHistory() {
        server.receiveMsgToYourself(this, prefix + "Enter the name of the user with whom you want to see the chatting history");
        String to = null;
        try {
            to = in.readLine();
        } catch (IOException ex) {
            log.error("Message reception error", ex);
        }
        List<Message> tempArr = server.getChattingHistory(this, to);
        if(tempArr.isEmpty()) {
            server.receiveMsgToYourself(this,  prefix + "Chatting history is empty");
        } else {
            for (int i = 0; i < tempArr.size(); i++) {
                this.sendXmlTo(tempArr.get(i));
            }
        }
    }

    // send xml message
    public void sendXmlTo(Message message) {
        try {
            StringWriter stringWriter = new StringWriter();
            jaxbParser = ParserProvider.newJaxbParser();
            jaxbParser.saveObject(stringWriter, message);
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

    // send a string message
    public void sendMsg(String msg) {
        try {
            out.write(msg + "\r\n");
            out.flush();
        } catch (IOException ex) {
            log.error("Error sending message", ex);
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
        stringReader = new StringReader(in.readLine());
        prefix = server.getPrefix();
    }
    public void setStatus() {
        server.receiveMsgToYourself(this, prefix + "Enter status:");
        String status = null;
        try {
            status = in.readLine();
        } catch (IOException ex) {
            log.error("Message reception error", ex);
        }
        this.status = status;
        server.receiveMsgToYourself(this, prefix + "Your new status: " + this.getStatus());
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
