package org.academiadecodigo.asciimos.chatserver.server;

import org.academiadecodigo.asciimos.chatserver.server.commands.Commands;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientHandler implements Runnable {

    private static List<ClientHandler> clients = new ArrayList<>();
    private ExecutorService cachedPool;

    private Socket clientSocket;
    private String name;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
        cachedPool = Executors.newCachedThreadPool();

        synchronized (clients) {
            clients.add(this);
        }

        name = "Client " + clients.size();
    }

    private void answerClient() throws IOException {
        BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        System.out.println("Client has connected from: " + clientSocket.getRemoteSocketAddress());
        cachedPool.submit(new ServerBroadcaster("Client has connected: " + name + "\n"));

        listener(input);
    }

    private void listener(BufferedReader input) throws IOException {
        String data = "";
        while ((data = input.readLine()) != null) {

            if (isInvalid(data) || isCommand(data)) {
                continue;
            }

            cachedPool.submit(new ServerBroadcaster("[" + name + "] " + data + "\n"));
        }
    }

    private boolean isCommand(String data) throws IOException {
        if (data.charAt(0) == '/') {
            StringBuilder builder = new StringBuilder(data);
            String commandString = builder.deleteCharAt(0).toString();

            Commands command = Commands.isCommand(commandString, this);

            if (command != null) {
                System.out.println("Executing command: /" + commandString);
                command.execute(this, commandString);
            }
            return true;
        }
        return false;
    }

    private boolean isInvalid(String data) {
        if (data.equals("")) {
            sendMessage("[SERVER] Cannot send empty message!\n");
            return true;
        }
        return false;
    }

    public void sendMessage(String data) {
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            writer.write(data + "\n");
            writer.flush();
        } catch (Exception e) {
            clients.remove(this);
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public void closeSocket() {
        try {
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            answerClient();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class ServerBroadcaster implements Runnable {

        private String data;

        private ServerBroadcaster(String data) {
            this.data = data;
        }

        private void broadcast(String data) throws IOException {
            synchronized (clients) {
                System.out.println("Broadcasting message: " + data.replace("\n", ""));
                for (ClientHandler c : clients) {
                    c.sendMessage(data);
                }
            }
        }

        @Override
        public void run() {
            try {
                broadcast(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
