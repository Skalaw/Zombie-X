package com.asda.zombiex.net;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.ServerSocket;
import com.badlogic.gdx.net.ServerSocketHints;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.utils.Array;

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
    private ServerCallback serverCallback;
    private Array<String> requests = new Array<String>();

    // create a thread that will listen for incoming socket connections
    public void startServer(ServerCallback callback) {
        serverCallback = callback;

        threadHandlingServer = new Thread(new Runnable() {
            @Override
            public void run() {
                ServerSocketHints serverSocketHint = new ServerSocketHints();
                serverSocketHint.acceptTimeout = 0;

                ServerSocket serverSocket = Gdx.net.newServerSocket(Net.Protocol.TCP, PORT, serverSocketHint);
                serverCallback.serverReady();

                while (true) {
                    // Create a socket
                    Socket socketClient = serverSocket.accept(null);
                    clientSocket.add(socketClient);
                    String remoteAddress = socketClient.getRemoteAddress();
                    Gdx.app.log("Server", "Address: " + remoteAddress + " is connected");
                    serverCallback.initClient(remoteAddress);
                    serverCallback.clientConnected(remoteAddress);
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
                    if (socket == null) {
                        continue;
                    }

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
                            Array<String> splitedRequest = splitRequest(str);
                            /*requests.addAll(splitedRequest);
                            if (indexSocket == clientSocket.size() - 1) {
                                serverCallback.request(socket.getRemoteAddress(), requests);
                            }*/

                            serverCallback.request(socket.getRemoteAddress(), splitedRequest);
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

    private Array<String> splitRequest(String response) {
        Array<String> splitedResponse = new Array<String>();
        int firstChar = 0;
        int lastChar = response.indexOf("|");
        while (lastChar != -1) {
            String responseTask = response.substring(firstChar, lastChar + 1);
            firstChar = lastChar + 1;
            lastChar = response.indexOf("|", firstChar);
            splitedResponse.add(responseTask);
        }
        return splitedResponse;
    }

    public void sendResponse(String remoteAddress, String simpleTask) {
        if (simpleTask.startsWith("IP:")) {
            int charEnd = simpleTask.indexOf(" ");
            String recipientAddress = simpleTask.substring(3, charEnd);
            simpleTask = simpleTask.substring(charEnd + 1);

            simpleTask = "client:" + remoteAddress + " " + simpleTask;

            sendDataSocket(recipientAddress, simpleTask.getBytes());
        } else {
            simpleTask = "client:" + remoteAddress + " " + simpleTask;

            sendDataSockets(simpleTask.getBytes());
        }
    }

    public void sendDataSocket(String remoteIp, byte[] bytes) {
        for (int i = 0; i < clientSocket.size(); i++) {
            Socket socket = clientSocket.get(i);

            if (socket.getRemoteAddress().equals(remoteIp)) {
                try {
                    socket.getOutputStream().write(bytes);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void sendDataSockets(byte[] bytes) {
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
