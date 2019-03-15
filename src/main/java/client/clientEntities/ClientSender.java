package client.clientEntities;

import client.release.ClientConsole;
import org.xml.sax.SAXException;
import utils.factory.ParserProvider;
import utils.message.impl.Command;
import utils.message.impl.Details;
import utils.message.impl.MessageXml;
import utils.parser.impl.JaxbParser;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.GregorianCalendar;
import java.util.Scanner;


/*      error 000;
        commandList.add("/i - Info"); 11
        commandList.add("/world - Send in general chat"); 12
        commandList.add("/pm - Send in personal chat"); 13
        commandList.add("/add - Add friend"); 14
        commandList.add("/ss - Set status"); 15
        commandList.add("/ch - Chatting history"); 16
        commandList.add("/f - Friends"); 17
        commandList.add("/o - Online"); 18
        commandList.add("/h - Help"); 19
        commandList.add("/exit - Leave chat"); 21
 */

public class ClientSender implements Runnable {
    BufferedWriter out;
    // console input
    private Scanner scanner;
    // parser
    private JaxbParser jaxbParser;
    // client instance
    private ClientConsole clientConsole = null;
    // sending mode flag
    private String modeFlag = "###end###";

    public ClientSender(BufferedWriter out, Scanner scanner, ClientConsole clientConsole) {
        this.out = out;
        this.scanner = scanner;
        this.clientConsole = clientConsole;
    }


    @Override
    public void run() {

        // send messages to server loop
        while (true) {

            String outMsg = scanner.nextLine().trim();
            if(isNotEmptyLine(outMsg)) {

                try {
                    sendXml(getFinalXml(outMsg));
                } catch (SAXException e) {
                    e.printStackTrace();
                } catch (JAXBException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (DatatypeConfigurationException e) {
                    e.printStackTrace();
                }

            }
        }
    }


    public void sendXml(Command command) throws SAXException, JAXBException, IOException {
        StringWriter stringWriter = new StringWriter();
        jaxbParser = ParserProvider.newJaxbParser();
        jaxbParser.saveObject(stringWriter, command);
        out.write(stringWriter.toString());
        out.newLine();
        out.flush();
    }

    public Command getFinalXml(String line) throws DatatypeConfigurationException {
        Command command = null;
        if(!isCommandLine(line)) {
            command = createGeneralXml(line);
        } else if(isExistingCommand(line.split(" ")[0]))  {
            if(line.length() >=3 && line.substring(0, 3).equals("/pm")) {
                command = createPrivateXml(line);
            } else {
                command = new Command();
                command.setCode(clientConsole.getCommandMap().get(line));
                Details details = new Details();
                MessageXml messageXml = new MessageXml();
                messageXml.setFrom(clientConsole.getLogin());
                details.setMessage(messageXml);
                command.setDetails(details);
            }

        } else {
            command = new Command();
            command.setCode(000);
            MessageXml messageXml = new MessageXml();
            messageXml.setFrom(clientConsole.getLogin());
            Details details = new Details();
            details.setMessage(messageXml);
            command.setDetails(details);
        }
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
            System.out.println("Что-то пошло не так");
        }
        return getFinalCommand(command, details, messageXml);
    }

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
