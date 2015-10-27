package com.asda.zombiex.net;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.ServerSocket;
import com.badlogic.gdx.net.ServerSocketHints;
import com.badlogic.gdx.net.Socket;

import java.io.IOException;
import java.util.ArrayList;

/**
 * @author Skala
 */
public class Server {
    public final static int PORT = 12203;

    private ArrayList<Socket> clientSocket = new ArrayList<Socket>();
    private Thread threadIncomingClients;
    private Thread threadHandlingServer;

    // create a thread that will listen for incoming socket connections
    public void startServer() {
        threadHandlingServer = new Thread(new Runnable() {
            @Override
            public void run() {
                ServerSocketHints serverSocketHint = new ServerSocketHints();
                serverSocketHint.acceptTimeout = 0;

                ServerSocket serverSocket = Gdx.net.newServerSocket(Net.Protocol.TCP, PORT, serverSocketHint);

                while (true) {
                    Gdx.app.log("Server", "accept");
                    // Create a socket
                    Socket socketClient = serverSocket.accept(null);
                    Gdx.app.log("Server", "Address: " + socketClient.getRemoteAddress()); // TODO: create new player
                    clientSocket.add(socketClient);
                    // TODO: here should be send info actually game (in while join to game)
                    createThreadServerIfNotExist();
                }
            }
        });
        threadHandlingServer.start();
    }

    private void createThreadServerIfNotExist() {
        if (threadIncomingClients == null) {
            handleSockets();
        }
    }

    // Thread checking and sending date to others sockets
    private void handleSockets() {
        threadIncomingClients = new Thread(new Runnable() {
            @Override
            public void run() {
                int indexSocket = 0;
                while (true) {
                    if (clientSocket.isEmpty()) {
                        Gdx.app.log("Server", "clientSocket: " + clientSocket.size()); // without this log, thread doesn't work????
                        continue;
                    }

                    Socket socket = clientSocket.get(indexSocket);

                    int available = 0;
                    try {
                        available = socket.getInputStream().available();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (available != 0) {
                        byte[] buffer = new byte[available];
                        try {
                            socket.getInputStream().read(buffer);
                            String str = new String(buffer, "UTF-8");
                            Gdx.app.log("Server", socket.getRemoteAddress() + " send: " + str);

                            sendDataSockets(str.getBytes());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    indexSocket = nextSocket(indexSocket);
                }
            }
        });
        threadIncomingClients.start();
    }

    private void sendDataSockets(byte[] bytes) {
        for (int i = 0; i < clientSocket.size(); i++) {
            Socket socket = clientSocket.get(i);
            try {
                socket.getOutputStream().write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private int nextSocket(int indexSocket) {
        indexSocket++;
        indexSocket %= clientSocket.size();
        return indexSocket;
    }
}
