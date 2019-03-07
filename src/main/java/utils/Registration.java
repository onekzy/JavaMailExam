package utils;

import entities.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Registration implements Runnable {
    String login;
    String password;
    String confirmPassword;
    Socket clientSocket;
    Server server;
    boolean regFlag = false;

    PrintWriter out;
    BufferedReader in;

    public Registration(Socket clientSocket, Server server) {
        this.clientSocket = clientSocket;
        this.server = server;
    }

    @Override
    public void run() {
        try {

            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            while (!regFlag)
            regOrAuth(out, in);

            Connection connection = server.getCon(login);
            connection.setServer(this.server);
            connection.setClientSocket(this.clientSocket);
            connection.onSetup();



            new Thread(connection).start();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void regOrAuth(PrintWriter out, BufferedReader in) throws IOException {
        out.println("Введите логин:");
        login = in.readLine();
        out.println("Введите пароль:");
        password = in.readLine();
        if(!server.checkReg(login)) {
            out.println("Подтвердите пароль:");
            confirmPassword = in.readLine();
            if(password.equals(confirmPassword)) {
                server.addRegUser(login, password);
                out.println("Вы, " + login + ", зарегистрированы. Нажмите клавишу 'Enter' для входа в чат");
                regFlag = true;
            } else {
                out.println("Пароли не совпадают");
            }
        } else if (server.getAuthority(login, password)) {
            out.println("Вы, " + login + ", вошли. Нажмите клавишу 'Enter' для входа в чат");
            regFlag = true;
        } else {
            out.println("Неверный пароль");
        }
    }
}
