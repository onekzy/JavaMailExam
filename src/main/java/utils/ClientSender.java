package utils;

import entities.Message;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.Scanner;

public class ClientSender implements Runnable {
    BufferedReader in = null;
    PrintWriter out = null;
    Scanner scanner = null;
    Message xmlMsg = null;

    public ClientSender(PrintWriter out, Scanner scanner) {
        this.out = out;
        this.scanner = scanner;
    }


    @Override
    public void run() {
        while (true) {
            String outMsg = scanner.nextLine();
            sendMsg(outMsg);
        }
    }

    public void sendMsg(String msg) {
        out.println(msg);
        out.flush();
    }

    public void sendXmlMsgToAll() {

    }

    public void sendXmlMsgToSpecificUser() {

    }

}
