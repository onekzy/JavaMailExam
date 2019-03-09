package server.serverEntities;

import server.release.TcpServer;
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
    TcpServer server;
    // флаг цикла проверки регистрации/входа
    boolean regFlag = false;

    PrintWriter out;
    BufferedReader in;

    public Registration(Socket clientSocket, TcpServer server) {
        this.clientSocket = clientSocket;
        this.server = server;
    }

    @Override
    public void run() {
        try {

            // инициализация потоков общения
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            // регистрация или аутентификация
            do {
                this.getAccept();
            } while (!regFlag);

            // создание и настройка соединения
            Connection connection = server.getCon(login);
            connection.setServer(this.server);
            connection.setClientSocket(this.clientSocket);
            connection.onSetup();

            // запуск потока соединения
            new Thread(connection).start();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // вход в чат, выполнение регистрации или входа
    public void getAccept() throws IOException {
        out.println("Введите логин:");
        login = in.readLine();
        out.println("Введите пароль:");
        password = in.readLine();

        if(!server.checkReg(login)) {
            doRegistration(login, password);
        } else if (server.getAuthority(login, password)) {
            doAuthentication(login);
        } else {
            out.println("Неверный пароль");
        }
    }

    // проверка регистрации
    public void doRegistration(String login, String password) throws IOException {
        out.println("Подтвердите пароль:");
        confirmPassword = in.readLine();
        if(password.equals(confirmPassword)) {
            server.addRegUser(login, password);
            out.println("Вы, " + login + ", зарегистрированы. Нажмите клавишу 'Enter' для входа в чат");
            regFlag = true;
        } else {
            out.println("Пароли не совпадают");
        }
    }

    // проверка аутентификации
    public void doAuthentication(String login) {
        out.println("Вы, " + login + ", вошли. Нажмите клавишу 'Enter' для входа в чат");
        regFlag = true;
    }
}
