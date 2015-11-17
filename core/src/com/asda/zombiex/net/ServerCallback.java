package com.asda.zombiex.net;

import com.badlogic.gdx.utils.Array;

/**
 * @author Skala
 */
public interface ServerCallback {
    void serverReady();
    void clientConnected(String remoteAddress);
    void initClient(String remoteAddress);
    void request(String remoteAddress, Array<String> request);
}
