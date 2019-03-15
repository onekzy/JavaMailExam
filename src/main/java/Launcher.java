
import org.xml.sax.SAXException;
import utils.message.impl.Command;
import utils.message.impl.Details;
import utils.message.impl.MessageXml;

import utils.parser.impl.JaxbParser;


import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.*;
import java.util.GregorianCalendar;

public class Launcher {
    public static void main(String[] args) throws JAXBException, SAXException, IOException, InterruptedException {
        System.out.println("asdsadsa");
        /*
        System.out.println(System.getProperty("user.dir"));
        //System.out.println(System.lineSeparator().toString());
        //String xsdFile = "D:\\DEVELOPMENT\\ConsoleChatv1\\src\\main\\resources\\schemas\\message.xsd";
        String xsdFile = "src\\main\\resources\\schemas\\message.xsd";
        File file = new File(xsdFile);
        FileReader fileReader = new FileReader(file);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String line = null;
        while((line = bufferedReader.readLine()) != null) {
            System.out.println(line);
        } */

        //TODO
        //1)Отправка конкретному пользователю


        final JaxbParser jaxbParser = new JaxbParser();
/*
        ExecutorService executorService = Executors.newCachedThreadPool();
        Runnable serverTask = new Runnable() {
            @Override
            public void run() {
                Socket socket;
                BufferedReader in;
                BufferedWriter out;
                try {
                    ServerSocket serverSocket = new ServerSocket(7856);
                    System.out.println("Server started");
                    socket = serverSocket.accept();
                    System.out.println("Connected: " + socket.getInetAddress());
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    //out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                    StringReader stringReader = new StringReader(in.readLine());
                    MessageXml msg = (MessageXml) jaxbParser.getObject(stringReader, MessageXml.class);
                    System.out.println("Server:\n" + msg);

                } catch (IOException | JAXBException e) {
                    e.printStackTrace();
                }
            }
        };

        executorService.execute(serverTask);
        executorService.shutdown(); */


        //Socket socket = new Socket("localhost", 7856);
        //BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        //BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        //StringWriter stringWriter = new StringWriter();
        GregorianCalendar cal = new GregorianCalendar();

        XMLGregorianCalendar xmlCalendar = null;
        try {
            xmlCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
        } catch (DatatypeConfigurationException ex) {
            System.out.println("sadasda");
        }

        File file = new File("src\\main\\resources\\file1");
        MessageXml message = new MessageXml();
        message.setFrom("onekzy");
        //message.setTo("all");
        //message.setBody("dsadadsadadsa");
        //message.setDate(xmlCalendar);

        Details details = new Details();
        details.setMessage(message);

        Command command = new Command();
        command.setCode(123);
        command.setDetails(details);


        jaxbParser.saveObject(file, command);

        Command command1 = (Command) jaxbParser.getObject(file, Command.class);
        System.out.println(command1.getCode() + " " + command1.getDetails().getMessage().getFrom() + " " + command1.getDetails().getMessage().getBody());
        //out.write(stringWriter.toString());
        //out.newLine();
        //out.flush();
        //System.out.println(in.readLine());
        //Message msg = (Message) jaxbParser.getObject(in, Message.class);
        //Thread.sleep( 1000);
        //System.out.println(msg);

        //File file = new File("message.xml");
        //String xml = null;

        //jaxbParser.saveObject(System.out, message);

        //Message message1 = (Message) jaxbParser.getObject(file, Message.class);
        //System.out.println(message1);


    }
}