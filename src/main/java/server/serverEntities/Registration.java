package server.serverEntities;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;
import server.release.TcpServer;
import utils.factory.ParserProvider;
import utils.parser.impl.JaxbParser;

import java.io.*;
import java.net.Socket;

public class Registration implements Runnable {
    // initialization of logger
    private static final Logger log = Logger.getLogger(Registration.class);
    private String login;
    private String password;
    private String confirmPassword;
    private Socket clientSocket;
    // server instance
    private TcpServer server;
    // reg/login check loop flag
    private boolean regFlag = false;
    // parser
    private JaxbParser jaxbParser = null;

    private BufferedWriter out;
    private BufferedReader in;

    public Registration(Socket clientSocket, TcpServer server) throws SAXException {
        this.clientSocket = clientSocket;
        this.server = server;
        this.jaxbParser = ParserProvider.newJaxbParser();

    }

    @Override
    public void run() {
        try {

            // initialization of i/o streams
            out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            // registration or authentication
            do {
                this.getAccept();
            } while (!regFlag);

            // create and configure connections
            Connection connection = server.getCon(login);
            connection.setServer(this.server);
            connection.setClientSocket(this.clientSocket);
            // setup the i/o streams for the connection
            connection.onSetup();

            // start connection thread
            new Thread(connection).start();

        } catch (IOException ex) {
            log.error("Message reception error", ex);
        }
    }

    public String getSystemXml(String message)  {
        return server.getSystemXml(message);
    }


    public String parseMsgBody(String raw) {
        return server.parseMsgBody(raw);
    }

    public void sendSystemMsg(String string) throws IOException {
        out.write(string);
        out.newLine();
        out.flush();
    }


    // enter chat, perform registration or login
    public void getAccept() throws IOException {
        sendSystemMsg(getSystemXml("Enter login:"));
        login = parseMsgBody(in.readLine());
        sendSystemMsg(getSystemXml("Enter password:"));
        password = parseMsgBody(in.readLine());

        // if user is not registered
        if(!server.checkReg(login)) {
            // registration
            doRegistration(login, password);
            // if already registered
        } else if (server.getAuthority(login, password)) {
            // authenticate
            doAuthentication(login);
        } else {
            // incorrect input
            sendSystemMsg(getSystemXml("Wrong password"));
        }
    }

    // registration check
    public void doRegistration(String login, String password) throws IOException {
        sendSystemMsg(getSystemXml("Confirm the password:"));
        confirmPassword = parseMsgBody(in.readLine());
        if(password.equals(confirmPassword)) {
            server.addRegUser(login, password);
            sendSystemMsg(getSystemXml("You, " + login + ", are registered. Welcome to our chat!"));
            // registration has passed
            regFlag = true;
        } else {
            sendSystemMsg(getSystemXml("Passwords do not match"));
        }
    }

    // authentication check
    public void doAuthentication(String login) throws IOException {
        sendSystemMsg(getSystemXml("You, " + login + ", are joined. Welcome to our chat!"));
        // authentication has passed
        regFlag = true;
    }
}
