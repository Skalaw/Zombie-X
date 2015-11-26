package com.asda.zombiex.net;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.net.SocketHints;
import com.badlogic.gdx.utils.Array;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Skala
 */
public class Client {
    private static ExecutorService executorReceiver = Executors.newSingleThreadExecutor();
    private static ExecutorService executorRequest = Executors.newSingleThreadExecutor();

    private Socket socket;
    private ClientCallback clientCallback;
    private ServerResponseListener serverResponseListener;
    private String host;

    private Vector2 tempVector = new Vector2(); // for perfomance

    public void startClient(ClientCallback clientCallback, ServerResponseListener serverResponseListener, String host) {
        this.clientCallback = clientCallback;
        this.serverResponseListener = serverResponseListener;
        this.host = host;

        handleSocket();
    }

    // create a thread that will listen data from incoming server
    private void handleSocket() {
        executorReceiver.execute(new Runnable() {
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
                            Array<String> responses = ParserUtils.splitResponse(str);
                            parserClient(responses);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    public void sendToServer(final String send) {
        executorRequest.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    socket.getOutputStream().write(send.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void parserClient(final Array<String> responses) {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < responses.size; i++) {
                    parserClient(responses.get(i));
                }
            }
        });
    }

    public void parserClient(String response) {
        if (response.startsWith("client:")) {
            int firstIndex = response.indexOf(":");
            int lastIndex = response.indexOf(" ");
            String remoteAddress = response.substring(firstIndex + 1, lastIndex);
            String action = response.substring(lastIndex + 1);

            if (action.startsWith("createPlayer")) {
                firstIndex = action.indexOf(":");
                String namePlayer = action.substring(firstIndex + 1);

                serverResponseListener.serverCreatePlayer(namePlayer);
            } else if (action.equals("assignPlayer")) {
                serverResponseListener.serverAssignPlayer(remoteAddress);
            } /*else if (server != null) {
                return;
            }*/ else if (action.startsWith("position: ")) {
                String value = action.replace("position: ", "");
                serverResponseListener.serverPositionPlayer(remoteAddress, tempVector.fromString(value));
            } else if (action.startsWith("radian: ")) {
                String value = action.replace("radian: ", "");
                float radian = (float) Double.parseDouble(value);
                serverResponseListener.serverViewfinderRadian(remoteAddress, radian);
            } else if (action.equals("fire")) {
                serverResponseListener.serverShot(remoteAddress);
            } else if (action.startsWith("velocity: ")) {
                String value = action.replace("velocity: ", "");
                serverResponseListener.serverVelocityPlayer(remoteAddress, tempVector.fromString(value));
            }
        }
    }
}