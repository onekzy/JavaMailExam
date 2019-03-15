package client.release;

import client.Client;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;
import utils.factory.ParserProvider;
import utils.message.impl.Command;
import utils.message.impl.Details;
import utils.message.impl.MessageXml;
import utils.parser.impl.JaxbParser;
import client.clientEntities.ClientSender;

import javax.xml.bind.JAXBException;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;



public class ClientConsole  {
    // initialization of logger
    private static final Logger log = Logger.getLogger(ClientConsole.class);
    private static final int SERVER_PORT = 3443;
    private static InetAddress localhost;
    private String login = null;
    private Socket clientSocket = null;
    private ClientSender clientSender = null;
    private Scanner scanner = new Scanner(System.in);
    private JaxbParser jaxbParser = null;
    // date format
    private SimpleDateFormat formatForDateNow = new SimpleDateFormat("hh:mm:ss E");
    // commands
    private static Map<String, Integer> commandsCode;
    BufferedReader in = null;
    BufferedWriter out = null;

    static {
        commandsCode = new HashMap<>();
        commandsCode.put("/i", 11);
        commandsCode.put("/world", 12);
        commandsCode.put("/pm", 13);
        commandsCode.put("/add", 14);
        commandsCode.put("/ss", 15);
        commandsCode.put("/ch", 16);
        commandsCode.put("/f", 17);
        commandsCode.put("/o", 18);
        commandsCode.put("/h", 19);
        commandsCode.put("/exit", 21);
    }


    public ClientConsole() {

        try {
            // get address by localhost
            localhost = InetAddress.getByName("localhost");
            // server socket connection
            clientSocket = new Socket(localhost, SERVER_PORT);
            // initialization of input / output streams
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            // receiving a string from the server about entering a login
            getSystemMsg(parseCommand(in.readLine()));
            // input login
            String login = scanner.nextLine();
            this.setLogin(login);
            sendOut(login);
            // receiving a string from the server about entering a password
            getSystemMsg(parseCommand(in.readLine()));
            // input password
            sendOut(scanner.nextLine());
            // creating a thread sending msg to server instance
            clientSender = new ClientSender(out, scanner, this);
            // start thread send
            new Thread(clientSender).start();

            // receiving msg loop
            while (true) {
                String raw = in.readLine();
                // if the message is not empty
                if(raw != null && !raw.equals("") && !raw.equals(" ")) {
                    Command command = parseCommand(raw);

                    if(command.getCode() == 777) {
                        getSystemMsg(command);
                    } else {
                        getHumanMsg(command);
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
    public void sendOut(String line) {
        try {
            Command command = new Command();
            Details details = new Details();
            MessageXml messageXml = new MessageXml();
            messageXml.setBody(line);
            command.setCode(777);
            details.setMessage(messageXml);
            command.setDetails(details);
            StringWriter stringWriter = new StringWriter();
            jaxbParser = ParserProvider.newJaxbParser();
            jaxbParser.saveObject(stringWriter, command);
            out.write(stringWriter.toString());
            out.newLine();
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (JAXBException e) {
            e.printStackTrace();
        }

    }

    public Command parseCommand(String raw) {
        Command command = null;
        StringReader stringReader = new StringReader(raw);
        try {
            jaxbParser = ParserProvider.newJaxbParser();
            command = (Command) jaxbParser.getObject(stringReader, Command.class);
        } catch (SAXException e) {
            e.printStackTrace();
        }catch (JAXBException e) {
            e.printStackTrace();
        }

        return command;
    }

    // reading and displaying a string message
    public void getSystemMsg(Command command) {
        System.out.println(command.getDetails().getMessage().getFrom() + ":" + command.getDetails().getMessage().getBody());
    }

    // reading and displaying xml messages
    public void getHumanMsg(Command command) {

            // get message sending time
            Date dateNow = command.getDetails().getMessage().getDate().toGregorianCalendar().getTime();
            // creating separators
            String timeStamp = formatForDateNow.format(dateNow);
            String sender = command.getDetails().getMessage().getFrom();
            String message = command.getDetails().getMessage().getBody();
            // message output to console
            System.out.println(timeStamp + "-" + sender + ": " + message);
    }

    public Map<String, Integer> getCommandMap() {
        return commandsCode;
    }


    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }
}
