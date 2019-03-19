package client.clientEntities;

import client.release.ClientConsole;
import com.melloware.jintellitype.HotkeyListener;
import com.melloware.jintellitype.JIntellitype;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;
import utils.nativeUtils.Counter;
import utils.factory.ParserProvider;
import utils.message.impl.Command;
import utils.message.impl.Details;
import utils.message.impl.MessageXml;
import utils.parser.impl.JaxbParser;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.awt.event.KeyEvent;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;

import java.util.GregorianCalendar;
import java.util.Scanner;

public class ClientSender implements Runnable {
    BufferedWriter out;
    // console input
    private Scanner scanner;
    //logger
    private static final Logger log = Logger.getLogger(ClientSender.class);
    private static boolean swapMode;

    // client instance
    private ClientConsole clientConsole = null;

    public ClientSender(BufferedWriter out, Scanner scanner, ClientConsole clientConsole) {
        this.out = out;
        this.scanner = scanner;
        this.clientConsole = clientConsole;
        swapMode = false;
    }

    static {
        initMode();
    }



    @Override
    public void run() {

        // send messages to server loop
        while (ClientConsole.proceed) {


            String outMsg = scanner.nextLine().trim();



                if (isNotEmptyLine(outMsg)) {
                    try {
                        sendXml(getFinalXml(outMsg));
                        if (outMsg.equals("/exit")) {
                            ClientConsole.proceed = false;
                        }
                    } catch (SAXException e) {
                        log.error("Parser error", e);
                    } catch (JAXBException e) {
                        log.error("Marshalling error", e);
                    } catch (IOException e) {
                        log.error("Input error", e);
                    } catch (DatatypeConfigurationException e) {
                        log.error("Timestamp error", e);
                    }

                }
            }

            }
    // native library init
    public static void initMode() {
        int id = 123;

        String s = System.getProperty("java.io.tmpdir");
        String postfix = "nativeutils"+ Counter.getCount() +"\\\\"+"JIntellitype.dll";
        String path = s.concat(postfix);
        JIntellitype.setLibraryLocation ( path );
        JIntellitype.getInstance().registerHotKey(id, JIntellitype.MOD_CONTROL, KeyEvent.VK_P);

        JIntellitype.getInstance().addHotKeyListener(new HotkeyListener() {
            @Override
            public void onHotKey(int i) {

                if(ClientSender.swapMode == true) {
                    ClientSender.swapMode = false;
                    System.out.println("[System:]: Chatting mode is enable");
                } else {
                    ClientSender.swapMode = true;
                    System.out.println("[System:]: Command mode is enable");
                }
            }
        });
    }


    public void sendXml(Command command) throws SAXException, JAXBException, IOException {
        StringWriter stringWriter = new StringWriter();
        JaxbParser jaxbParser = ParserProvider.newJaxbParser();
        jaxbParser.saveObject(stringWriter, command);
        out.write(stringWriter.toString());
        out.newLine();
        out.flush();
    }

    // command processing
    public Command getFinalXml(String line) throws DatatypeConfigurationException {

        Command command = null;
        String core = line.split(" ")[0];

        if(!isCommandLine(line)) {
            if(ClientSender.swapMode == true) {
                System.out.println("[System:]: Commands must start with '/'");
                command = createEmptyXml();
            } else {
            command = createGeneralXml(line); }

        } else if(isExistingCommand(core))  {
            if(ClientSender.swapMode == false) {
                System.out.println("[System:]: Only chatting. Press 'Ctrl + P' for changing mode");
                command = createEmptyXml();
            } else {
                switch (core) {
                    case "/pm":
                        command = createPrivateXml(line);
                        break;
                    case "/ss":
                        command = createStatusXml(line);
                        break;
                    case "/ch":
                        command = createHistoryXml(line);
                        break;
                    case "/add":
                        command = createAddXml(line);
                        break;
                    default:
                        command = createDefaultXml(line);
                }
            }

        } else {
            command = createErrorXml();
        }
        return command;
    }

    public Command createErrorXml() {
        Command command = new Command();
        command.setCode(000);
        return getFoolCommand(command);
    }

    public Command createEmptyXml() {
        Command command = new Command();
        command.setCode(123);
        return getFoolCommand(command);
    }

    private Command getFoolCommand(Command command) {
        MessageXml messageXml = new MessageXml();
        messageXml.setFrom(clientConsole.getLogin());
        Details details = new Details();
        details.setMessage(messageXml);
        command.setDetails(details);

        return command;
    }

