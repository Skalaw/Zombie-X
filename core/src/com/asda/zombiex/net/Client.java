package com.asda.zombiex.net;

import com.asda.zombiex.states.Play;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.net.SocketHints;

import java.io.IOException;

/**
 * @author Skala
 */
public class Client {
    private final static int PORT = 12203;
    private Socket socket;
    private Play play;

    public void startClient(Play play, String host) {
        this.play = play;

        SocketHints socketHints = new SocketHints();

        socket = Gdx.net.newClientSocket(Net.Protocol.TCP, host, PORT, socketHints); // "127.0.0.1" - localhost
        handleSocket();
    }

    private void handleSocket() {
        new Thread(new Runnable() {
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
                            Gdx.app.log("Server", socket.getRemoteAddress() + " send: " + str);

                            if (str.equals("jump")) {
                                play.getActualPlayer().jump();
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();
    }

    public void clientSendJump() {
        String jump = "jump";
        Gdx.app.log("Client", "clientSendJump");

        try {
            socket.getOutputStream().write(jump.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}