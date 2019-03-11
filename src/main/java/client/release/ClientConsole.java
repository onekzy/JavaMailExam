package client.release;

import client.Client;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;
import utils.factory.ParserProvider;
import utils.message.impl.MessageXml;
import utils.parser.impl.JaxbParser;
import server.release.TcpServer;
import client.clientEntities.ClientSender;

import javax.xml.bind.JAXBException;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;



public class ClientConsole implements Client {
    // initialization of logger
    private static final Logger log = Logger.getLogger(ClientConsole.class);
    private static final int SERVER_PORT = 3443;
    private static InetAddress localhost;
    private Socket clientSocket = null;
    private ClientSender clientSender = null;
    private Scanner scanner = new Scanner(System.in);
    private JaxbParser jaxbParser = null;
    // date format
    private SimpleDateFormat formatForDateNow = new SimpleDateFormat("hh:mm:ss E");


    public ClientConsole() {
        BufferedReader in = null;
        PrintWriter out = null;
        try {
            // get address by localhost
            localhost = InetAddress.getByName("localhost");
            // server socket connection
            clientSocket = new Socket(localhost, SERVER_PORT);
            // initialization of input / output streams
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            // receiving a string from the server about entering a login
            System.out.println(in.readLine());
            // input login
            out.println(scanner.nextLine());
            // receiving a string from the server about entering a password
            System.out.println(in.readLine());
            // input password
            out.println(scanner.nextLine());
            // creating a thread sending msg to server instance
            clientSender = new ClientSender(out, scanner);
            // start thread send
            new Thread(clientSender).start();

            // receiving msg loop
            while (true) {
                String read = in.readLine();
                // if the message is not empty
                if(read != null && !read.equals("") && !read.equals(" ")) {
                    // if the message is xml
                    if(read.charAt(0) == '<' && read.charAt(read.length()-1) == '>') {
                        // reading and displaying xml messages
                        getXmlMsg(read);
                    } else {
                        // reading and displaying a string message
                        getMsg(read);
                    }
                }
            }

        } catch (UnknownHostException ex) {
            log.error("Error connecting to server", ex);
        } catch (IOException ex) {
            log.error("Error receiving message from server", ex);
        } finally {
            log.info("Closing " + clientSocket);
            try {
                clientSocket.close();
            } catch (IOException ex) {
                log.error("Disconnecting error", ex);
            }

        }

    }

    // reading and displaying a string message
    public void getMsg(String msg) {
        System.out.println(msg);
    }

    // reading and displaying xml messages
    public void getXmlMsg(String msg) {
        try {
            // parser initialization
            jaxbParser = ParserProvider.newJaxbParser();
            StringReader stringReader = new StringReader(msg);
            MessageXml message = (MessageXml) jaxbParser.getObject(stringReader, MessageXml.class);
            // get message sending time
            Date dateNow = message.getDate().toGregorianCalendar().getTime();
            // creating separators
            String lineTopSeparator = "<----" + formatForDateNow.format(dateNow) + "---->\n";
            String lineBottomSeparator = "<------------------->\n";
            // message output to console
            System.out.println(lineTopSeparator + message + "\n" + lineBottomSeparator);
        } catch (SAXException ex) {
            log.error("Error parser initialization", ex);
        } catch (JAXBException ex) {
            log.error("Marshalling error", ex);
        }
    }




}
