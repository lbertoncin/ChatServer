package org.academiadecodigo.asciimos.chatserver;

import org.academiadecodigo.asciimos.chatserver.server.ClientHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    public static void main(String[] args) throws IOException {
        ExecutorService cachedPool = Executors.newCachedThreadPool();
        ServerSocket serverSocket = new ServerSocket(9000);

        while (true) {
            ClientHandler clientHandler = new ClientHandler(serverSocket.accept());
            cachedPool.submit(clientHandler);
        }
    }
}
