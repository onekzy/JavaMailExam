package client.clientEntities;

import java.io.PrintWriter;
import java.util.Scanner;

public class ClientSender implements Runnable {
    PrintWriter out;
    // console input
    Scanner scanner;

    public ClientSender(PrintWriter out, Scanner scanner) {
        this.out = out;
        this.scanner = scanner;
    }


    @Override
    public void run() {

        // send messages to server loop
        while (true) {
            String outMsg = scanner.nextLine();
            sendMsg(outMsg);
        }
    }

    // send messages to the server
    public void sendMsg(String msg) {
        out.println(msg);
        out.flush();
    }

}
