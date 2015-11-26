package com.asda.zombiex.net;

/**
 * @author Skala
 */
public interface ClientRequestListener {
    void analogIntensity(String remoteAddress, float intensity);
    void analogRadian(String remoteAddress, float radian);
    void firstButtonClicked(String remoteAddress);
    boolean secondButtonClicked(String remoteAddress);
}
