package server.serverEntities;

import org.apache.log4j.Logger;
import server.release.TcpServer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Registration implements Runnable {
    // server prefix
    private String prefix;
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

    private PrintWriter out;
    private BufferedReader in;

    public Registration(Socket clientSocket, TcpServer server) {
        this.clientSocket = clientSocket;
        this.server = server;
        prefix = server.getPrefix();
    }

    @Override
    public void run() {
        try {

            // initialization of i/o streams
            out = new PrintWriter(clientSocket.getOutputStream(), true);
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

    // enter chat, perform registration or login
    public void getAccept() throws IOException {
        out.println(prefix + "Enter login:");
        login = in.readLine().trim();
        out.println(prefix + "Enter password:");
        password = in.readLine().trim();

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
            out.println(prefix + "Wrong password");
        }
    }

    // registration check
    public void doRegistration(String login, String password) throws IOException {
        out.println(prefix + "Confirm the password:");
        confirmPassword = in.readLine();
        if(password.equals(confirmPassword)) {
            server.addRegUser(login, password);
            out.println(prefix = "You, " + login + ", are registered. Press 'Enter' to enter chat");
            // registration has passed
            regFlag = true;
        } else {
            out.println(prefix + "Passwords do not match");
        }
    }

    // authentication check
    public void doAuthentication(String login) {
        out.println(prefix + "You, " + login + ", are joined. Press 'Enter' to enter chat");
        // authentication has passed
        regFlag = true;
    }
}
