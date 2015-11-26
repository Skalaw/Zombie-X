package com.asda.zombiex.net;

import com.asda.zombiex.utils.StringUtils;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.ServerSocket;
import com.badlogic.gdx.net.ServerSocketHints;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.utils.Array;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Skala
 */
public class Server {
    public final static int PORT = 12203;
    private final static int MAX_PLAYERS = 8;

    private static ExecutorService executorServer = Executors.newSingleThreadExecutor();
    private static ExecutorService executorClients = Executors.newFixedThreadPool(MAX_PLAYERS);

    private ArrayList<Socket> clientSocket = new ArrayList<Socket>();
    private ServerCallback serverCallback;
    private ClientRequestListener clientRequestListener;

    // create a thread that will listen for incoming socket connections
    public void startServer(ServerCallback callback, ClientRequestListener clientRequestListener) {
        serverCallback = callback;
        this.clientRequestListener = clientRequestListener;

        executorServer.execute(new Runnable() {
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
                    handleSockets(socketClient);
                }
            }
        });
    }

    // Thread checking and sending date to others sockets
    private void handleSockets(final Socket socket) {
        executorClients.execute(new Runnable() {
            @Override
            public void run() {
                while (true) {
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
                            Array<String> splitedRequest = ParserUtils.splitRequest(str);
                            parserServer(socket.getRemoteAddress(), splitedRequest);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    public void sendResponseClient(String remoteAddress, String simpleTask) {
        simpleTask = StringUtils.append("client:", remoteAddress, " ", simpleTask);
        sendDataSocket(remoteAddress, simpleTask.getBytes());
    }

    public void sendResponseClients(String remoteAddress, String simpleTask) {
        simpleTask = StringUtils.append("client:", remoteAddress, " ", simpleTask);
        sendDataSockets(simpleTask.getBytes());
    }

    private void sendDataSocket(String remoteIp, byte[] bytes) {
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

    private void parserServer(final String remoteAddress, final Array<String> request) {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < request.size; i++) {
                    parserServer(remoteAddress, request.get(i));
                }
            }
        });
    }

    private void parserServer(String remoteAddress, String request) {
        String requestParse = request.substring(0, request.length() - 1);

        if (requestParse.equals("jump")) {
            clientRequestListener.firstButtonClicked(remoteAddress);
        } else if (requestParse.startsWith("moving: ")) {
            String value = requestParse.replace("moving: ", "");
            float intensity = (float) Double.parseDouble(value);

            clientRequestListener.analogIntensity(remoteAddress, intensity);
        } else if (requestParse.startsWith("radian: ")) {
            String value = requestParse.replace("radian: ", "");
            float radian = (float) Double.parseDouble(value);
            clientRequestListener.analogRadian(remoteAddress, radian);

            sendResponseClients(remoteAddress, request);
        } else if (requestParse.equals("fire")) {
            if (clientRequestListener.secondButtonClicked(remoteAddress)) {
                sendResponseClients(remoteAddress, request);
            }
        }
    }
}
