package entities;

import org.xml.sax.SAXException;
import parser.impl.JaxbParser;
import utils.ClientSender;

import javax.xml.bind.JAXBException;
import java.io.*;
import java.net.Socket;
import java.sql.SQLOutput;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;



public class Client {
    private Socket clientSocket = null;
    private String login = null;
    private String name = null;
    private String password = null;
    ClientSender clientSender = null;
    private Scanner scanner = new Scanner(System.in);
    JaxbParser jaxbParser = null;
    SimpleDateFormat formatForDateNow = new SimpleDateFormat("hh:mm:ss E");

    Message message = new Message();


    public Client() {
        BufferedReader in = null;
        PrintWriter out = null;
        try {
            clientSocket = new Socket("localhost", Server.PORT);
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
            Date dateNow = new Date();
            String lineTopSeparator = "<----" + formatForDateNow.format(dateNow) + "---->\n";
            String lineBottomSeparator = "<------------------->\n";
            jaxbParser = new JaxbParser();
            StringReader stringReader = new StringReader(msg);
            Message message = (Message) jaxbParser.getObject(stringReader, Message.class);
            System.out.println(lineTopSeparator + message + "\n" + lineBottomSeparator);
        } catch (SAXException | JAXBException e) {
            e.printStackTrace();
        }
    }




}