    public Command createGeneralXml(String line) throws DatatypeConfigurationException {
        Command command = new Command();
        Details details = new Details();
        MessageXml messageXml = new MessageXml();
        command.setCode(1);
        messageXml.setTo("All");
        messageXml.setFrom(clientConsole.getLogin());
        messageXml.setBody(line);
        return getFinalCommand(command, details, messageXml);
    }

    public Command createStatusXml(String line) throws DatatypeConfigurationException {
        Command command = new Command();
        Details details = new Details();
        MessageXml messageXml = new MessageXml();
        messageXml.setFrom(clientConsole.getLogin());

        String[] divided = line.split(" ");
        command.setCode(clientConsole.getCommandMap().get(divided[0]));
        if(divided.length == 2) {
            messageXml.setTo(divided[1]);
        } else if(divided.length == 1) {
            System.out.println("Enter a new status");
            messageXml.setTo(scanner.nextLine());
        } else {
            System.out.println("Something else...");
        }
        return getFinalCommand(command, details, messageXml);
    }

    public Command createDefaultXml(String line) throws DatatypeConfigurationException {
        Command command  = new Command();
        command.setCode(clientConsole.getCommandMap().get(line));
        Details details = new Details();
        MessageXml messageXml = new MessageXml();
        messageXml.setFrom(clientConsole.getLogin());
        details.setMessage(messageXml);
        command.setDetails(details);
        return getFinalCommand(command, details, messageXml);
    }


    public Command createAddXml(String line) throws DatatypeConfigurationException {
        Command command = new Command();
        Details details = new Details();
        MessageXml messageXml = new MessageXml();
        messageXml.setFrom(clientConsole.getLogin());

        String[] divided = line.split(" ");
        command.setCode(clientConsole.getCommandMap().get(divided[0]));
        if(divided.length == 2) {
            messageXml.setTo(divided[1]);
        } else if(divided.length == 1) {
            System.out.println("Enter the login name of the user who will be added to the friend list");
            messageXml.setTo(scanner.nextLine());
        } else {
            System.out.println("Something else...");
        }
        return getFinalCommand(command, details, messageXml);
    }

    public Command createHistoryXml(String line) throws DatatypeConfigurationException {
        Command command = new Command();
        Details details = new Details();
        MessageXml messageXml = new MessageXml();
        messageXml.setFrom(clientConsole.getLogin());

        String[] divided = line.split(" ");
        command.setCode(clientConsole.getCommandMap().get(divided[0]));
        if(divided.length == 2) {
            messageXml.setTo(divided[1]);
        } else if(divided.length == 1) {
            System.out.println("Enter the name of the user with whom you want to see the chatting history");
            messageXml.setTo(scanner.nextLine());
        } else {
            System.out.println("Something else...");
        }
        return getFinalCommand(command, details, messageXml);
    }

    public Command createPrivateXml(String line) throws DatatypeConfigurationException {
        Command command = new Command();
        Details details = new Details();
        MessageXml messageXml = new MessageXml();
        messageXml.setFrom(clientConsole.getLogin());

        String[] divided = line.split(" ");
        command.setCode(clientConsole.getCommandMap().get(divided[0]));
        if(divided.length == 2) {
            messageXml.setTo(divided[1]);
            System.out.println("Enter a message");
            messageXml.setBody(scanner.nextLine());
        } else if(divided.length == 1) {
            System.out.println("Enter the recipient of the message");
            messageXml.setTo(scanner.nextLine());
            System.out.println("Enter a message");
            messageXml.setBody(scanner.nextLine());
        } else {
            System.out.println("Something else...");
        }
        return getFinalCommand(command, details, messageXml);
    }

    // constant setup
    private Command getFinalCommand(Command command, Details details, MessageXml messageXml) throws DatatypeConfigurationException {
        XMLGregorianCalendar xmlCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar());
        messageXml.setDate(xmlCalendar);
        details.setMessage(messageXml);
        command.setDetails(details);
        return command;
    }


    public boolean isCommandLine(String line) {
        return String.valueOf(line.charAt(0)).equals("/");
    }

    public boolean isNotEmptyLine(String line) {
        return line != null && !line.equals("") && !line.equals(" ") && !line.equals("/");
    }

    public boolean isExistingCommand(String line) {
        return clientConsole.getCommandMap().keySet().contains(line);
    }

}
