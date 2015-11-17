package com.asda.zombiex.net;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.net.SocketHints;

import java.io.IOException;

/**
 * @author Skala
 */
public class Client {
    private Socket socket;
    private ClientCallback clientCallback;
    private String host;

    public void startClient(ClientCallback clientCallback, String host) {
        this.clientCallback = clientCallback;
        this.host = host;

        handleSocket();
    }

    // create a thread that will listen data from incoming server
    private void handleSocket() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                SocketHints socketHints = new SocketHints();
                socket = Gdx.net.newClientSocket(Net.Protocol.TCP, host, Server.PORT, socketHints); // "127.0.0.1" - localhost

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

                            clientCallback.onResponse(str);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();
    }

    public void sendToServer(String send) {
        Gdx.app.log("Client", "send: " + send);
        try {
            socket.getOutputStream().write(send.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}