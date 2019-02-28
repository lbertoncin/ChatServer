package org.academiadecodigo.asciimos.chatserver.server;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
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

        System.out.println("Client has connected from: " + clientSocket.getRemoteSocketAddress());
        cachedPool.submit(new ServerBroadcaster("Client has connected: " + name + "\n"));
        BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        listener(input);
    }

    private void listener(BufferedReader input) throws IOException {
        String line = "";
        while ((line = input.readLine()) != null) {
            if (line.equals("")) {
                send("[SERVER] Cannot send empty message!\n");
                continue;
            }

            // command
            if (line.charAt(0) == '/') {
                StringBuilder string = new StringBuilder(line);
                string.deleteCharAt(0);

                if (string.length() == 0) {
                    send("[SERVER] Command not found! EG: /help\n");
                    continue;
                }

                String[] args = string.toString().split(" ");
                // commands with no arguments
                if (args[0].equals("help")) {
                    send("----- Command Helper -----\n" +
                            "1. /setname <name>\n" +
                            "2. /quit <reason>\n" +
                            "--------------------\n");
                }

                // commands with arguments only
                if (args.length <= 1) {
                    send("[SERVER] You have to specify the argument!\n");
                    continue;
                }

                if (args[0].equals("setname")) {
                    send("[SERVER] You have changed your name succesfully to " + args[1] + "!\n");
                    this.name = args[1];
                }

                if (args[0].equals("quit")) {
                    send("[SERVER] Byeeee " + name + "!\n");
                    synchronized (clients) {
                        clientSocket.close();
                        clients.remove(this);
                    }
                    cachedPool.submit(new ServerBroadcaster(name + " has disconnected: " + args[1] + "\n"));
                    return;
                }
                continue;
            }
            cachedPool.submit(new ServerBroadcaster("[" + name + "] " + line + "\n"));
        }
    }

    private void send(String data) {
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            writer.write(data);
            writer.flush();
        } catch (SocketException e) {
            System.out.println("Client " + name + " is not answering, removing it from the list.");
            synchronized (clients) {
                clients.remove(this);
            }
        } catch (IOException e) {
            synchronized (clients) {
                clients.remove(this);
            }
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
                    c.send(data);
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
