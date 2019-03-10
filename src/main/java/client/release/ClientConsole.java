package client.release;

import client.Client;
import org.xml.sax.SAXException;
import utils.message.impl.MessageXml;
import utils.parser.impl.JaxbParser;
import server.release.TcpServer;
import client.clientEntities.ClientSender;

import javax.xml.bind.JAXBException;
import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;



public class ClientConsole implements Client {
    private Socket clientSocket = null;
    private String login = null;
    private String name = null;
    private String password = null;
    private ClientSender clientSender = null;
    private Scanner scanner = new Scanner(System.in);
    JaxbParser jaxbParser = null;
    SimpleDateFormat formatForDateNow = new SimpleDateFormat("hh:mm:ss E");

    MessageXml message = new MessageXml();


    public ClientConsole() {
        BufferedReader in = null;
        PrintWriter out = null;
        try {
            clientSocket = new Socket("localhost", TcpServer.PORT);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            System.out.println(in.readLine());
            out.println(scanner.nextLine());
            System.out.println(in.readLine());
            out.println(scanner.nextLine());
            clientSender = new ClientSender(out, scanner);
            new Thread(clientSender).start();

            while (true) {
                String read = in.readLine();
                if(read != null && !read.equals("") && !read.equals(" ")) {
                    if(read.charAt(0) == '<' && read.charAt(read.length()-1) == '>') {
                        getXmlMsg(read);
                    } else {
                        getMsg(read);
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.out.println("Closing " + clientSocket);
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    public void getMsg(String msg) {
        System.out.println(msg);
    }

    public void getXmlMsg(String msg) {
        try {
            jaxbParser = new JaxbParser();
            StringReader stringReader = new StringReader(msg);
            MessageXml message = (MessageXml) jaxbParser.getObject(stringReader, MessageXml.class);
            Date dateNow = message.getDate().toGregorianCalendar().getTime();
            String lineTopSeparator = "<----" + formatForDateNow.format(dateNow) + "---->\n";
            String lineBottomSeparator = "<------------------->\n";

            System.out.println(lineTopSeparator + message + "\n" + lineBottomSeparator);
        } catch (SAXException | JAXBException e) {
            e.printStackTrace();
        }
    }




}
