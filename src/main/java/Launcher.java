import utils.message.impl.MessageXml;
import org.xml.sax.SAXException;
import utils.parser.impl.JaxbParser;


import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Launcher {
    public static void main(String[] args) throws JAXBException, SAXException, IOException, InterruptedException, DatatypeConfigurationException {
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

        ExecutorService executorService = Executors.newCachedThreadPool();
        Runnable serverTask = new Runnable() {
            @Override
            public void run() {
                Socket socket;
                BufferedReader in;
                BufferedWriter out;
                try {
                    ServerSocket serverSocket = new ServerSocket(7856);
                    System.out.println("server started");
                    socket = serverSocket.accept();
                    System.out.println("Connected: " + socket.getInetAddress());
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    //out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                    StringReader stringReader = new StringReader(in.readLine());
                    MessageXml msg = (MessageXml) jaxbParser.getObject(stringReader, MessageXml.class);
                    System.out.println("server:\n" + msg);
                    SimpleDateFormat formatForDateNow = new SimpleDateFormat("hh:mm:ss E");
                    Date date = msg.getDate().toGregorianCalendar().getTime();
                    System.out.println(formatForDateNow.format(date));

                } catch (IOException | JAXBException e) {
                    e.printStackTrace();
                }
            }
        };

        executorService.execute(serverTask);
        executorService.shutdown();


        Socket socket = new Socket("localhost", 7856);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        StringWriter stringWriter = new StringWriter();
        MessageXml message = new MessageXml();
        GregorianCalendar cal = new GregorianCalendar();
        XMLGregorianCalendar xmlCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
        message.setDate(xmlCalendar);
        message.setFrom("onekzy");
        message.setTo("all");
        message.setTitle("Test");
        message.setSubject("Example");
        message.setBody("dsadadsadadsa");
        jaxbParser.saveObject(stringWriter, message);
        out.write(stringWriter.toString());
        out.newLine();
        out.flush();
        //System.out.println(in.readLine());
        //MessageXml msg = (MessageXml) jaxbParser.getObject(in, MessageXml.class);
        //Thread.sleep( 1000);
        //System.out.println(msg);

        //File file = new File("message.xml");
        //String xml = null;

        //jaxbParser.saveObject(System.out, message);

        //MessageXml message1 = (MessageXml) jaxbParser.getObject(file, MessageXml.class);
        //System.out.println(message1);


    }
}
