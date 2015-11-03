package com.asda.zombiex.net;

/**
 * @author Skala
 */
public interface ServerCallback {
    void serverReady();
    void clientConnected(String remoteAddress);
    void initClient(String remoteAddress);
}
