package com.asda.zombiex.net;

import com.badlogic.gdx.math.Vector2;

/**
 * @author Skala
 */
public interface ServerResponseListener {
    void serverStartClient();
    void serverCreatePlayer(String namePlayer);
    void serverAssignPlayer(String namePlayer);
    void serverPositionPlayer(String namePlayer, Vector2 value);
    void serverViewfinderRadian(String namePlayer, float radian);
    void serverShot(String namePlayer);
    void serverVelocityPlayer(String namePlayer, Vector2 value);
    void setNickname(String namePlayer, String nickname);
    void setLeaderboard(String namePlayer, int scoreKill, int scoreDead);
    void updateLeaderboard();
}
